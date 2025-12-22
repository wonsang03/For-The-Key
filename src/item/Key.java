package item;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Key {
    private double x, y;
    private boolean picked = false;

    private static BufferedImage spriteSheet;
    private BufferedImage image;

    // Static 초기화로 한 번만 로드
    static {
        try {
            java.io.InputStream is = Key.class.getResourceAsStream("/item/item/items.png");
            if (is != null) spriteSheet = ImageIO.read(is);
        } catch (Exception e) {
            spriteSheet = null;
        }
    }

    // [김선욱님 코드] 열쇠 생성자
    public Key(double x, double y) {
        this.x = x;
        this.y = y;
        setSpriteRegion();
    }

    // [김선욱님 코드] 스프라이트 좌표 직접 지정
    private void setSpriteRegion() {
        if (spriteSheet == null) {
            image = null;
            return;
        }

        int spriteX = 1761;
        int spriteY = 1288;
        int spriteW = 32;
        int spriteH = 25;

        try {
            image = spriteSheet.getSubimage(spriteX, spriteY, spriteW, spriteH);
        } catch (Exception e) {
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
