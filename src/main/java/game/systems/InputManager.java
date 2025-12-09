package game.systems;

import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages keyboard input state.
 * Tracks which keys are currently pressed.
 */
public class InputManager implements KeyListener {
    private Set<Character> pressedKeys;
    private Set<Integer> pressedKeyCodes;
    
    public InputManager() {
        this.pressedKeys = new HashSet<>();
        this.pressedKeyCodes = new HashSet<>();
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
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(Character.toUpperCase(e.getKeyChar()));
        pressedKeyCodes.remove(e.getKeyCode());
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
    
    public void clear() {
        pressedKeys.clear();
        pressedKeyCodes.clear();
    }
}
