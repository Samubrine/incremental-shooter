package game.entities;

import game.systems.InputManager;
import game.systems.UpgradeManager;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Player entity with WASD movement and upgradeable shooting.
 */
public class Player extends Entity {
    private static final double BASE_SPEED = 200.0;
    private static final double BASE_FIRE_RATE = 0.5; // seconds between shots
    
    private UpgradeManager upgradeManager;
    private double health;
    private double maxHealth;
    private int coins;
    private List<Projectile> projectiles;
    
    private double fireTimer;
    private double speed;
    
    public Player(double x, double y, UpgradeManager upgradeManager) {
        super(x, y, 30, 30);
        this.upgradeManager = upgradeManager;
        this.projectiles = new ArrayList<>();
        reset();
    }
    
    public void reset() {
        this.maxHealth = 100 + (upgradeManager.getHealthLevel() * 20);
        this.health = maxHealth;
        // Don't reset coins - preserve between waves
        this.fireTimer = 0;
        this.speed = BASE_SPEED + (upgradeManager.getSpeedLevel() * 20);
        this.projectiles.clear();
        this.alive = true;
    }
    
    public void fullReset() {
        this.coins = 0;
        reset();
    }
    
    @Override
    public void update(double deltaTime) {
        // Update fire timer
        fireTimer += deltaTime;
        
        // Update projectiles
        projectiles.removeIf(p -> !p.isAlive());
        for (Projectile p : projectiles) {
            p.update(deltaTime);
        }
    }
    
    /**
     * Update with input for movement and shooting.
     */
    public void update(double deltaTime, InputManager input) {
        handleMovement(deltaTime, input);
        tryShoot();
        update(deltaTime);
    }
    
    /**
     * Handle player movement based on input.
     */
    public void handleMovement(double deltaTime, InputManager input) {
        double dx = 0, dy = 0;
        
        if (input.isKeyPressed('W')) dy -= 1;
        if (input.isKeyPressed('S')) dy += 1;
        if (input.isKeyPressed('A')) dx -= 1;
        if (input.isKeyPressed('D')) dx += 1;
        
        // Normalize diagonal movement
        if (dx != 0 && dy != 0) {
            dx *= 0.707;
            dy *= 0.707;
        }
        
        x += dx * speed * deltaTime;
        y += dy * speed * deltaTime;
        
        // Clamp to screen bounds
        x = Math.max(0, Math.min(770, x));
        y = Math.max(0, Math.min(570, y));
    }
    
    /**
     * Auto-fire mechanic - shoots if cooldown ready.
     */
    public void tryShoot() {
        double fireRate = BASE_FIRE_RATE / (1 + upgradeManager.getFireRateLevel() * 0.3);
        
        if (fireTimer >= fireRate) {
            shoot();
            fireTimer = 0;
        }
    }
    
    private void shoot() {
        int bulletCount = 1 + upgradeManager.getBulletCountLevel();
        double damage = 10 + (upgradeManager.getDamageLevel() * 5);
        double bulletSpeed = 400 + (upgradeManager.getBulletSpeedLevel() * 50);
        
        if (bulletCount == 1) {
            projectiles.add(new Projectile(getCenterX(), getCenterY(), 0, -bulletSpeed, damage, true));
        } else {
            // Spread bullets in a fan pattern
            double angleSpread = 15 * (bulletCount - 1);
            for (int i = 0; i < bulletCount; i++) {
                double angle = Math.toRadians(-90 + (-angleSpread/2 + (angleSpread/(bulletCount-1)) * i));
                double vx = Math.cos(angle) * bulletSpeed;
                double vy = Math.sin(angle) * bulletSpeed;
                projectiles.add(new Projectile(getCenterX(), getCenterY(), vx, vy, damage, true));
            }
        }
    }
    
    public void takeDamage(double damage) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            alive = false;
        }
    }
    
    public void addCoins(int amount) {
        coins += amount;
    }
    
    public boolean spendCoins(int amount) {
        if (coins >= amount) {
            coins -= amount;
            return true;
        }
        return false;
    }
    
    @Override
    public void render(Graphics2D g2d) {
        // Render player as blue circle
        g2d.setColor(Color.CYAN);
        g2d.fillOval((int)x, (int)y, (int)width, (int)height);
        
        // Render health bar above player
        g2d.setColor(Color.RED);
        g2d.fillRect((int)x, (int)(y - 10), (int)width, 5);
        g2d.setColor(Color.GREEN);
        g2d.fillRect((int)x, (int)(y - 10), (int)(width * (health / maxHealth)), 5);
        
        // Render projectiles - use copy to avoid ConcurrentModificationException
        List<Projectile> projectilesCopy = new ArrayList<>(projectiles);
        for (Projectile p : projectilesCopy) {
            if (p.isAlive()) {
                p.render(g2d);
            }
        }
    }
    
    // Getters
    public List<Projectile> getProjectiles() { return projectiles; }
    public double getHealth() { return health; }
    public double getMaxHealth() { return maxHealth; }
    public int getCoins() { return coins; }
    public boolean isDead() { return !alive; }
}
