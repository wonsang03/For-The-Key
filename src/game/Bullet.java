package game;

import java.awt.*;

public class Bullet {
    private double x, y, dx, dy;
    private double speed, damage, distanceTraveled, range;
    private boolean active = true;

    public Bullet(double x, double y, double angle, double speed, double damage, double range) {
        this.x = x; this.y = y;
        this.speed = speed; this.damage = damage; this.range = range;
        this.dx = Math.cos(angle) * speed;
        this.dy = Math.sin(angle) * speed;
    }

    public void update() {
        x += dx; y += dy; distanceTraveled += speed;
        if (distanceTraveled >= range) active = false;
    }

    public void draw(Graphics2D g2) {
        if (!active) return;
        g2.setColor(Color.YELLOW);
        g2.fillOval((int)x, (int)y, 10, 10);
    }

    public void deactivate() { active = false; }
    public boolean isActive() { return active; }
    public double getX() { return x; }
    public double getY() { return y; }
}
