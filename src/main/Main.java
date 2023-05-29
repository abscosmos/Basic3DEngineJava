package main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class Main {
    private static final String windowTitle = "Space Defender";
    private static final double fpsUpdateSpeed = 0.125;
    public static Image icon;
    private static double delta;
    private static long startTime;
    private static double targetHealthPercent = 0;

    private static final Font directionsFont = new Font("Courier New", Font.PLAIN, 14);
    private static final Font scoreFont = new Font("Courier New", Font.PLAIN, 18);

    public static final Color bgColor = new Color(30, 32, 44);

    static {
        try { icon = ImageIO.read(new File("resources/icons/icon_full.png"));}
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

        PhysicsEngine.ready();
        Renderer.ready();

        JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
                g.setColor(bgColor);
                g.fillRect(0, 0, getWidth(), getHeight());

                Renderer.frameProcess(this, g);
                renderHUD(this, g);
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

            try { PhysicsEngine.process(delta); }
            catch (Exception e) { throw new RuntimeException(e); }
            Renderer.process(delta);

            panel.repaint();
        };

        Timer timer = new Timer(1000 / 60, repaintAction); timer.start();

        Timer setWindowTitle = new Timer((int) (fpsUpdateSpeed * 1000), e -> frame.setTitle(
                windowTitle + " | FPS: " + Math.round(1.0/delta) + " | " + Renderer.numTriangles + ", " + Renderer.numVisibleTris
        )); setWindowTitle.start();

        frame.add(panel); frame.revalidate();
    }

    public static void renderHUD(JPanel panel, Graphics g) {
        if(!PhysicsEngine.gameState.equals(PhysicsEngine.State.PLAY)) return;

        final int CORNER_SPACING = 16;
        final int UI_SPACING = 8;
        final int[] DIRECTIONS_BOX_DIMENSIONS = new int[] {235, 95};
        final int[] SCORE_BOX_DIMENSIONS = new int[] {200, 28};
        final int[] HEALTH_BAR_DIMENSIONS = new int[] {125, 20};
        final int UI_BOX_BORDER = 2;
        final int UI_TEXT_LINE_SPACING = 14;

        final String[] instructions = new String[] {
                "Space Defender:",
                "W,A,S,D to move",
                "E,Q to change elevation",
                "SPACE to shoot laser",
                "Shoot enemies & don't crash",
                "[Basic3DEngineJava example]"
        };

        final Color BORDER_COLOR = new Color(145, 115, 29);
        final Color BOX_FILL = new Color(58, 58, 58);
        final Color HEALTH_FULL = new Color(56, 162, 65);
        final Color HEALTH_EMPTY = new Color(173, 59, 59);

        g.setFont(directionsFont);

        int[] cornerOrigin = new int[] {CORNER_SPACING, panel.getHeight() - CORNER_SPACING};

        g.setColor(BORDER_COLOR);

        g.fillRect(
                cornerOrigin[0],
                cornerOrigin[1] - DIRECTIONS_BOX_DIMENSIONS[1] - 2 * UI_BOX_BORDER,
                DIRECTIONS_BOX_DIMENSIONS[0] + 2 * UI_BOX_BORDER,
                DIRECTIONS_BOX_DIMENSIONS[1] + 2 * UI_BOX_BORDER
        );

        g.setColor(BOX_FILL);

        g.fillRect(
                cornerOrigin[0] + UI_BOX_BORDER,
                cornerOrigin[1] - DIRECTIONS_BOX_DIMENSIONS[1] - UI_BOX_BORDER,
                DIRECTIONS_BOX_DIMENSIONS[0],
                DIRECTIONS_BOX_DIMENSIONS[1]
        );

        int[] scoreCornerOrigin = new int[] {cornerOrigin[0], cornerOrigin[1] - DIRECTIONS_BOX_DIMENSIONS[1] - 2 * UI_BOX_BORDER - UI_SPACING};

        g.setColor(BORDER_COLOR);

        g.fillRect(
                scoreCornerOrigin[0],
                scoreCornerOrigin[1] - SCORE_BOX_DIMENSIONS[1] - 2 * UI_BOX_BORDER,
                SCORE_BOX_DIMENSIONS[0] + 2 * UI_BOX_BORDER,
                SCORE_BOX_DIMENSIONS[1] + 2 * UI_BOX_BORDER
        );

        g.setColor(BOX_FILL);

        g.fillRect(
                scoreCornerOrigin[0] + UI_BOX_BORDER,
                scoreCornerOrigin[1] - SCORE_BOX_DIMENSIONS[1] - UI_BOX_BORDER,
                SCORE_BOX_DIMENSIONS[0],
                SCORE_BOX_DIMENSIONS[1]
        );

        int[] healthCornerOrigin = new int[] {cornerOrigin[0], scoreCornerOrigin[1] - SCORE_BOX_DIMENSIONS[1] - 2 * UI_BOX_BORDER - UI_SPACING};

        g.setColor(BORDER_COLOR);

        g.fillRect(
                healthCornerOrigin[0],
                healthCornerOrigin[1] - HEALTH_BAR_DIMENSIONS[1] - 2 * UI_BOX_BORDER,
                HEALTH_BAR_DIMENSIONS[0] + 2 * UI_BOX_BORDER,
                HEALTH_BAR_DIMENSIONS[1] + 2 * UI_BOX_BORDER
        );

        g.setColor(BOX_FILL);

        g.fillRect(
                healthCornerOrigin[0] + UI_BOX_BORDER,
                healthCornerOrigin[1] - HEALTH_BAR_DIMENSIONS[1] - UI_BOX_BORDER,
                HEALTH_BAR_DIMENSIONS[0],
                HEALTH_BAR_DIMENSIONS[1]
        );

        g.setColor(Color.WHITE);

        for(int i = instructions.length -1; i > -1; i--) {
            int bottomUpIdx = instructions.length -1 - i;
            g.drawString(instructions[i], cornerOrigin[0] + UI_BOX_BORDER + UI_SPACING, cornerOrigin[1] - UI_BOX_BORDER - UI_SPACING - bottomUpIdx * (UI_TEXT_LINE_SPACING));
        }

        g.setFont(scoreFont);
        g.drawString(("Eliminations: " + PhysicsEngine.player.enemyElims), scoreCornerOrigin[0] + UI_BOX_BORDER + UI_SPACING, scoreCornerOrigin[1] - UI_BOX_BORDER - UI_SPACING);

        double healthPercent = PhysicsEngine.player.getHealth()/100;

        targetHealthPercent = Mth.moveTowards(targetHealthPercent, healthPercent, 0.05);

        int healthBarLen = (int) ((HEALTH_BAR_DIMENSIONS[0] - 2 * UI_BOX_BORDER) * targetHealthPercent);

        g.setColor(lerpColor(HEALTH_EMPTY, HEALTH_FULL, targetHealthPercent));

        g.fillRect(
                healthCornerOrigin[0] + 2 * UI_BOX_BORDER,
                healthCornerOrigin[1] - HEALTH_BAR_DIMENSIONS[1],
                healthBarLen,
                HEALTH_BAR_DIMENSIONS[1] - 2 * UI_BOX_BORDER
        );
    }

    public static Color lerpColor(Color a, Color b, double t) {
        return new Color(
                (int) (Mth.lerp(a.getRed(), b.getRed(), t)),
                (int) (Mth.lerp(a.getGreen(), b.getGreen(), t)),
                (int) (Mth.lerp(a.getBlue(), b.getBlue(), t))
        );
    }
}