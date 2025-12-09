package game.entities;

import java.awt.*;

public class DamageText {

    private double x, y;
    private int value;
    private Color color;
    private double lifetime = 1.0; // detik
    private double velocityY = -40;
    private boolean alive = true;

    public DamageText(double x, double y, int value, boolean isCrit) {
        this.x = x;
        this.y = y;
        this.value = value;
        this.color = isCrit ? Color.YELLOW : Color.RED;
    }

    public void update(double deltaTime) {
        y += velocityY * deltaTime;
        lifetime -= deltaTime;
        if (lifetime <= 0) alive = false;
    }

    public void render(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(String.valueOf(value), (int) x, (int) y);
    }

    public boolean isAlive() {
        return alive;
    }
}
