package game.ui;

import javax.swing.JFrame;

/**
 * Main game window container.
 */
public class GameWindow extends JFrame {
    
    public GameWindow() {
        setTitle("2D Incremental Shooter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        
        pack();
        setLocationRelativeTo(null);
    }
}
