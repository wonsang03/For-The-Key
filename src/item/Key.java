package item;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Key {
    private double x, y;
    private boolean picked = false;

    private static BufferedImage spriteSheet;
    private BufferedImage image;

    // [김선욱님 코드] 열쇠 생성자
    public Key(double x, double y) {
        this.x = x;
        this.y = y;
        loadSpriteSheet();
        setSpriteRegion();
    }

    private static void loadSpriteSheet() {
        if (spriteSheet != null) return;
        try {
            spriteSheet = ImageIO.read(Key.class.getResource("/item/items.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("⚠️ Key 시트 로드 실패: " + e.getMessage());
            spriteSheet = null;
        }
    }

    // [김선욱님 코드] 스프라이트 좌표 직접 지정
    private void setSpriteRegion() {
        if (spriteSheet == null) {
            image = null;
            return;
        }

        int spriteX = 420;
        int spriteY = 860;
        int spriteW = 32;
        int spriteH = 32;

        try {
            image = spriteSheet.getSubimage(spriteX, spriteY, spriteW, spriteH);
        } catch (Exception e) {
            System.out.println("⚠️ Key 이미지 잘라내기 실패: " + e.getMessage());
            image = null;
        }
    }

    // [김선욱님 코드] 열쇠 그리기
    public void draw(Graphics2D g2) {
        if (picked) return;

        if (image != null) {
            g2.drawImage(image, (int)x, (int)y, 32, 32, null);
        } else {
            g2.setColor(Color.YELLOW);
            g2.fillOval((int)x, (int)y, 20, 20);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, 32, 32);
    }

    public boolean isPicked() { return picked; }
    public void pickUp() { picked = true; }
    public double getX() { return x; }
    public double getY() { return y; }
}
