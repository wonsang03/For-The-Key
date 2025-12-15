package item;

import java.awt.*;

public class HitSpark {
    private double x, y;           // 중심 좌표 (타격 지점)
    private long startTime;        // 시작 시각
    private long duration = 150;   // 0.15초 동안 표시
    private boolean active = true;

    public HitSpark(double x, double y) {
        this.x = x;
        this.y = y;
        this.startTime = System.currentTimeMillis();
    }

    public boolean isActive() {
        if (!active) return false;
        if (System.currentTimeMillis() - startTime > duration)
            active = false;
        return active;
    }

    public void draw(Graphics2D g, double cameraX, double cameraY) {
        if (!isActive()) return;

        double sx = x - cameraX;
        double sy = y - cameraY;

        // 💥 간단한 빛 파편 이펙트
        g.setColor(new Color(255, 240, 120, 180));
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60 + Math.random() * 20 - 10);
            int len = 6 + (int)(Math.random() * 6);
            int x2 = (int)(sx + Math.cos(angle) * len);
            int y2 = (int)(sy + Math.sin(angle) * len);
            g.drawLine((int)sx, (int)sy, x2, y2);
        }
    }
}
