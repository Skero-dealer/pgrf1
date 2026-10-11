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

    private Point startPoint;
    private Point currentPoint;

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
        this.lineRasterizer = new TrivialLineRasterizer(canvas.getRaster());
        this.polygonRasterizer = new PolygonOutlineRasterizer(lineRasterizer);

        ArrayList<Line> mockLines = new ArrayList<>();
        mockLines.add(new Line(new Point(100, 100), new Point(300, 100), Color.WHITE.getRGB()));
        mockLines.add(new Line(new Point(300, 100), new Point(350, 250), Color.WHITE.getRGB()));
        mockLines.add(new Line(new Point(350, 250), new Point(200, 350), Color.WHITE.getRGB()));
        mockLines.add(new Line(new Point(200, 350), new Point(100, 250), Color.WHITE.getRGB()));
        polygons.add(new Polygon(mockLines));
    }

    /*
     * -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Main functions
     * -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
     */

    public void init() {
        canvas.clear();

        // Control panel

        JButton clearButton = new JButton("Clear Canvas");
        clearButton.setBackground(Color.RED);

        controlPanel.add(clearButton);

        // Events
        // Control Panel
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearEntireCanvas();
            }
        });

        // Canvas
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = getPoint(e);
                currentPoint = startPoint;
            }

            @Override 
            public void mouseReleased(MouseEvent e) {
                lines.add(new Line(startPoint, getPoint(e), LINE_COLOR));
                currentPoint = null;
                startPoint = null;
                render();
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentPoint == null) return;
                if (startPoint == null) return;
                currentPoint = getPoint(e);
                
                render();
                lineRasterizer.rasterize(new Line(startPoint, currentPoint, LINE_COLOR));
                canvas.repaint();
            }
        });

        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == 'c') {
                    clearEntireCanvas();
                }
            }
        });

        canvas.repaint();
    }

    private void render() {
        canvas.clear();

        // Render all lines
        for (Line line: lines) {
            lineRasterizer.rasterize(line);
        }

        // Render all polygons
        for (Polygon polygon: polygons) {
            polygonRasterizer.rasterize(polygon);
        }
        canvas.repaint();
    }

    private void clearEntireCanvas() {
        polygons.clear();
        lines.clear();
        render();
    }
    /*
    private void render() {
        canvas.clear();
        polygonRasterizer.rasterize(polygon);
        canvas.repaint();
    }*/

    private void startLine(MouseEvent e) { // Když klikneme a začneme vykreslovat, takže vykreslit previewLine

    }
    private void finishPolygon(MouseEvent e) { // Když myš pustíme
        render();
    }

    private Point getPoint(MouseEvent e) {
        return new Point(e.getX(), e.getY());
    }

}