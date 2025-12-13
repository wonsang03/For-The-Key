package item;

import java.awt.*;
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

    // 기본 크기
    private static final int SPRITE_W = 32;
    private static final int SPRITE_H = 32;

    public Bullet(double x, double y, double angle, double speed, double damage, double range, WeaponType weaponType) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.damage = damage;
        this.range = range;
        this.weaponType = weaponType;

        this.dx = Math.cos(angle) * speed;
        this.dy = Math.sin(angle) * speed;

        loadSpriteSheet();
        loadSpriteRegion(weaponType);
    }

    /** 🔹 스프라이트 시트 한 번만 로드 */
    private static void loadSpriteSheet() {
        if (spriteSheet != null) return;
        try {
            spriteSheet = ImageIO.read(new File("res/item/items.png"));
            System.out.println("✅ bullets 시트 로드 완료");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("⚠️ bullets.png 로드 실패: " + e.getMessage());
            spriteSheet = null;
        }
    }

    /** 🔹 WeaponType별 스프라이트 좌표 지정 */
    private void loadSpriteRegion(WeaponType type) {
        int spriteX = 0;
        int spriteY = 0;
        int spriteW = 32;
        int spriteH = 32;

        switch (type) {
            case PISTOL:
                spriteX = 1921;
                spriteY = 805;
                spriteW = 32;
                spriteH = 32;
                break;

            case SHOTGUN:
                spriteX = 1961;
                spriteY = 805;
                spriteW = 32;
                spriteH = 32;
                break;

            case SNIPER:
                spriteX = 2001;
                spriteY = 805;
                spriteW = 32;
                spriteH = 32;
                break;

            case DAGGER:
                spriteX = 2041;
                spriteY = 805;
                spriteW = 32;
                spriteH = 32;
                break;

            case LONG_SWORD:
                spriteX = 2081;
                spriteY = 805;
                spriteW = 32;
                spriteH = 32;
                break;

            case KNIGHT_SWORD:
                spriteX = 2121;
                spriteY = 805;
                spriteW = 32;
                spriteH = 32;
                break;

            default:
                spriteX = 0;
                spriteY = 0;
                spriteW = 32;
                spriteH = 32;
        }

        if (spriteSheet != null) {
            try {
                image = spriteSheet.getSubimage(spriteX, spriteY, spriteW, spriteH);
            } catch (Exception e) {
                System.out.println("⚠️ 탄환 이미지 잘라내기 실패 (" + type + "): " + e.getMessage());
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

    /** 🔹 탄환 그리기 */
    public void draw(Graphics2D g2) {
        if (!active) return;

        if (image != null) {
            g2.drawImage(image, (int)x, (int)y, SPRITE_W, SPRITE_H, null);
        } else {
            g2.setColor(Color.YELLOW);
            g2.fillOval((int)x, (int)y, 10, 10);
        }
    }

    // ===== Getter / Setter =====
    public void deactivate() { active = false; }
    public boolean isActive() { return active; }
    public double getX() { return x; }
    public double getY() { return y; }
}
