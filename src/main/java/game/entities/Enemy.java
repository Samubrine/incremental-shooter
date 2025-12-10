package game.entities;

import java.awt.*;

/**
 * Base enemy class - demonstrates inheritance and polymorphism.
 * Different enemy types extend this class.
 */
public abstract class Enemy extends Entity {

    protected double health;
    protected double maxHealth;
    protected double damage;
    protected int coinValue;
    protected Color color;

    // === CRITICAL HIT VISUAL EFFECT ===
    private boolean critFlash = false;
    private double critFlashTimer = 0;

    public Enemy(double x, double y, double width, double height,
                 double health, double damage, int coinValue) {
        super(x, y, width, height);
        this.maxHealth = health;
        this.health = health;
        this.damage = damage;
        this.coinValue = coinValue;
    }

    /**
     * Each enemy type has different movement behavior.
     */
    public abstract void updateMovement(double deltaTime, Player player);

    /**
     * Some enemies shoot projectiles.
     */
    public abstract Projectile tryShoot(Player player);

    @Override
    public void update(double deltaTime) {
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;

        // === UPDATE CRIT FLASH TIMER ===
        if (critFlash) {
            critFlashTimer -= deltaTime;
            if (critFlashTimer <= 0) {
                critFlash = false;
            }
        }

        if (y > 650 || x < -100 || x > 900) {
            alive = false;
        }
    }

    // === DAMAGE SYSTEM WITH CRITICAL EFFECT SUPPORT ===
    public void takeDamage(double damage, boolean isCrit) {
        health -= damage;

        if (isCrit) {
            critFlash = true;
            critFlashTimer = 0.12; // 120ms flash
        }

        if (health <= 0) {
            health = 0;
            alive = false;
        }
    }

    // === BACKWARD COMPATIBILITY (OLD CALL STILL WORKS) ===
    public void takeDamage(double damage) {
        takeDamage(damage, false);
    }
    
    /**
     * Apply difficulty multipliers to enemy HP, damage, and coin rewards.
     * Called when enemy is spawned to scale based on selected difficulty.
     */
    public void applyDifficultyMultiplier(double hpMultiplier, double coinMultiplier) {
        this.maxHealth *= hpMultiplier;
        this.health = this.maxHealth;
        this.damage *= hpMultiplier;
        this.coinValue = (int)(this.coinValue * coinMultiplier);
    }

    @Override
    public void render(Graphics2D g2d) {

        // === CRIT FLASH COLOR OVERRIDE ===
        if (critFlash) {
            g2d.setColor(Color.WHITE);
        } else {
            g2d.setColor(color);
        }

        // Render enemy as pentagon
        int[] xPoints = new int[5];
        int[] yPoints = new int[5];
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        double radius = width / 2;

        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(-90 + (360.0 / 5) * i);
            xPoints[i] = (int) (centerX + radius * Math.cos(angle));
            yPoints[i] = (int) (centerY + radius * Math.sin(angle));
        }
        g2d.fillPolygon(xPoints, yPoints, 5);

        // Health bar
        g2d.setColor(Color.RED);
        g2d.fillRect((int) x, (int) (y - 8), (int) width, 4);
        g2d.setColor(Color.GREEN);
        g2d.fillRect(
                (int) x,
                (int) (y - 8),
                (int) (width * (health / maxHealth)),
                4
        );
    }

    // Getters
    public double getDamage() { return damage; }
    public int getCoinValue() { return coinValue; }
}
