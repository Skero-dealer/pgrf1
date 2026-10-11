package view;

import javax.swing.*;
import java.awt.*;

/**
 * Represents the main application window that displays the {@link Canvas}.
 * The window initializes the canvas, configures the Swing frame, and provides access to the canvas.
 *
 * @author PGRF FIM UHK
 * @version 2026
 */
public class Window extends JFrame {

    private final Canvas canvas;
    private final JPanel controlPanel;

    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- Constructors -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

    public Window(int width, int height) {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("FIM UHK | PGRF1 | 2026 | Vojta");
        setLayout(new BorderLayout());
        setResizable(false);

        this.controlPanel = new JPanel();
        add(controlPanel, BorderLayout.EAST);

        canvas = new Canvas(width, height);
        add(canvas, BorderLayout.WEST);
        pack();

        setLocationRelativeTo(null);
        canvas.setFocusable(true);
        canvas.grabFocus();
        setVisible(true);
    }

    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= Getters -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

    public Canvas getCanvas() {
        return canvas;
    }

    public JPanel getControlPanel() {
        return controlPanel;
    }

}