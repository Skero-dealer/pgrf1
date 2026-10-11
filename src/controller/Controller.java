package controller;

import java.awt.Color;
import java.awt.event.*;
import java.util.ArrayList;

import graphics.rasterizer.*;
import model.*;
import view.Canvas;
import view.Window;

import javax.swing.*;

/**
 * Handles user input and controls the application flow related to the
 * {@link Canvas}.
 * The controller coordinates input events, canvas operations, and rendering
 * updates.
 *
 * @author V.Šafránek
 * @version 2026
 */
public class Controller {

    private boolean isShiftDown = false;
    private String currentMode;
    private Line previewLine;
    private Polygon previewPolygon;

    private final Canvas canvas;
    private final JPanel controlPanel;

    private final LineRasterizer lineRasterizer;
    private final PolygonRasterizer polygonRasterizer;

    private final ArrayList<Polygon> polygons = new ArrayList<>();
    private final ArrayList<Line> lines = new ArrayList<Line>();
    private static final int LINE_COLOR = Color.WHITE.getRGB();
    private static final int PREVIEW_COLOR = Color.RED.getRGB();


    /*
     * -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- Constructors
     * -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
     */

    public Controller(Window window) {
        this.controlPanel = window.getControlPanel();
        this.canvas = window.getCanvas();
        this.lineRasterizer = new BresenhamLineRasterizer(canvas.getRaster());
        this.polygonRasterizer = new PolygonOutlineRasterizer(lineRasterizer);

        ArrayList<Line> mockLines = new ArrayList<>();
        mockLines.add(new Line(new Point(100, 100), new Point(300, 100), Color.WHITE.getRGB()));
        mockLines.add(new Line(new Point(300, 100), new Point(350, 250), Color.WHITE.getRGB()));
        mockLines.add(new Line(new Point(350, 250), new Point(200, 350), Color.WHITE.getRGB()));
        mockLines.add(new Line(new Point(200, 350), new Point(100, 250), Color.WHITE.getRGB()));
        polygons.add(new Polygon(mockLines));

        this.currentMode = "Line";
    }

    /*
     * -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Main functions
     * -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
     */

