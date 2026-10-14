package controller;

import graphics.rasterizer.BresenhamLineRasterizer;
import graphics.rasterizer.LineRasterizer;
import graphics.rasterizer.PolygonOutlineRasterizer;
import graphics.rasterizer.PolygonRasterizer;
import model.Line;
import model.Point;
import model.Polygon;
import view.Canvas;
import view.Window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

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
    // Drawing variables
    private String currentMode;
    private Line previewLine;
    private Polygon previewPolygon;
    private ArrayList<Point> previewPolygonPoints = new ArrayList<>();
    // UI
    private final Canvas canvas;
    private final JPanel controlPanel;
    // Rasterizers
    private final LineRasterizer lineRasterizer;
    private final PolygonRasterizer polygonRasterizer;
    // Objects
    private final ArrayList<Polygon> polygons = new ArrayList<>();
    private final ArrayList<Line> lines = new ArrayList<Line>();
    // Customizables
    private static final int LINE_COLOR = Color.WHITE.getRGB();
    private final int NEARBY_POINT_DISTANCE = 10;
    // States
    private boolean isShiftDown = false;


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

        JButton switchDrawModeButton = new JButton("Current Mode: LINES");
        switchDrawModeButton.setBackground(Color.WHITE);

        controlPanel.add(clearButton);
        controlPanel.add(switchDrawModeButton);

        // Events
        // Control Panel
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearEntireCanvas();
            }
        });

        switchDrawModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Tady by bylo lepší místo "==" dát equals, ale funguje to, takže if it ain't broken don't fix it
                if (currentMode == "Line") {
                    currentMode = "Polygon";
                } else {
                    currentMode = "Line";
                }
                previewPolygon = null;

                switchDrawModeButton.setText("Current Mode: " + currentMode.toUpperCase());
            }
        });

        // Mouse
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mouseButtonClickType = e.getButton();

                if (mouseButtonClickType == 1) {
                    // Left Mouse button click / Draw new shape
                    if (currentMode == "Polygon") {
                        if (previewPolygon == null) {
                            ArrayList<Line> polygonLines = new ArrayList<>();
                            polygonLines.add(new Line(getPoint(e), getPoint(e), LINE_COLOR));

                            previewPolygon = new Polygon(polygonLines);
                        } else {
                            Point startingPoint = previewPolygon.lines.getLast().point2;
                            previewPolygon.lines.add(new Line(startingPoint, getPoint(e), LINE_COLOR));
                        }
                    } else {
                        previewLine = new Line(getPoint(e), getPoint(e), LINE_COLOR);
                    }
                } else if (mouseButtonClickType == 2) {
                    // Middle Mouse button click / Delete points
                    ClosestPoint closestPointData = getClosestPoint(e); // {Polygon|Line closestObject; Point closestPoint}
                    if (closestPointData == null) return;

                    Object closestPointOriginObject = closestPointData.originObject;

                    if (closestPointOriginObject.getClass() == Polygon.class) {
                        Polygon targetPolygon = (Polygon) closestPointOriginObject;
                        for (Line polygonLine : targetPolygon.lines) {
                            if (polygonLine.point1 == closestPointData.point || polygonLine.point2 == closestPointData.point) {
                                targetPolygon.lines.remove(polygonLine);

                                if (targetPolygon.lines.size() > 3) {
                                    polygons.remove(targetPolygon);
                                }
                                break;
                            }
                        }
                    } else {
                        Line targetLine = (Line) closestPointOriginObject;
                        lines.remove(targetLine);
                    }

                    render();
                } else if (mouseButtonClickType == 3) {
                    // Right Mouse button click / Move the point
                    ClosestPoint closestPointData = getClosestPoint(e); // {Polygon|Line closestObject; Point closestPoint}
                    if (closestPointData == null) return;

                    Object closestPointOriginObject = closestPointData.originObject;

                    if (closestPointOriginObject.getClass() == Polygon.class) {
                        Polygon targetPolygon = (Polygon) closestPointOriginObject;
                        if (previewPolygon == null) {
                            // Procházíme přes všechny pointy v každé čáře polygonu a hledáme oba body vrcholu
                            // Poté to dáme do naší proměnné "previewPolygonPoints".
                            // Tady je to teď dost confusing a nejspíš proč se to nevykresluje., mě znalosti z Javy
                            // říkají že tam ukládám jenom reference na ten objekt, ale v tom případě by render() neměl
                            // mít problém ten polygon prostě znovu vykreslit když s tím bodem pohybuju. But it refuses
                            Point secondClosestPoint = new Point(1,1);
                            for (Line polygonLine: targetPolygon.lines) {
                                if (polygonLine.point1 == closestPointData.point || polygonLine.point2 == closestPointData.point) continue;

                                if (polygonLine.point1.x == closestPointData.point.x && polygonLine.point1.y == closestPointData.point.y) {
                                    secondClosestPoint = polygonLine.point1;
                                    break;
                                }
                                if (polygonLine.point2.x == closestPointData.point.x && polygonLine.point2.y == closestPointData.point.y) {
                                    secondClosestPoint = polygonLine.point2;
                                    break;
                                }
                            }

                            previewPolygonPoints.add(closestPointData.point);
                            previewPolygonPoints.add(secondClosestPoint);
                        }
                    } else {
                        Line targetLine = (Line) closestPointOriginObject;

                        // Remove from the completed lines list
                        lines.remove(targetLine);
                        // Switch the points because the Preview takes the Point2 as end point.
                        if (targetLine.point1 == closestPointData.point) {
                            Point temp = targetLine.point1;
                            targetLine.point1 = targetLine.point2;
                            targetLine.point2 = temp;
                        }

                        previewLine = (Line) closestPointOriginObject;
                    }

                    render();
                }

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Přidání nové úsečky
                if (previewLine != null) {
                    lines.add(previewLine);

                    previewLine = null;
                }

                // Přidání nového polygonu
                if (previewPolygon != null) {
                    if (!polygons.contains(previewPolygon)) {
                        polygons.add(previewPolygon);
                    }

                    // Polygon zapouzdření demonstrace
                    if (previewPolygon.getIsEditing()) {
                        previewPolygon.setIsEditing(false);
                        previewPolygon = null;
                    }
                }

                // Vyčistíme polygon edit cache
                if (!previewPolygonPoints.isEmpty()) {
                    previewPolygonPoints.clear();
                }

                render();
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // For whatever reason, pokud zrovna dragguju když je mód set to Polygon, tak keyPressed simply
                // doesn't fire at all and to top it all off MouseEvent has the .isShiftDown attribute so that solves that

                // Future Vojta here: In Hindsight this might be caused by my poor implementation of Java swing, will be fixed
                // when we do it on cvičení.
                isShiftDown = e.isShiftDown();

                // Místo porovnání režimu tady se ptáme jestli existuje Preview, protože co kdyby uživatel editoval
                // Polygon v režimu Lines.
                if (previewLine != null) {
                    previewLine.point2 = getStraightPoint(e, previewLine);

                    render();
                    lineRasterizer.rasterize(previewLine);
                }

                // Aktualizujeme polygon který kreslíme
                if (previewPolygon != null) {
                    Line lastPreviewPolygonLine = previewPolygon.lines.getLast();
                    lastPreviewPolygonLine.point2 = getStraightPoint(e, lastPreviewPolygonLine);

                    render();
                    polygonRasterizer.rasterize(previewPolygon);
                }

                // Aktualizujeme body, které aktuálně editujeme u již vytvořeného polygonu
                // Tohleto je actually neaktualizuje a nebudou dynamicky vykresleny, netuším proč
                if (previewPolygonPoints != null) {
                    for (Point previewPoint : previewPolygonPoints) {
                        previewPoint.x = e.getX();
                        previewPoint.y = e.getY();
                    }
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
                // Tohleto přestane, z mi neznámého důvodu, vypisovat a fungovat po nějaké chvilce kreslení.
                System.out.println(e.getKeyChar());
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
        if (!lines.isEmpty()) {
            for (Line line : lines) {
                lineRasterizer.rasterize(line);
            }
        }

        // Render all polygons
        if (!polygons.isEmpty()) {
            for (Polygon polygon : polygons) {
                polygonRasterizer.rasterize(polygon);
            }
        }

        canvas.repaint();
    }

    private void clearEntireCanvas() {
        polygons.clear();
        lines.clear();
        previewPolygonPoints.clear();

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

            if (ddxy < 50) {
                // Tady by normálně byl nádherný a ukázkový kód pro Úhlopříčku
            } else {
                if (dx < dy) {
                    finalPoint.x = x1;
                } else {
                    finalPoint.y = y1;
                }
            }
        }

        return finalPoint;
    }

    private Point getPoint(MouseEvent e) {
        return new Point(e.getX(), e.getY());
    }

    // Hledání a vracení nejbližšího bodu když chceme editovat/mazat bod

    class ClosestPoint {
        public Object originObject;
        public Point point;

        public ClosestPoint(Object OriginObject, Point pointd) {
            this.originObject = OriginObject;
            this.point = pointd;
        }
    }

    public static double getDistanceBetweenPoints(Point A, Point B) {
        return Math.sqrt((B.x - A.x)^2 + (B.y - A.y)^2);
    }

    private ClosestPoint getClosestPoint(MouseEvent e) {
        Point clickPoint = getPoint(e);

        if(!lines.isEmpty()) {

            for (Line line : lines) {
                if (getDistanceBetweenPoints(clickPoint, line.point1) < NEARBY_POINT_DISTANCE) {
                    return new ClosestPoint(line, line.point1);
                }

                if (getDistanceBetweenPoints(clickPoint, line.point2) < NEARBY_POINT_DISTANCE) {
                    return new ClosestPoint(line, line.point2);
                }
            }
        }

        if (!polygons.isEmpty()) {

            for (Polygon polygon : polygons) {
                for (Line line: polygon.lines) {
                    if (getDistanceBetweenPoints(clickPoint, line.point1) < NEARBY_POINT_DISTANCE) {
                        return new ClosestPoint(polygon, line.point1);
                    }

                    if (getDistanceBetweenPoints(clickPoint, line.point2) < NEARBY_POINT_DISTANCE) {
                        return new ClosestPoint(polygon, line.point2);
                    }
                }
            }
        }

        return null;
    }
}