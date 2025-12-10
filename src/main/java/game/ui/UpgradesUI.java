package game.ui;

import game.GameEngine;
import game.systems.UpgradeManager;
import game.data.GameData;
import java.awt.*;

/**
 * Permanent upgrades UI screen (purchased with cash).
 */
public class UpgradesUI {
    
    public void render(Graphics2D g2d, GameEngine engine) {
        g2d.setColor(new Color(20, 30, 50));
        g2d.fillRect(0, 0, 800, 600);
        
        // Title
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        drawCenteredString(g2d, "PERMANENT UPGRADES", 800, 50);
        
        // Cash display (load from save)
        GameData data = engine.getSaveManager().loadGame();
        int cash = data != null ? data.getCash() : 0;
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        drawCenteredString(g2d, "Cash: $" + cash, 800, 100);
        
        // Upgrade options
        UpgradeManager um = engine.getUpgradeManager();
        int y = 130;
        int spacing = 55;
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        
        drawPermanentUpgrade(g2d, "1. Fire Rate", 
                            um.getPermanentLevel(UpgradeManager.UpgradeType.FIRE_RATE),
                            um.getTempLevel(UpgradeManager.UpgradeType.FIRE_RATE),
                            um.getUpgradeCost(UpgradeManager.UpgradeType.FIRE_RATE, um.getPermanentLevel(UpgradeManager.UpgradeType.FIRE_RATE)), 
                            um.getStatCalculation(UpgradeManager.UpgradeType.FIRE_RATE),
                            cash, y);
        y += spacing;
        drawPermanentUpgrade(g2d, "2. Damage", 
                            um.getPermanentLevel(UpgradeManager.UpgradeType.DAMAGE),
                            um.getTempLevel(UpgradeManager.UpgradeType.DAMAGE),
                            um.getUpgradeCost(UpgradeManager.UpgradeType.DAMAGE, um.getPermanentLevel(UpgradeManager.UpgradeType.DAMAGE)),
                            um.getStatCalculation(UpgradeManager.UpgradeType.DAMAGE),
                            cash, y);
        y += spacing;
        drawPermanentUpgrade(g2d, "3. Health", 
                            um.getPermanentLevel(UpgradeManager.UpgradeType.HEALTH),
                            um.getTempLevel(UpgradeManager.UpgradeType.HEALTH),
                            um.getUpgradeCost(UpgradeManager.UpgradeType.HEALTH, um.getPermanentLevel(UpgradeManager.UpgradeType.HEALTH)),
                            um.getStatCalculation(UpgradeManager.UpgradeType.HEALTH),
                            cash, y);
        y += spacing;
        drawPermanentUpgrade(g2d, "4. Speed", 
                            um.getPermanentLevel(UpgradeManager.UpgradeType.SPEED),
                            um.getTempLevel(UpgradeManager.UpgradeType.SPEED),
                            um.getUpgradeCost(UpgradeManager.UpgradeType.SPEED, um.getPermanentLevel(UpgradeManager.UpgradeType.SPEED)),
                            um.getStatCalculation(UpgradeManager.UpgradeType.SPEED),
                            cash, y);
        y += spacing;
        drawPermanentUpgrade(g2d, "5. Bullet Count", 
                            um.getPermanentLevel(UpgradeManager.UpgradeType.BULLET_COUNT),
                            um.getTempLevel(UpgradeManager.UpgradeType.BULLET_COUNT),
                            um.getUpgradeCost(UpgradeManager.UpgradeType.BULLET_COUNT, um.getPermanentLevel(UpgradeManager.UpgradeType.BULLET_COUNT)),
                            um.getStatCalculation(UpgradeManager.UpgradeType.BULLET_COUNT),
                            cash, y);
        y += spacing;
        drawPermanentUpgrade(g2d, "6. Bullet Speed", 
                            um.getPermanentLevel(UpgradeManager.UpgradeType.BULLET_SPEED),
                            um.getTempLevel(UpgradeManager.UpgradeType.BULLET_SPEED),
                            um.getUpgradeCost(UpgradeManager.UpgradeType.BULLET_SPEED, um.getPermanentLevel(UpgradeManager.UpgradeType.BULLET_SPEED)),
                            um.getStatCalculation(UpgradeManager.UpgradeType.BULLET_SPEED),
                            cash, y);
        y += spacing;
        drawPermanentUpgrade(g2d, "7. Crit Chance", 
                            um.getPermanentLevel(UpgradeManager.UpgradeType.CRIT_CHANCE),
                            um.getTempLevel(UpgradeManager.UpgradeType.CRIT_CHANCE),
                            um.getUpgradeCost(UpgradeManager.UpgradeType.CRIT_CHANCE, um.getPermanentLevel(UpgradeManager.UpgradeType.CRIT_CHANCE)),
                            um.getStatCalculation(UpgradeManager.UpgradeType.CRIT_CHANCE),
                            cash, y);
        y += spacing;
        drawPermanentUpgrade(g2d, "8. Crit Damage", 
                            um.getPermanentLevel(UpgradeManager.UpgradeType.CRIT_DAMAGE),
                            um.getTempLevel(UpgradeManager.UpgradeType.CRIT_DAMAGE),
                            um.getUpgradeCost(UpgradeManager.UpgradeType.CRIT_DAMAGE, um.getPermanentLevel(UpgradeManager.UpgradeType.CRIT_DAMAGE)),
                            um.getStatCalculation(UpgradeManager.UpgradeType.CRIT_DAMAGE),
                            cash, y);
        
        // Instructions
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        drawCenteredString(g2d, "Press number key (1-8) to purchase permanent upgrade", 800, 540);
        drawCenteredString(g2d, "Press ESC to return to menu", 800, 570);
    }
    
    private void drawPermanentUpgrade(Graphics2D g2d, String name, int permLevel, int tempLevel, int cost, String statCalc, int cash, int y) {
        boolean canAfford = cash >= cost;
        
        // Upgrade name and levels
        g2d.setColor(Color.WHITE);
        String levelText = "[Cash: " + permLevel;
        if (tempLevel > 0) {
            levelText += " + Coins: " + tempLevel;
        }
        levelText += "]";
        g2d.drawString(name + " " + levelText, 50, y);
        
        // Cost
        g2d.setColor(canAfford ? Color.GREEN : Color.RED);
        g2d.drawString("$" + cost, 720, y);
        
        // Stat calculation (smaller font, gray)
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString(statCalc, 70, y + 15);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18)); // Reset font
    }
    
    private void drawCenteredString(Graphics2D g2d, String text, int width, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }
}
