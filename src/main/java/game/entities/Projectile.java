package game.entities;

import java.awt.*;

/**
 * Projectile entity with health system.
 * Bullets can collide with each other - lower HP bullet is destroyed.
 * Damage is equal to bullet's health points.
 */
public class Projectile extends Entity {

    private double health;      // Bullet HP (also determines damage)
    private boolean playerOwned;
    private boolean critical;
    private Color color;

    public Projectile(double x, double y, double vx, double vy,
                      double damage, boolean playerOwned, boolean critical) {
        super(x, y, playerOwned ? 8 : 6, playerOwned ? 8 : 6);
        this.velocityX = vx;
        this.velocityY = vy;
        this.health = damage;   // HP = damage
        this.playerOwned = playerOwned;
        this.critical = critical;

        // Color based on crit and owner
        if (playerOwned) {
            this.color = critical ? Color.ORANGE : Color.YELLOW;
        } else {
            this.color = Color.RED;
        }
    }

    // Backward compatibility
    public Projectile(double x, double y, double vx, double vy,
                      double damage, boolean playerOwned) {
        this(x, y, vx, vy, damage, playerOwned, false);
    }

    @Override
    public void update(double deltaTime) {
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;

        if (x < -20 || x > 820 || y < -20 || y > 620) {
            alive = false;
        }
    }

    @Override
    public void render(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillOval((int) x, (int) y, (int) width, (int) height);

        // Glow effect
        g2d.setColor(new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                100
        ));
        g2d.fillOval(
                (int) (x - 2),
                (int) (y - 2),
                (int) (width + 4),
                (int) (height + 4)
        );
    }

    /**
     * Reduce bullet health. If HP <= 0, bullet is destroyed.
     */
    public void takeDamage(double amount) {
        health -= amount;
        if (health <= 0) {
            alive = false;
        }
    }

    public double getDamage() { return health; }  // Damage = current HP
    public double getHealth() { return health; }
    public boolean isPlayerOwned() { return playerOwned; }
    public boolean isCritical() { return critical; }
}
