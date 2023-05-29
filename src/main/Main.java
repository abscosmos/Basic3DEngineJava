package main;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.plaf.basic.BasicMenuBarUI;
import javax.swing.plaf.basic.BasicMenuUI;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class Main {
    private static final String windowTitle = "Basic 3D Java Engine";
    private static final double fpsUpdateSpeed = 0.125;
    public static Image icon = null;
    private static double delta;
    private static long startTime;

    private static JLabel triangleCount;

    public static Color bgColor = new Color(150, 200, 230);

    static {
        try { icon = ImageIO.read(new File("resources/icon.png"));}
        catch (IOException ignored) { }
    }


    public static void main(String[] args) throws IOException {
        int width = 1024, height = 1024;

        JFrame frame = new JFrame(windowTitle);
        frame.setLayout(new BorderLayout());

        frame.setIconImage(icon);
        frame.setVisible(true);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addKeyListener(new InputManager());
        InputManager.createKeyMap();
        frame.setFocusable(true);
        frame.requestFocusInWindow();

        createMenuBar(frame);
        PhysicsEngine.ready();
        Renderer.ready();

        JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
                g.setColor(bgColor);
                g.fillRect(0, 0, getWidth(), getHeight());

                Renderer.frameProcess(this, g);
            }
        };

        frame.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowLostFocus(WindowEvent e) { InputManager.resetKeys(); }
            @Override
            public void windowGainedFocus(WindowEvent e) { }
        });

        ActionListener repaintAction = evt -> {
            long endTime = System.currentTimeMillis();
            delta = ((endTime - startTime) * 1E-3);
            startTime = endTime;
            
            PhysicsEngine.process(delta);
            Renderer.process(delta);

            panel.repaint();
            triangleCount.setText(String.format("Tris: %,d | %,d", Renderer.numTriangles, Renderer.numVisibleTris));
        };

        Timer timer = new Timer(1000 / 60, repaintAction); timer.start();

        Timer setWindowTitle = new Timer((int) (fpsUpdateSpeed * 1000), e -> frame.setTitle(windowTitle + " (FPS: " + Math.round(1.0/delta) + ")")); setWindowTitle.start();

        frame.add(panel); frame.revalidate();
    }

    public static void createMenuBar(JFrame jframe) {
        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        jframe.setJMenuBar(menuBar);

        menuBar.setUI(new BasicMenuBarUI());

        // Menus
        JMenu debugMenu = new JMenu("Debug");
        JMenu colorsMenu = new JMenu("Colors");


        // Colors Menu
        JMenuItem pickColor = new JMenuItem("Change Color");
        JMenuItem pickBGColor = new JMenuItem("Change BG Color");

        // Debug Menu
        JCheckBoxMenuItem displayWireframe = new JCheckBoxMenuItem("Display Wireframe") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Icon icon = isSelected() ? new ImageIcon("resources/check.png") : new ImageIcon("resources/uncheck.png");
                icon.paintIcon(this, g, 4, 4);
            }
        };
        JCheckBoxMenuItem fillTriangles = new JCheckBoxMenuItem("Fill Triangles") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Icon icon = isSelected() ? new ImageIcon("resources/check.png") : new ImageIcon("resources/uncheck.png");
                icon.paintIcon(this, g, 4, 4);
            }
        };
        fillTriangles.setState(true);
        triangleCount = new JLabel(String.format("Tris: %,d | %,d", Renderer.numTriangles, Renderer.numVisibleTris));  triangleCount.setEnabled(false);

        JCheckBoxMenuItem displayColliders = new JCheckBoxMenuItem("Display Colliders") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Icon icon = isSelected() ? new ImageIcon("resources/check.png") : new ImageIcon("resources/uncheck.png");
                icon.paintIcon(this, g, 4, 4);
            }
        };

        for(JMenu menu : java.util.List.of(colorsMenu, debugMenu)) {
            menu.setUI(new BasicMenuUI() {
                @Override
                protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                    ButtonModel model = menuItem.getModel();
                    Color oldColor = g.getColor();
                    int menuWidth = menuItem.getWidth();
                    int menuHeight = menuItem.getHeight();

                    if (model.isArmed() || (menuItem instanceof JMenu && model.isSelected())) {
                        g.setColor(menuItem.getBackground().darker());
                        g.fillRect(0,0, menuWidth, menuHeight);
                    } else {
                        g.setColor(menuItem.getBackground());
                        g.fillRect(0,0, menuWidth, menuHeight);
                    }
                    g.setColor(oldColor);
                }

                @Override
                protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.drawString(text, textRect.x, (textRect.y + textRect.height) - 2);
                }
            });
        }

        // Add Items To Sub-Menus
        colorsMenu.add(pickColor);
        colorsMenu.add(pickBGColor);
        debugMenu.add(colorsMenu);

        debugMenu.add(displayWireframe);
        debugMenu.add(fillTriangles);
        debugMenu.add(displayColliders);
        debugMenu.add(triangleCount);

        menuBar.add(debugMenu);


        // Sense Updates
        for (Component c : menuBar.getComponents()) {
            if (c instanceof JMenu m) {
                m.addMenuListener(new MenuListener() {
                    @Override
                    public void menuSelected(MenuEvent e) {
                        InputManager.resetKeys();
                    }

                    @Override
                    public void menuDeselected(MenuEvent e) { }

                    @Override
                    public void menuCanceled(MenuEvent e) { }
                });
            }
        }

        pickColor.addActionListener(e -> {
            CustomColorPicker colorPicker = new CustomColorPicker(Renderer.drawColor);
            if (JOptionPane.showConfirmDialog(jframe, colorPicker, "Select Draw Color", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                Renderer.drawColor = colorPicker.previewColor.getBackground();
            }
        });

        pickBGColor.addActionListener(e -> {
            CustomColorPicker colorPicker = new CustomColorPicker(bgColor);
            if (JOptionPane.showConfirmDialog(jframe, colorPicker, "Select Background Color", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                bgColor = colorPicker.previewColor.getBackground();
            }
        });

        displayWireframe.addActionListener(e -> Renderer.doShowWireframe = displayWireframe.isSelected());
        fillTriangles.addActionListener(e -> Renderer.doFillTriangles = fillTriangles.isSelected());
        displayColliders.addActionListener(e -> Renderer.doDisplayColliders = displayColliders.isSelected());
    }
}
