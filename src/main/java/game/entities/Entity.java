package game.entities;

import java.awt.Graphics2D;

/**
 * Base class for all game entities (Player, Enemies, Projectiles).
 * Provides common properties and behaviors using OOP principles.
 */
public abstract class Entity {
    protected double x, y;
    protected double velocityX, velocityY;
    protected double width, height;
    protected boolean alive;
    
    public Entity(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.alive = true;
    }
    
    /**
     * Update entity state - must be implemented by subclasses.
     */
    public abstract void update(double deltaTime);
    
    /**
     * Render entity - must be implemented by subclasses.
     */
    public abstract void render(Graphics2D g2d);
    
    /**
     * Check collision with another entity using AABB.
     */
    public boolean collidesWith(Entity other) {
        return this.x < other.x + other.width &&
               this.x + this.width > other.x &&
               this.y < other.y + other.height &&
               this.y + this.height > other.y;
    }
    
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void setVelocity(double vx, double vy) {
        this.velocityX = vx;
        this.velocityY = vy;
    }
    
    public void kill() {
        this.alive = false;
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean isAlive() { return alive; }
    public double getCenterX() { return x + width / 2; }
    public double getCenterY() { return y + height / 2; }
}
