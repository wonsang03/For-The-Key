package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import main.GamePanel;

public class UIRenderer {

    GamePanel gp;
    Font basicFont;

    public UIRenderer(GamePanel gp) {
        this.gp = gp;
        // 폰트 설정 (이름: Arial, 스타일: 굵게, 크기: 40)
        basicFont = new Font("Arial", Font.BOLD, 40);
    }

    public void draw(Graphics2D g2) {
        // 폰트 적용 및 글자색 하얀색
        g2.setFont(basicFont);
        g2.setColor(Color.WHITE);

        // 테스트용 글씨 화면에 출력 (좌표 x:50, y:50)
        g2.drawString("For The Key", 50, 50);
    }
}