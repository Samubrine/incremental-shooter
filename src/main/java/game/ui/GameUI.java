package game.ui;

import game.GameEngine;
import game.entities.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * In-game HUD and rendering.
 */
public class GameUI {
    
    public void render(Graphics2D g2d, GameEngine engine) {
        // Render game entities
        Player player = engine.getPlayer();
        if (player != null) {
            player.render(g2d);
        }
        
        // Render enemies and projectiles
        if (engine.getWaveManager() != null) {
            // Create defensive copies to avoid ConcurrentModificationException
            List<Enemy> enemiesCopy = new ArrayList<>(engine.getWaveManager().getEnemies());
            List<Projectile> projectilesCopy = new ArrayList<>(engine.getWaveManager().getEnemyProjectiles());
            
            for (Enemy enemy : enemiesCopy) {
                if (enemy.isAlive()) {
                    enemy.render(g2d);
                }
            }
            
            for (Projectile proj : projectilesCopy) {
                if (proj.isAlive()) {
                    proj.render(g2d);
                }
            }
        }
        
        // Render HUD
        renderHUD(g2d, engine);
        
        // Render pause overlay if paused
        if (engine.isPaused()) {
            renderPauseOverlay(g2d);
        }
    }
    
    private void renderHUD(Graphics2D g2d, GameEngine engine) {
        Player player = engine.getPlayer();
        if (player == null) return;
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        
        // Top left - Wave and health
        g2d.drawString("Wave: " + engine.getWaveManager().getCurrentWave() + "/15", 10, 20);
        g2d.drawString("Health: " + (int)player.getHealth() + "/" + (int)player.getMaxHealth(), 10, 40);
        g2d.drawString("Coins: " + player.getCoins(), 10, 60);
        
        // Top right - Difficulty
        g2d.drawString("Difficulty: " + engine.getCurrentDifficulty(), 680, 20);
        
        // Health bar at bottom
        int barWidth = 400;
        int barHeight = 20;
        int barX = (800 - barWidth) / 2;
        int barY = 560;
        
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(barX, barY, barWidth, barHeight);
        
        g2d.setColor(Color.RED);
        g2d.fillRect(barX, barY, (int)(barWidth * (player.getHealth() / player.getMaxHealth())), barHeight);
        
        g2d.setColor(Color.WHITE);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }
    
    private void renderPauseOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, 800, 600);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "PAUSED";
        int x = (800 - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, 300);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        text = "Press P to Resume";
        fm = g2d.getFontMetrics();
        x = (800 - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, 350);
    }
}
