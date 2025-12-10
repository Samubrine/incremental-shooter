package game.ui;

import game.GameEngine;
import java.awt.*;

/**
 * Settings UI screen with volume control and data reset.
 */
public class SettingsUI {
    
    public void render(Graphics2D g2d, GameEngine engine) {
        g2d.setColor(new Color(30, 30, 50));
        g2d.fillRect(0, 0, 800, 600);
        
        // Title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        drawCenteredString(g2d, "SETTINGS", 800, 50);
        
        // Sound toggle
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        String soundStatus = engine.getSoundManager().isSoundEnabled() ? "ON" : "OFF";
        Color soundColor = engine.getSoundManager().isSoundEnabled() ? Color.GREEN : Color.RED;
        g2d.setColor(Color.WHITE);
        drawCenteredString(g2d, "Sound: ", 800, 150);
        g2d.setColor(soundColor);
        g2d.drawString(soundStatus, 450, 150);
        
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        drawCenteredString(g2d, "Press T to toggle", 800, 180);
        
        // Volume slider
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        drawCenteredString(g2d, "Master Volume", 800, 250);
        
        int volume = engine.getSoundManager().getVolume();
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        drawCenteredString(g2d, volume + "%", 800, 280);
        
        // Volume bar
        int barWidth = 400;
        int barHeight = 30;
        int barX = (800 - barWidth) / 2;
        int barY = 290;
        
        // Background
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(barX, barY, barWidth, barHeight);
        
        // Filled portion
        int fillWidth = (int)(barWidth * (volume / 100.0));
        g2d.setColor(new Color(100, 200, 100));
        g2d.fillRect(barX, barY, fillWidth, barHeight);
        
        // Border
        g2d.setColor(Color.WHITE);
        g2d.drawRect(barX, barY, barWidth, barHeight);
        
        // Instructions
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        drawCenteredString(g2d, "Use LEFT/RIGHT arrow keys to adjust volume", 800, 350);
        drawCenteredString(g2d, "Press - / + to adjust by 1%", 800, 380);
        
        // Audio Latency slider (only if HitSoundPlayer is initialized)
        if (engine.getHitSoundPlayer() != null) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            drawCenteredString(g2d, "Audio Latency", 800, 430);
            
            int latency = engine.getHitSoundPlayer().getLatencyOffset();
            String latencyText = latency < 0 ? latency + "ms (Predictive)" : 
                                latency > 0 ? "+" + latency + "ms (Delayed)" :
                                latency + "ms (Instant)";
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            Color latencyColor = latency < 0 ? new Color(100, 200, 255) : // Blue for predictive
                                latency > 0 ? new Color(255, 150, 100) :    // Orange for delayed
                                Color.GREEN;                                 // Green for instant
            g2d.setColor(latencyColor);
            drawCenteredString(g2d, latencyText, 800, 460);
            
            // Latency bar (-100 to +100)
            int latBarWidth = 400;
            int latBarHeight = 30;
            int latBarX = (800 - latBarWidth) / 2;
            int latBarY = 470;
            
            // Background
            g2d.setColor(new Color(50, 50, 50));
            g2d.fillRect(latBarX, latBarY, latBarWidth, latBarHeight);
            
            // Center line (0ms)
            int centerX = latBarX + latBarWidth / 2;
            g2d.setColor(new Color(80, 80, 80));
            g2d.fillRect(centerX - 1, latBarY, 2, latBarHeight);
            
            // Latency indicator
            int latencyPos = (int)(latBarWidth / 2 + (latBarWidth / 2.0) * (latency / 100.0));
            latencyPos = Math.max(0, Math.min(latBarWidth - 10, latencyPos));
            g2d.setColor(latencyColor);
            g2d.fillRect(latBarX + latencyPos, latBarY, 10, latBarHeight);
            
            // Border
            g2d.setColor(Color.WHITE);
            g2d.drawRect(latBarX, latBarY, latBarWidth, latBarHeight);
            
            // Latency instructions
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            drawCenteredString(g2d, "Use A/D or Q/E to adjust latency", 800, 520);
        }
        
        // Reset data option
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        drawCenteredString(g2d, "Press R to RESET ALL DATA", 800, 560);
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        drawCenteredString(g2d, "(This will delete all progress and upgrades)", 800, 585);
        
        // Back to menu
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        drawCenteredString(g2d, "Press ESC to return to menu", 800, 595);
    }
    
    private void drawCenteredString(Graphics2D g2d, String text, int width, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }
}

