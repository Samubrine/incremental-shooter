package game.entities;

import java.awt.*;

/**
 * Projectile entity for both player and enemy bullets.
 * Uses composition pattern - owned by shooter.
 */
public class Projectile extends Entity {
    private double damage;
    private boolean playerOwned;
    private Color color;
    
    public Projectile(double x, double y, double vx, double vy, 
                     double damage, boolean playerOwned) {
        super(x, y, playerOwned ? 8 : 6, playerOwned ? 8 : 6);
        this.velocityX = vx;
        this.velocityY = vy;
        this.damage = damage;
        this.playerOwned = playerOwned;
        this.color = playerOwned ? Color.YELLOW : Color.RED;
    }
    
    @Override
    public void update(double deltaTime) {
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        
        // Remove if out of bounds
        if (x < -20 || x > 820 || y < -20 || y > 620) {
            alive = false;
        }
    }
    
    @Override
    public void render(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillOval((int)x, (int)y, (int)width, (int)height);
        
        // Add glow effect
        g2d.setColor(new Color(color.getRed(), color.getGreen(), 
                              color.getBlue(), 100));
        g2d.fillOval((int)(x-2), (int)(y-2), (int)(width+4), (int)(height+4));
    }
    
    public double getDamage() { return damage; }
    public boolean isPlayerOwned() { return playerOwned; }
}
