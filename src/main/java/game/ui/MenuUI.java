package game.ui;

import game.GameEngine;
import game.data.GameData;
import java.awt.*;

/**
 * Main menu UI screen with continue/new game options.
 */
public class MenuUI {
    
    public void render(Graphics2D g2d, GameEngine engine) {
        g2d.setColor(new Color(10, 15, 30));
        g2d.fillRect(0, 0, 800, 600);
        
        // Title
        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        drawCenteredString(g2d, "INCREMENTAL SHOOTER", 800, 120);
        
        // Check if save exists
        GameData saveData = engine.getSaveManager().loadGame();
        boolean hasSave = (saveData != null && saveData.getCash() > 0);
        
        // Menu options
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        
        if (hasSave) {
            g2d.setColor(Color.WHITE);
            drawCenteredString(g2d, "Press C to CONTINUE", 800, 250);
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            g2d.setColor(Color.LIGHT_GRAY);
            drawCenteredString(g2d, "(Continue from last difficulty)", 800, 280);
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            g2d.setColor(Color.WHITE);
            drawCenteredString(g2d, "Press N for NEW GAME", 800, 330);
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            g2d.setColor(Color.LIGHT_GRAY);
            drawCenteredString(g2d, "(Select difficulty)", 800, 360);
        } else {
            g2d.setColor(Color.WHITE);
            drawCenteredString(g2d, "Press SPACE to Start New Game", 800, 270);
        }
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        g2d.setColor(Color.WHITE);
        drawCenteredString(g2d, "Press U for Upgrades", 800, 420);
        drawCenteredString(g2d, "Press S for Settings", 800, 460);
        drawCenteredString(g2d, "Press ESC to Quit", 800, 500);
    }
    
    private void drawCenteredString(Graphics2D g2d, String text, int width, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }
}

