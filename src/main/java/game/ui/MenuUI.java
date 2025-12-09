package game.ui;

import game.GameEngine;
import java.awt.*;

/**
 * Main menu UI screen.
 */
public class MenuUI {
    
    public void render(Graphics2D g2d) {
        // Title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        drawCenteredString(g2d, "INCREMENTAL SHOOTER", 800, 150);
        
        // Menu options
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        drawCenteredString(g2d, "Press SPACE to Start", 800, 300);
        drawCenteredString(g2d, "Press U for Upgrades", 800, 350);
        drawCenteredString(g2d, "Press S for Settings", 800, 400);
        drawCenteredString(g2d, "Press ESC to Quit", 800, 450);
    }
    
    private void drawCenteredString(Graphics2D g2d, String text, int width, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }
}
