package game;

import game.ui.GameWindow;
import javax.swing.SwingUtilities;

/**
 * Entry point for the 2D Incremental Shooter game.
 * Initializes the game window and starts the application.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}
