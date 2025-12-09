package game.ui;

import game.GameEngine;
import game.systems.UpgradeManager;
import java.awt.*;

/**
 * Shop UI for purchasing upgrades between waves.
 */
public class ShopUI {
    
    public void render(Graphics2D g2d, GameEngine engine) {
        g2d.setColor(new Color(20, 20, 40));
        g2d.fillRect(0, 0, 800, 600);
        
        // Title
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        drawCenteredString(g2d, "UPGRADE SHOP", 800, 50);
        
        // Coins display
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        drawCenteredString(g2d, "Coins: " + engine.getPlayer().getCoins(), 800, 100);
        
        // Upgrade options
        UpgradeManager um = engine.getUpgradeManager();
        int y = 150;
        int spacing = 60;
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        
        drawUpgradeOption(g2d, "1. Fire Rate (Lvl " + um.getFireRateLevel() + ")", y);
        y += spacing;
        drawUpgradeOption(g2d, "2. Damage (Lvl " + um.getDamageLevel() + ")", y);
        y += spacing;
        drawUpgradeOption(g2d, "3. Health (Lvl " + um.getHealthLevel() + ")", y);
        y += spacing;
        drawUpgradeOption(g2d, "4. Speed (Lvl " + um.getSpeedLevel() + ")", y);
        y += spacing;
        drawUpgradeOption(g2d, "5. Bullet Count (Lvl " + um.getBulletCountLevel() + ")", y);
        y += spacing;
        drawUpgradeOption(g2d, "6. Bullet Speed (Lvl " + um.getBulletSpeedLevel() + ")", y);
        
        // Instructions
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        drawCenteredString(g2d, "Press number key to purchase upgrade (50 coins each)", 800, 500);
        drawCenteredString(g2d, "Press SPACE to continue to next wave", 800, 550);
    }
    
    private void drawUpgradeOption(Graphics2D g2d, String text, int y) {
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, 200, y);
        g2d.setColor(Color.YELLOW);
        g2d.drawString("Cost: 50 coins", 500, y);
    }
    
    private void drawCenteredString(Graphics2D g2d, String text, int width, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }
}
