package game.ui;

import game.GameEngine;
import java.awt.*;

/**
 * Settings UI screen for game configuration.
 */
public class SettingsUI {
    
    public void render(Graphics2D g2d, GameEngine engine) {
        g2d.setColor(new Color(30, 30, 50));
        g2d.fillRect(0, 0, 800, 600);
        
        // Title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        drawCenteredString(g2d, "SETTINGS", 800, 50);
        
        // Sound setting
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        String soundStatus = engine.getSoundManager().isSoundEnabled() ? "ON" : "OFF";
        drawCenteredString(g2d, "Sound: " + soundStatus, 800, 200);
        drawCenteredString(g2d, "Press S to toggle", 800, 240);
        
        // Reset data option
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        drawCenteredString(g2d, "Press R to RESET ALL DATA", 800, 350);
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        drawCenteredString(g2d, "(This will delete all progress and upgrades)", 800, 380);
        
        // Back to menu
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        drawCenteredString(g2d, "Press ESC to return to menu", 800, 500);
    }
    
    private void drawCenteredString(Graphics2D g2d, String text, int width, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }
}
