package item;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Bullet {
    private double x, y, dx, dy;
    private double speed, damage, distanceTraveled, range;
    private boolean active = true;

    private BufferedImage image;
    private WeaponType weaponType;
    private static BufferedImage spriteSheet;
    private double angle;

    private static final int SPRITE_W = 32;
    private static final int SPRITE_H = 32;

    // [김선욱님 코드] 총알 생성자
    public Bullet(double x, double y, double angle, double speed, double damage, double range, WeaponType weaponType) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.damage = damage;
        this.range = range;
        this.weaponType = weaponType;
        this.angle = angle;

        this.dx = Math.cos(angle) * speed;
        this.dy = Math.sin(angle) * speed;

        loadSpriteSheet();
        setSpriteRegion(weaponType);
    }

    /** 🔹 스프라이트 시트 로드 */
    private static void loadSpriteSheet() {
        if (spriteSheet != null) return;
        try {
            java.io.InputStream is = Bullet.class.getResourceAsStream("/res/item/items.png");
            if (is != null) spriteSheet = ImageIO.read(is);
            else spriteSheet = null;
        } catch (IOException | IllegalArgumentException e) {
            spriteSheet = null;
        }
    }

    /** 🔹 무기 타입별 스프라이트 좌표 지정 */
    private void setSpriteRegion(WeaponType type) {
        int spriteX = 0;
        int spriteY = 0;
        int spriteW = 32;
        int spriteH = 32;

        switch (type) {
            case PISTOL:
                spriteX = 1921;
                spriteY = 805;
                spriteW = 25;
                spriteH = 25;
                break;

            case SHOTGUN:
                spriteX = 522;
                spriteY = 839;
                spriteW = 25;
                spriteH = 25;
                break;

            case SNIPER:
                spriteX = 34;
                spriteY = 808;
                spriteW = 25;
                spriteH = 25;
                break;

            default:
                // ⚠️ 근접 무기(DAGGER, LONG_SWORD, KNIGHT_SWORD)는 탄막 비활성화
                image = null;
                return;
        }

        if (spriteSheet != null) {
            try {
                image = spriteSheet.getSubimage(spriteX, spriteY, spriteW, spriteH);
            } catch (Exception e) {
                image = null;
            }
        } else {
            image = null;
        }
    }

    /** 🔹 탄환 이동 업데이트 */
    public void update() {
        x += dx;
        y += dy;
        distanceTraveled += speed;
        if (distanceTraveled >= range) active = false;
    }

    /** 🔹 탄환 그리기 (방향 회전) */
    public void draw(Graphics2D g2) {
        if (!active) return;

        if (image != null) {
            AffineTransform old = g2.getTransform();

            // 중심 기준 회전
            AffineTransform transform = new AffineTransform();
            transform.translate(x + SPRITE_W / 2.0, y + SPRITE_H / 2.0);
            transform.rotate(angle);
            transform.translate(-SPRITE_W / 2.0, -SPRITE_H / 2.0);

            g2.drawImage(image, transform, null);
            g2.setTransform(old);
        } else {
            g2.setColor(Color.YELLOW);
            g2.fillOval((int)x, (int)y, 10, 10);
        }
    }

    public void deactivate() { active = false; }
    public boolean isActive() { return active; }
    public double getX() { return x; }
    public double getY() { return y; }
}
