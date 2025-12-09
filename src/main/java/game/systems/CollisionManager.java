package game.systems;

import game.entities.*;
import java.util.List;

/**
 * Handles all collision detection between entities.
 * Uses AABB collision detection.
 */
public class CollisionManager {
    
    public void checkCollisions(Player player, List<Enemy> enemies, 
                               List<Projectile> enemyProjectiles) {
        // Player projectiles vs enemies
        for (Projectile bullet : player.getProjectiles()) {
            if (!bullet.isAlive()) continue;
            
            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && bullet.collidesWith(enemy)) {
                    enemy.takeDamage(bullet.getDamage());
                    bullet.kill();
                    
                    if (!enemy.isAlive()) {
                        player.addCoins(enemy.getCoinValue());
                    }
                    break;
                }
            }
        }
        
        // Enemy projectiles vs player
        for (Projectile bullet : enemyProjectiles) {
            if (bullet.isAlive() && bullet.collidesWith(player)) {
                player.takeDamage(bullet.getDamage());
                bullet.kill();
            }
        }
        
        // Enemies vs player (collision damage)
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && enemy.collidesWith(player)) {
                player.takeDamage(enemy.getDamage());
                enemy.kill(); // Enemy dies on contact
            }
        }
    }
}
