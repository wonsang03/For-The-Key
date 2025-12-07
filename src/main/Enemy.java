package main;

import java.awt.*;

public class Enemy {
    private double x, y;
    private int width = 40, height = 40;
    private int hp = 100;
    private int maxHP = 100;
    private boolean alive = true;

    public Enemy(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        // 적 AI가 추가된다면 여기에 이동 로직 작성
    }

    public void draw(Graphics2D g2) {
        if (!alive) return;

        // === 적 본체 ===
        g2.setColor(Color.RED);
        g2.fillRect((int)x, (int)y, width, height);

        // === HP 바 배경 ===
        int barWidth = 50;
        int barHeight = 6;
        int barX = (int)(x + width / 2 - barWidth / 2);
        int barY = (int)(y - 10);

        double hpRatio = (double) hp / maxHP;

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(barX, barY, barWidth, barHeight);

        g2.setColor(Color.GREEN);
        g2.fillRect(barX, barY, (int)(barWidth * hpRatio), barHeight);

        g2.setColor(Color.WHITE);
        g2.drawRect(barX, barY, barWidth, barHeight);

        // === HP 숫자 표시 (선택사항) ===
        g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 10));
        g2.setColor(Color.WHITE);
        g2.drawString(hp + "/" + maxHP, barX + 10, barY - 2);
    }

    public void takeDamage(double dmg) {
        hp -= dmg;
        if (hp <= 0) {
            alive = false;
        }
    }

    public boolean isAlive() { return alive; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
