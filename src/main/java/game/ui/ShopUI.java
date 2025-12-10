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
        int y = 140;
        int spacing = 50;
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        
        drawUpgradeOption(g2d, "1. Fire Rate (Lvl " + um.getFireRateLevel() + ")", 
                         um.getTempUpgradeCost(UpgradeManager.UpgradeType.FIRE_RATE, um.getTempLevel(UpgradeManager.UpgradeType.FIRE_RATE)), 
                         y);
        y += spacing;
        drawUpgradeOption(g2d, "2. Damage (Lvl " + um.getDamageLevel() + ")", 
                         um.getTempUpgradeCost(UpgradeManager.UpgradeType.DAMAGE, um.getTempLevel(UpgradeManager.UpgradeType.DAMAGE)), 
                         y);
        y += spacing;
        drawUpgradeOption(g2d, "3. Health (Lvl " + um.getHealthLevel() + ")", 
                         um.getTempUpgradeCost(UpgradeManager.UpgradeType.HEALTH, um.getTempLevel(UpgradeManager.UpgradeType.HEALTH)), 
                         y);
        y += spacing;
        drawUpgradeOption(g2d, "4. Speed (Lvl " + um.getSpeedLevel() + ")", 
                         um.getTempUpgradeCost(UpgradeManager.UpgradeType.SPEED, um.getTempLevel(UpgradeManager.UpgradeType.SPEED)), 
                         y);
        y += spacing;
        drawUpgradeOption(g2d, "5. Bullet Count (Lvl " + um.getBulletCountLevel() + ")", 
                         um.getTempUpgradeCost(UpgradeManager.UpgradeType.BULLET_COUNT, um.getTempLevel(UpgradeManager.UpgradeType.BULLET_COUNT)), 
                         y);
        y += spacing;
        drawUpgradeOption(g2d, "6. Bullet Speed (Lvl " + um.getBulletSpeedLevel() + ")", 
                         um.getTempUpgradeCost(UpgradeManager.UpgradeType.BULLET_SPEED, um.getTempLevel(UpgradeManager.UpgradeType.BULLET_SPEED)), 
                         y);
        y += spacing;
        drawUpgradeOption(g2d, "7. Crit Chance (Lvl " + um.getCritChanceLevel() + ") +0.5%", 
                         um.getTempUpgradeCost(UpgradeManager.UpgradeType.CRIT_CHANCE, um.getTempLevel(UpgradeManager.UpgradeType.CRIT_CHANCE)), 
                         y);
        y += spacing;
        drawUpgradeOption(g2d, "8. Crit Damage (Lvl " + um.getCritDamageLevel() + ") +1%", 
                         um.getTempUpgradeCost(UpgradeManager.UpgradeType.CRIT_DAMAGE, um.getTempLevel(UpgradeManager.UpgradeType.CRIT_DAMAGE)), 
                         y);
        
        // Instructions
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        drawCenteredString(g2d, "Press number key to purchase upgrade (costs scale with level)", 800, 500);
        drawCenteredString(g2d, "Press SPACE to continue to next wave", 800, 550);
    }
    
    private void drawUpgradeOption(Graphics2D g2d, String text, int cost, int y) {
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, 200, y);
        g2d.setColor(Color.YELLOW);
        g2d.drawString("Cost: " + cost + " coins", 500, y);
    }
    
    private void drawCenteredString(Graphics2D g2d, String text, int width, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }
}