    public void init() {
        canvas.clear();
        render();

        // Control panel

        JButton clearButton = new JButton("Clear Canvas");
        clearButton.setBackground(Color.RED);

        JButton switchLineModeButton = new JButton("Current Mode: LINES");
        switchLineModeButton.setBackground(Color.WHITE);

        controlPanel.add(clearButton);
        controlPanel.add(switchLineModeButton);

        // Events
        // Control Panel
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearEntireCanvas();
            }
        });

        switchLineModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Tady by bylo lepší místo == dát equals, ale funguje to, takže if it ain't broken don't fix it
                if (currentMode == "Line") {
                    previewPolygon = null;
                    currentMode = "Polygon";
                } else {
                    currentMode = "Line";
                }

                switchLineModeButton.setText("Current Mode: " + currentMode.toUpperCase());
            }
        });

        // Canvas
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentMode == "Polygon") {
                    if (previewPolygon == null) {
                        ArrayList<Line> polygonLines = new ArrayList();
                        polygonLines.add(new Line(getPoint(e), getPoint(e), LINE_COLOR));

                        previewPolygon = new Polygon(polygonLines);
                    } else {
                        Point startingPoint = previewPolygon.lines.getLast().point2;
                        previewPolygon.lines.add(new Line(startingPoint, getPoint(e), LINE_COLOR));
                    }
                } else {
                    previewLine = new Line(getPoint(e), getPoint(e), LINE_COLOR);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentMode == "Line") {
                    lines.add(previewLine);
                } else {
                    if (!polygons.contains(previewPolygon)) {
                        polygons.add(previewPolygon);
                    }
                }

                previewLine = null;
                render();
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // For whatever reason, tak pokud zrovna dragguju když je mód set to Polygon, tak keyPressed simply
                // doesn't fire at all and to top it all off MouseEvent has the .isShiftDown attribute sooooooooo
                // that solves that java quirk!!!!
                isShiftDown = e.isShiftDown();

                // Proč místo režimu tady porovnávám preview? Protože je to zase jiný approach k této problematice
                if (previewLine != null) {
                    previewLine.point2 = getStraightPoint(e, previewLine);

                    render();
                    lineRasterizer.rasterize(previewLine);
                } else if (previewPolygon != null) {
                    Line lastPreviewPolygonLine = previewPolygon.lines.getLast();
                    lastPreviewPolygonLine.point2 = getStraightPoint(e, lastPreviewPolygonLine);

                    render();
                    polygonRasterizer.rasterize(previewPolygon);
                }

                canvas.repaint();
            }
        });

        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Update shiftkey
                isShiftDown = e.isShiftDown();

                // Clear canvas upon 'C'
                if (e.getKeyChar() == 'c' || e.getKeyChar() == 'C') {
                    clearEntireCanvas();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Update shiftkey
                isShiftDown = e.isShiftDown();
            }
        });

        canvas.repaint();
    }

    private void render() {
        canvas.clear();

        // Render all lines
        for (Line line : lines) {
            lineRasterizer.rasterize(line);
        }

        // Render all polygons
        for (Polygon polygon : polygons) {
            polygonRasterizer.rasterize(polygon);
        }
        canvas.repaint();
    }

    private void clearEntireCanvas() {
        polygons.clear();
        lines.clear();

        previewLine = null;
        previewPolygon = null;

        render();
    }

    private Point getStraightPoint(MouseEvent e, Line targetLine) {
        // Tady se řeší aby čára byla rovná když se drží SHIFT

        Point finalPoint = getPoint(e);
        if (isShiftDown) {
            // THE WRONG WAY TO DO IT
            // Pokud je rozdíl mezi x souřadnicemi větší než mezi y, tak je dominantní osa x,
            // tudíž úhel je nejspíš <45, takže zajístíme aby čára byla rovná podle osy x.
            // To samé pokud je větší rozdíl v ose y.
            int x1 = targetLine.point1.x;
            int y1 = targetLine.point1.y;
            int x2 = finalPoint.x;
            int y2 = finalPoint.y;

            int dx = Math.abs(x2 - x1);
            int dy = Math.abs(y2 - y1);
            int ddxy = Math.abs(dx - dy);

            //System.out.println("x: " + dx + "; y: " + dy);
            System.out.println(ddxy);
            if (ddxy < 50) {
                // Úhlopříčka

                /*
                finalPoint.x = x2 + dy;
                finalPoint.y = y2 + dx;
                // Zkusil jsem
                */
            } else {
                if (dx < dy) {
                    finalPoint.x = x1;
                } else {
                    finalPoint.y = y1;
                }
            }



            // THE RIGHT WAY TO DO IT
            // Tady jsem zkoušel Trigonometrii ale vůbec nic nefungovalo!!!
            // Keep in mind že tady previewLine == targetLine
                        /*
                        int x1 = previewLine.point1.x;
                        int y1 = previewLine.point1.y;
                        int x2 = finalPoint.x;
                        int y2 = finalPoint.y;
                        if (x1 > x2) {
                            int temp = x1;
                            x1 = x2;
                            x2 = temp;
                        }
                        if (y1 > y2) {
                            int temp = y1;
                            y1 = y2;
                            y2 = temp;
                        }

                        double adj = x1 - x2;
                        double opp = y1 - y2;
                        double hyp = Math.sqrt((x2 - x1)^2 + (y2 - y1)^2);

                        if (adj == 0) {
                            finalPoint.x = x1;
                        } else {
                            double ultimateAngle = Math.cos(adj / hyp);
                            //System.out.println(ultimateAngle);
                            if (ultimateAngle > 0) {
                                finalPoint.x = x1;
                            } else {
                                finalPoint.x = x2;
                            }
                        }
                        */
        }

        return finalPoint;
    }

    private Point getPoint(MouseEvent e) {
        return new Point(e.getX(), e.getY());
    }

}