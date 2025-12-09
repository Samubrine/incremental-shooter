package game.entities;

import java.awt.*;

/**
 * Base enemy class - demonstrates inheritance and polymorphism.
 * Different enemy types extend this class.
 */
public abstract class Enemy extends Entity {
    protected double health;
    protected double maxHealth;
    protected double damage;
    protected int coinValue;
    protected Color color;
    
    public Enemy(double x, double y, double width, double height, 
                 double health, double damage, int coinValue) {
        super(x, y, width, height);
        this.maxHealth = health;
        this.health = health;
        this.damage = damage;
        this.coinValue = coinValue;
    }
    
    /**
     * Each enemy type has different movement behavior.
     */
    public abstract void updateMovement(double deltaTime, Player player);
    
    /**
     * Some enemies shoot projectiles.
     */
    public abstract Projectile tryShoot(Player player);
    
    @Override
    public void update(double deltaTime) {
        // Move based on velocity
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        
        // Remove if out of bounds
        if (y > 650 || x < -100 || x > 900) {
            alive = false;
        }
    }
    
    public void takeDamage(double damage) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            alive = false;
        }
    }
    
    @Override
    public void render(Graphics2D g2d) {
        // Render enemy as pentagon
        g2d.setColor(color);
        int[] xPoints = new int[5];
        int[] yPoints = new int[5];
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        double radius = width / 2;
        
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(-90 + (360.0 / 5) * i);
            xPoints[i] = (int)(centerX + radius * Math.cos(angle));
            yPoints[i] = (int)(centerY + radius * Math.sin(angle));
        }
        g2d.fillPolygon(xPoints, yPoints, 5);
        
        // Health bar
        g2d.setColor(Color.RED);
        g2d.fillRect((int)x, (int)(y - 8), (int)width, 4);
        g2d.setColor(Color.GREEN);
        g2d.fillRect((int)x, (int)(y - 8), (int)(width * (health / maxHealth)), 4);
    }
    
    // Getters
    public double getDamage() { return damage; }
    public int getCoinValue() { return coinValue; }
}
