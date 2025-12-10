package game.systems;

import game.GameEngine;
import game.entities.*;
import java.util.List;

/**
 * Handles all collision detection between entities.
 * Uses AABB collision detection.
 */
public class CollisionManager {
    private final GameEngine engine;
    
    public CollisionManager(GameEngine engine) {
        this.engine = engine;
    }
    
    public void checkCollisions(Player player, List<Enemy> enemies, 
                                List<Projectile> enemyProjectiles) {

        // Player projectiles vs enemies
        for (Projectile bullet : player.getProjectiles()) {
            if (!bullet.isAlive()) continue;

            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && bullet.collidesWith(enemy)) {

                    enemy.takeDamage(
                        bullet.getDamage(),
                        bullet.isCritical()
                    );

                    // Play hit sound
                    if (bullet.isCritical()) {
                        engine.getSoundManager().playSound("hit_critical");
                        // trigger screen shake on critical
                        engine.triggerScreenShake(0.20, 8.0);
                    } else {
                        engine.getSoundManager().playSound("hit");
                    }

                    // spawn floating damage text
                    engine.spawnDamageText(
                        enemy.getCenterX(),
                        enemy.getCenterY() - 10, // slightly above center
                        (int) Math.round(bullet.getDamage()),
                        bullet.isCritical()
                    );

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
                engine.getSoundManager().playSound("player_damaged");
                bullet.kill();
            }
        }

        // Enemies vs player (body collision)
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && enemy.collidesWith(player)) {
                player.takeDamage(enemy.getDamage());
                engine.getSoundManager().playSound("player_damaged");
                enemy.kill(); // Enemy dies on contact
            }
        }
    }
}
