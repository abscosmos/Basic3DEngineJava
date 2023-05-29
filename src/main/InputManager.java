package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

public class InputManager implements KeyListener {

    private final static HashMap<Integer, String> keyNames = new HashMap<>();

    static {
        keyNames.put(KeyEvent.VK_W, "w");
        keyNames.put(KeyEvent.VK_A, "a");
        keyNames.put(KeyEvent.VK_S, "s");
        keyNames.put(KeyEvent.VK_D, "d");

        keyNames.put(KeyEvent.VK_Q, "q");
        keyNames.put(KeyEvent.VK_E, "e");

        keyNames.put(KeyEvent.VK_LEFT, "left_arrow");
        keyNames.put(KeyEvent.VK_RIGHT, "right_arrow");
        keyNames.put(KeyEvent.VK_UP, "up_arrow");
        keyNames.put(KeyEvent.VK_DOWN, "down_arrow");
    }

    public static final HashMap<String, Boolean> keyMap = new HashMap<>();

    public static void createKeyMap() {
        for (String key : keyNames.values()) {
            keyMap.put(key, false);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        String keyName = keyNames.get(e.getKeyCode());
        if (keyName != null) {
            keyMap.replace(keyName, true);
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        String keyName = keyNames.get(e.getKeyCode());
        if (keyName != null) {
            keyMap.replace(keyName, false);
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {}


    public static void resetKeys() {
        keyMap.replaceAll((k, v) -> false);
    }

    // Input Functions

    public static int getAxis(String positive, String negative) {
        return (keyMap.get(positive) ? 1 : 0) - (keyMap.get(negative) ? 1 : 0);
    }
}
