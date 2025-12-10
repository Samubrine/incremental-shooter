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

        // Bullet vs Bullet collisions (player bullets vs enemy bullets)
        checkBulletCollisions(player.getProjectiles(), enemyProjectiles);

        // Player projectiles vs enemies
        for (Projectile bullet : player.getProjectiles()) {
            if (!bullet.isAlive()) continue;

            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && bullet.collidesWith(enemy)) {

                    enemy.takeDamage(
                        bullet.getDamage(),
                        bullet.isCritical()
                    );

                    // Play hit sound using OpenAL (ultra-low latency)
                    if (bullet.isCritical()) {
                        engine.getOpenALSoundEngine().playSound("hit_critical");
                        // trigger screen shake on critical
                        engine.triggerScreenShake(0.20, 8.0);
                    } else {
                        engine.getOpenALSoundEngine().playSound("hit");
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
                engine.getOpenALSoundEngine().playSound("player_damaged");
                bullet.kill();
            }
        }

        // Enemies vs player (body collision)
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && enemy.collidesWith(player)) {
                player.takeDamage(enemy.getDamage());
                engine.getOpenALSoundEngine().playSound("player_damaged");
                enemy.kill(); // Enemy dies on contact
            }
        }
    }

    /**
     * Check bullet vs bullet collisions.
     * Lower HP bullet is destroyed, higher HP bullet loses health equal to lower bullet's HP.
     */
    private void checkBulletCollisions(List<Projectile> playerBullets, List<Projectile> enemyBullets) {
        for (Projectile playerBullet : playerBullets) {
            if (!playerBullet.isAlive()) continue;

            for (Projectile enemyBullet : enemyBullets) {
                if (!enemyBullet.isAlive()) continue;

                if (playerBullet.collidesWith(enemyBullet)) {
                    double playerHP = playerBullet.getHealth();
                    double enemyHP = enemyBullet.getHealth();

                    if (playerHP > enemyHP) {
                        // Player bullet wins
                        playerBullet.takeDamage(enemyHP);
                        enemyBullet.kill();
                    } else if (enemyHP > playerHP) {
                        // Enemy bullet wins
                        enemyBullet.takeDamage(playerHP);
                        playerBullet.kill();
                    } else {
                        // Equal HP - both destroyed
                        playerBullet.kill();
                        enemyBullet.kill();
                    }
                    
                    // Only one collision per bullet per frame
                    if (!playerBullet.isAlive()) break;
                }
            }
        }
    }
}
