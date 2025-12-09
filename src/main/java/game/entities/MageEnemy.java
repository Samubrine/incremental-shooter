package game.entities;

import java.awt.Color;

/**
 * Mage enemy that shoots projectiles at the player.
 * Appears in later waves.
 */
public class MageEnemy extends Enemy {
    private static final double SPEED = 50.0;
    private static final double FIRE_RATE = 2.0; // seconds
    
    private double fireTimer;
    
    public MageEnemy(double x, double y, int wave) {
        super(x, y, 30, 30,
              30 + (wave * 15), // More health than basic
              8 + (wave * 3),
              25); // More coins
        this.color = Color.MAGENTA;
        this.velocityY = SPEED;
        this.fireTimer = 0;
    }
    
    @Override
    public void updateMovement(double deltaTime, Player player) {
        // Mage moves slower
        fireTimer += deltaTime;
    }
    
    @Override
    public Projectile tryShoot(Player player) {
        if (fireTimer >= FIRE_RATE && y > 50 && y < 500) {
            fireTimer = 0;
            
            // Calculate direction to player
            double dx = player.getCenterX() - getCenterX();
            double dy = player.getCenterY() - getCenterY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            double projectileSpeed = 200;
            double vx = (dx / distance) * projectileSpeed;
            double vy = (dy / distance) * projectileSpeed;
            
            return new Projectile(getCenterX(), getCenterY(), vx, vy, damage, false);
        }
        return null;
    }
}
