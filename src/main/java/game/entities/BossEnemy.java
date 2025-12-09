package game.entities;

import java.awt.Color;

/**
 * Boss enemy - appears on boss waves (every 5 waves).
 * Large, tough, and shoots multiple projectiles.
 */
public class BossEnemy extends Enemy {
    private static final double SPEED = 30.0;
    private static final double FIRE_RATE = 1.5;
    
    private double fireTimer;
    private double sideMovement;
    
    public BossEnemy(double x, double y, int wave) {
        super(x, y, 60, 60,
              200 + (wave * 50), // Very high health
              15 + (wave * 5),
              100); // Lots of coins
        this.color = Color.ORANGE;
        this.velocityY = SPEED;
        this.velocityX = 100; // Side to side movement
        this.fireTimer = 0;
        this.sideMovement = 0;
    }
    
    @Override
    public void updateMovement(double deltaTime, Player player) {
        fireTimer += deltaTime;
        sideMovement += deltaTime;
        
        // Move side to side
        if (sideMovement > 2.0) {
            velocityX = -velocityX;
            sideMovement = 0;
        }
        
        // Bounce off walls
        if (x < 0 || x > 740) {
            velocityX = -velocityX;
        }
        
        // Stop moving down after reaching position
        if (y > 100) {
            velocityY = 0;
        }
    }
    
    @Override
    public Projectile tryShoot(Player player) {
        if (fireTimer >= FIRE_RATE && y > 50) {
            fireTimer = 0;
            
            // Boss shoots in multiple directions
            // Return one, but wave manager handles spawning pattern
            double dx = player.getCenterX() - getCenterX();
            double dy = player.getCenterY() - getCenterY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            double projectileSpeed = 150;
            double vx = (dx / distance) * projectileSpeed;
            double vy = (dy / distance) * projectileSpeed;
            
            return new Projectile(getCenterX(), getCenterY(), vx, vy, damage, false);
        }
        return null;
    }
}
