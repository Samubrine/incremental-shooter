package game.systems;

import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class InputManager implements KeyListener {

    private Set<Character> pressedKeys;
    private Set<Integer> pressedKeyCodes;
    private boolean dashPressed;

    public InputManager() {
        this.pressedKeys = new HashSet<>();
        this.pressedKeyCodes = new HashSet<>();
    }

    public boolean isDashPressed() {
        return dashPressed;
    }

    public boolean isKeyPressed(char key) {
        return pressedKeys.contains(Character.toUpperCase(key));
    }

    public boolean isKeyPressed(int keyCode) {
        return pressedKeyCodes.contains(keyCode);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(Character.toUpperCase(e.getKeyChar()));
        pressedKeyCodes.add(e.getKeyCode());

        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            dashPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(Character.toUpperCase(e.getKeyChar()));
        pressedKeyCodes.remove(e.getKeyCode());

        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            dashPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public void clear() {
        pressedKeys.clear();
        pressedKeyCodes.clear();
        dashPressed = false;
    }
}
