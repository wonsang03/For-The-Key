package main;

import java.awt.*;

public class Player {
    private double x, y;
    private double moveSpeed = 4.0;
    private double attackMultiplier = 1.0;
    private double attackSpeedBonus = 0.0;
    private int maxHP = 100;
    private int hp = 100;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void move(boolean up, boolean down, boolean left, boolean right) {
        if (up) y -= moveSpeed;
        if (down) y += moveSpeed;
        if (left) x -= moveSpeed;
        if (right) x += moveSpeed;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.CYAN);
        g2.fillRect((int)x, (int)y, 40, 40);
    }

    // === 스탯 관련 ===
    public void heal(int value) { hp = Math.min(maxHP, hp + value); }
    public void addAttackBonus(double v) { attackMultiplier += v; }
    public void addSpeedBonus(double v) { moveSpeed += v; }
    public void addAttackSpeedBonus(double v) { attackSpeedBonus += v; }
    public void addMaxHP(double v) { maxHP += v; hp += v; }

    // === Getter ===
    public double getX() { return x; }
    public double getY() { return y; }
    public int getHP() { return hp; }
    public int getMaxHP() { return maxHP; }
    public double getAttackMultiplier() { return attackMultiplier; }
    public double getMoveSpeed() { return moveSpeed; }
    public double getAttackSpeedBonus() { return attackSpeedBonus; }
    
    // === Setter ===
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}
