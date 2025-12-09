package game.systems;

import game.entities.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages wave progression and enemy spawning.
 * Handles 15 waves with scaling difficulty and boss waves every 5 waves.
 */
public class WaveManager {
    private int currentWave;
    private int difficulty;
    private List<Enemy> enemies;
    private List<Projectile> enemyProjectiles;
    
    private int enemiesPerWave;
    private int enemiesSpawned;
    private double spawnTimer;
    private double spawnInterval;
    private boolean waveActive;
    
    public WaveManager(int difficulty) {
        this.difficulty = difficulty;
        this.currentWave = 0;
        this.enemies = new ArrayList<>();
        this.enemyProjectiles = new ArrayList<>();
        startNextWave();
    }
    
    public void startNextWave() {
        currentWave++;
        waveActive = true;
        enemiesSpawned = 0;
        spawnTimer = 0;
        
        // Calculate enemies for this wave
        boolean isBossWave = (currentWave % 5 == 0);
        
        if (isBossWave) {
            enemiesPerWave = 1; // Just boss
            spawnInterval = 0.5;
        } else {
            // More enemies each wave, scaled by difficulty
            enemiesPerWave = 5 + (currentWave * 2) + (difficulty * 3);
            spawnInterval = 1.5 / (1 + difficulty * 0.2); // Faster spawns on higher difficulty
        }
    }
    
    public void update(double deltaTime, Player player) {
        if (!waveActive) return;
        
        // Spawn enemies
        spawnTimer += deltaTime;
        if (enemiesSpawned < enemiesPerWave && spawnTimer >= spawnInterval) {
            spawnEnemy();
            spawnTimer = 0;
        }
        
        // Update all enemies
        enemies.removeIf(e -> !e.isAlive());
        for (Enemy enemy : enemies) {
            enemy.updateMovement(deltaTime, player);
            enemy.update(deltaTime);
            
            // Try to shoot
            Projectile proj = enemy.tryShoot(player);
            if (proj != null) {
                enemyProjectiles.add(proj);
                
                // Boss shoots in pattern
                if (enemy instanceof BossEnemy) {
                    addBossProjectilePattern(enemy);
                }
            }
        }
        
        // Update enemy projectiles
        enemyProjectiles.removeIf(p -> !p.isAlive());
        for (Projectile p : enemyProjectiles) {
            p.update(deltaTime);
        }
        
        // Check if wave complete
        if (enemiesSpawned >= enemiesPerWave && enemies.isEmpty()) {
            waveActive = false;
        }
    }
    
    private void spawnEnemy() {
        double x = Math.random() * 750;
        double y = -30;
        
        boolean isBossWave = (currentWave % 5 == 0);
        
        if (isBossWave) {
            enemies.add(new BossEnemy(x, y, currentWave));
        } else if (currentWave >= 7 && Math.random() < 0.3) {
            // 30% chance of mage after wave 7
            enemies.add(new MageEnemy(x, y, currentWave));
        } else {
            enemies.add(new BasicEnemy(x, y, currentWave));
        }
        
        enemiesSpawned++;
    }
    
    private void addBossProjectilePattern(Enemy boss) {
        // Boss shoots in 3 directions
        for (int i = -1; i <= 1; i++) {
            double angle = Math.toRadians(90 + (i * 20));
            double vx = Math.cos(angle) * 150;
            double vy = Math.sin(angle) * 150;
            enemyProjectiles.add(new Projectile(boss.getCenterX(), boss.getCenterY(), 
                                               vx, vy, boss.getDamage(), false));
        }
    }
    
    public boolean isWaveComplete() {
        return !waveActive;
    }
    
    public void handleEnemyKilled(Enemy enemy, Player player) {
        player.addCoins(enemy.getCoinValue());
    }
    
    // Getters
    public int getCurrentWave() { return currentWave; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Projectile> getEnemyProjectiles() { return enemyProjectiles; }
    public boolean isWaveActive() { return waveActive; }
}
