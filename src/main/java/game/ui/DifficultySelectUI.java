package game.ui;

import game.GameEngine;
import game.data.DifficultyConfig;
import java.awt.*;

/**
 * Difficulty selection screen with 3x2 grid layout.
 * Shows 6 difficulty options with stats and start button zones.
 */
public class DifficultySelectUI {
    
    public void render(Graphics2D g2d, GameEngine engine, int unlockedDifficulty) {
        g2d.setColor(new Color(15, 20, 35));
        g2d.fillRect(0, 0, 800, 600);
        
        // Title
        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        drawCenteredString(g2d, "SELECT DIFFICULTY", 800, 40);
        
        // 3x2 grid of difficulty boxes
        int boxWidth = 240;
        int boxHeight = 160;
        int startX = 40;
        int startY = 80;
        int gapX = 20;
        int gapY = 20;
        
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int difficultyLevel = row * 3 + col + 1; // 1-6
                int x = startX + col * (boxWidth + gapX);
                int y = startY + row * (boxHeight + gapY);
                
                drawDifficultyBox(g2d, difficultyLevel, x, y, boxWidth, boxHeight, 
                                 difficultyLevel <= unlockedDifficulty);
            }
        }
        
        // Endless mode box - centered at bottom
        int endlessBoxWidth = 280;
        int endlessBoxHeight = 140;
        int endlessX = (800 - endlessBoxWidth) / 2;
        int endlessY = 380;
        drawEndlessBox(g2d, endlessX, endlessY, endlessBoxWidth, endlessBoxHeight);
        
        // Instructions
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        drawCenteredString(g2d, "Press number key (1-6) or E for Endless", 800, 545);
        drawCenteredString(g2d, "Press ESC to return to menu", 800, 570);
    }
    
    private void drawDifficultyBox(Graphics2D g2d, int level, int x, int y, 
                                   int width, int height, boolean unlocked) {
        DifficultyConfig config = DifficultyConfig.getConfig(level);
        
        // Box background
        if (unlocked) {
            g2d.setColor(new Color(40, 50, 70));
            g2d.fillRect(x, y, width, height);
            g2d.setColor(new Color(80, 100, 140));
            g2d.drawRect(x, y, width, height);
            g2d.drawRect(x+1, y+1, width-2, height-2);
        } else {
            // Locked appearance
            g2d.setColor(new Color(30, 30, 30));
            g2d.fillRect(x, y, width, height);
            g2d.setColor(new Color(60, 60, 60));
            g2d.drawRect(x, y, width, height);
        }
        
        int textY = y + 25;
        
        if (unlocked) {
            // Title
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            String title = "Difficulty " + level;
            drawCenteredStringInBox(g2d, title, x, width, textY);
            
            textY += 20;
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            drawCenteredStringInBox(g2d, config.getName(), x, width, textY);
            
            // Stats
            textY += 25;
            g2d.setColor(new Color(255, 150, 150));
            g2d.setFont(new Font("Arial", Font.PLAIN, 13));
            drawCenteredStringInBox(g2d, config.getHPModifierText(), x, width, textY);
            
            textY += 18;
            g2d.setColor(new Color(255, 200, 100));
            drawCenteredStringInBox(g2d, config.getCoinsModifierText(), x, width, textY);
            
            textY += 18;
            g2d.setColor(new Color(150, 255, 150));
            drawCenteredStringInBox(g2d, config.getCashModifierText(), x, width, textY);
            
            textY += 18;
            g2d.setColor(new Color(200, 150, 255));
            g2d.setFont(new Font("Arial", Font.PLAIN, 11));
            drawCenteredStringInBox(g2d, config.getSpecialEnemies(), x, width, textY);
            
            // Start button area
            int buttonY = y + height - 30;
            g2d.setColor(new Color(50, 150, 50));
            g2d.fillRect(x + 10, buttonY, width - 20, 20);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            drawCenteredStringInBox(g2d, "START", x, width, buttonY + 15);
            
        } else {
            // Locked
            g2d.setColor(new Color(100, 100, 100));
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            drawCenteredStringInBox(g2d, "LOCKED", x, width, y + height/2);
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            drawCenteredStringInBox(g2d, "Complete Difficulty " + (level-1), x, width, y + height/2 + 20);
        }
    }
    
    private void drawEndlessBox(Graphics2D g2d, int x, int y, int width, int height) {
        DifficultyConfig config = DifficultyConfig.getConfig(999);
        
        // Box background - special purple/gold theme for endless
        g2d.setColor(new Color(60, 40, 80));
        g2d.fillRect(x, y, width, height);
        g2d.setColor(new Color(180, 120, 255));
        g2d.drawRect(x, y, width, height);
        g2d.drawRect(x+1, y+1, width-2, height-2);
        
        int textY = y + 25;
        
        // Title
        g2d.setColor(new Color(255, 215, 0)); // Gold
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        drawCenteredStringInBox(g2d, "♾ ENDLESS MODE ♾", x, width, textY);
        
        textY += 25;
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        drawCenteredStringInBox(g2d, config.getHPModifierText() + " & Damage", x, width, textY);
        
        textY += 18;
        g2d.setColor(new Color(255, 200, 100));
        drawCenteredStringInBox(g2d, config.getCoinsModifierText(), x, width, textY);
        
        textY += 18;
        g2d.setColor(new Color(150, 255, 150));
        drawCenteredStringInBox(g2d, config.getCashModifierText(), x, width, textY);
        
        // Start button
        int buttonY = y + height - 30;
        g2d.setColor(new Color(100, 50, 150));
        g2d.fillRect(x + 10, buttonY, width - 20, 22);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        drawCenteredStringInBox(g2d, "START (Press E)", x, width, buttonY + 16);
    }
    
    private void drawCenteredStringInBox(Graphics2D g2d, String text, int boxX, int boxWidth, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = boxX + (boxWidth - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }
    
    private void drawCenteredString(Graphics2D g2d, String text, int width, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }
}
