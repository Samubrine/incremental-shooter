package game.entities;

import game.systems.InputManager;
import game.systems.UpgradeManager;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Player entity with WASD movement, upgradeable shooting, and critical hit.
 */
public class Player extends Entity {

    // === CRITICAL HIT SYSTEM ===
    private double critChance = 0.1;     // 10% chance
    private double critMultiplier = 2.0; // 2x damage
    // === DASH SYSTEM ===
private double dashCooldown = 1.5;   // detik
private double dashDuration = 0.15;  // detik
private double dashSpeedMultiplier = 4.0;

private double dashCooldownTimer = 0;
private double dashTimer = 0;

private boolean isDashing = false;
private boolean invincible = false;

private double dashDirX = 0;
private double dashDirY = 0;

    private final Random random = new Random();

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
        fireTimer += deltaTime;

        projectiles.removeIf(p -> !p.isAlive());
        for (Projectile p : projectiles) {
            p.update(deltaTime);
        }
    }

    public void update(double deltaTime, InputManager input) {
        handleDash(deltaTime, input);
        handleMovement(deltaTime, input);
        tryShoot();
        update(deltaTime);
    }

    private void handleDash(double deltaTime, InputManager input) {

    // Cooldown timer
    if (dashCooldownTimer > 0)
        dashCooldownTimer -= deltaTime;

    // Dash sedang berlangsung
    if (isDashing) {
        dashTimer -= deltaTime;

        x += dashDirX * speed * dashSpeedMultiplier * deltaTime;
        y += dashDirY * speed * dashSpeedMultiplier * deltaTime;

        if (dashTimer <= 0) {
            isDashing = false;
            invincible = false;
        }
        return; // jangan pakai movement normal saat dash
    }

    // Trigger dash (SHIFT)
    if (input.isDashPressed() && dashCooldownTimer <= 0) {

        double dx = 0, dy = 0;

        if (input.isKeyPressed('W')) dy -= 1;
        if (input.isKeyPressed('S')) dy += 1;
        if (input.isKeyPressed('A')) dx -= 1;
        if (input.isKeyPressed('D')) dx += 1;

        // Kalau tidak tekan arah, dash ke atas
        if (dx == 0 && dy == 0) dy = -1;

        // Normalisasi arah
        double length = Math.sqrt(dx * dx + dy * dy);
        dashDirX = dx / length;
        dashDirY = dy / length;

        isDashing = true;
        invincible = true;
        dashTimer = dashDuration;
        dashCooldownTimer = dashCooldown;
    }
}


    public void handleMovement(double deltaTime, InputManager input) {
        double dx = 0, dy = 0;

        if (input.isKeyPressed('W')) dy -= 1;
        if (input.isKeyPressed('S')) dy += 1;
        if (input.isKeyPressed('A')) dx -= 1;
        if (input.isKeyPressed('D')) dx += 1;

        if (dx != 0 && dy != 0) {
            dx *= 0.707;
            dy *= 0.707;
        }

        x += dx * speed * deltaTime;
        y += dy * speed * deltaTime;

        x = Math.max(0, Math.min(770, x));
        y = Math.max(0, Math.min(570, y));
    }

    public void tryShoot() {
        double fireRate = BASE_FIRE_RATE / (1 + upgradeManager.getFireRateLevel() * 0.3);

        if (fireTimer >= fireRate) {
            shoot();
            fireTimer = 0;
        }
    }

    private void shoot() {
    int bulletCount = 1 + upgradeManager.getBulletCountLevel();
    double baseDamage = 10 + (upgradeManager.getDamageLevel() * 5);
    double bulletSpeed = 400 + (upgradeManager.getBulletSpeedLevel() * 50);

    // === CRITICAL HIT CHECK ===
    boolean isCrit = random.nextDouble() < critChance;
    double finalDamage = isCrit ? baseDamage * critMultiplier : baseDamage;

    if (bulletCount == 1) {
        projectiles.add(new Projectile(
                getCenterX(), getCenterY(),
                0, -bulletSpeed,
                finalDamage,
                true,
                isCrit          // ✅ FIX PENTING
        ));
    } else {
        double angleSpread = 15 * (bulletCount - 1);
        for (int i = 0; i < bulletCount; i++) {
            double angle = Math.toRadians(
                    -90 + (-angleSpread / 2 + (angleSpread / (bulletCount - 1)) * i)
            );
            double vx = Math.cos(angle) * bulletSpeed;
            double vy = Math.sin(angle) * bulletSpeed;

            projectiles.add(new Projectile(
                    getCenterX(), getCenterY(),
                    vx, vy,
                    finalDamage,
                    true,
                    isCrit      // ✅ FIX PENTING
            ));
        }
    }
}


    public void takeDamage(double damage) {
    if (invincible) return; // ✅ kebal saat dash

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
        if (isDashing)
    g2d.setColor(Color.WHITE);
else
    g2d.setColor(Color.CYAN);

        g2d.fillOval((int) x, (int) y, (int) width, (int) height);

        g2d.setColor(Color.RED);
        g2d.fillRect((int) x, (int) (y - 10), (int) width, 5);
        g2d.setColor(Color.GREEN);
        g2d.fillRect(
                (int) x,
                (int) (y - 10),
                (int) (width * (health / maxHealth)),
                5
        );

        List<Projectile> projectilesCopy = new ArrayList<>(projectiles);
        for (Projectile p : projectilesCopy) {
            if (p.isAlive()) {
                p.render(g2d);
            }
        }
    }

    // === GETTERS ===
    public List<Projectile> getProjectiles() { return projectiles; }
    public double getHealth() { return health; }
    public double getMaxHealth() { return maxHealth; }
    public int getCoins() { return coins; }
    public boolean isDead() { return !alive; }
    public double getCritChance() { return critChance; }
    public double getCritMultiplier() { return critMultiplier; }

}
