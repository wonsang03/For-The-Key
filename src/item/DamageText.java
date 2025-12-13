package item;

import java.awt.*;

public class DamageText {
    private double x, y;
    private String text;
    private Color color;
    private long startTime;
    private int duration = 800;
    private double riseSpeed = 0.7;

    // [김선욱님 코드] 데미지 텍스트 생성자
    public DamageText(double x, double y, String text, Color color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.startTime = System.currentTimeMillis();
    }

    // [김선욱님 코드] 데미지 텍스트 업데이트
    public void update() {
        y -= riseSpeed;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - startTime > duration;
    }

    // [김선욱님 코드] 데미지 텍스트 그리기
    public void draw(Graphics2D g2) {
        long elapsed = System.currentTimeMillis() - startTime;
        float alpha = 1.0f - (float)elapsed / duration;
        if (alpha < 0) alpha = 0;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setColor(color);
        g2.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        g2.drawString(text, (int)x, (int)y);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}
