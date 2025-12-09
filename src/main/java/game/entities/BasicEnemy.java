package game.entities;

import java.awt.Color;

/**
 * Basic enemy that moves straight toward player.
 */
public class BasicEnemy extends Enemy {
    private static final double SPEED = 80.0;
    
    public BasicEnemy(double x, double y, int wave) {
        super(x, y, 25, 25, 
              20 + (wave * 10), // Health scales with wave
              5 + (wave * 2),   // Damage scales with wave
              10);
        this.color = Color.RED;
        this.velocityY = SPEED;
    }
    
    @Override
    public void updateMovement(double deltaTime, Player player) {
        // Basic enemy just moves down
        // Velocity already set in constructor
    }
    
    @Override
    public Projectile tryShoot(Player player) {
        // Basic enemies don't shoot
        return null;
    }
}
