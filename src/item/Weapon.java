package item;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

// [김선욱님 코드] 무기 시스템
public class Weapon {
    private WeaponType type;
    private BufferedImage weaponImage;
    private static BufferedImage weaponSheet;
    private static BufferedImage itemSheet;

    // [김선욱님 코드] 무기 생성자
    public Weapon(WeaponType type) {
        this.type = type;
        loadSpriteSheets();
        loadWeaponImage();
    }

    // [김선욱님 코드] 시트 2종류 로드 (weapons + items)
    private void loadSpriteSheets() {
        try {
            if (weaponSheet == null)
                weaponSheet = ImageIO.read(getClass().getResource("/item/weapons.png"));
            if (itemSheet == null)
                itemSheet = ImageIO.read(getClass().getResource("/item/items.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("⚠️ 시트 로드 실패: " + e.getMessage());
        }
    }

    // [김선욱님 코드] WeaponType별로 어느 시트 쓸지 + 좌표 지정
    private void loadWeaponImage() {
        int spriteX = 0, spriteY = 0, spriteW = 32, spriteH = 32;
        BufferedImage sheet = null;

        switch (type) {
            case PISTOL:
                sheet = weaponSheet;
                spriteX = 34;
                spriteY = 1278;
                spriteW = 32;
                spriteH = 32;
                break;

            case SHOTGUN:
                sheet = weaponSheet;
                spriteX = 74;
                spriteY = 1278;
                spriteW = 32;
                spriteH = 32;
                break;

            case SNIPER:
                sheet = weaponSheet;
                spriteX = 114;
                spriteY = 1278;
                spriteW = 32;
                spriteH = 32;
                break;

            case DAGGER:
                sheet = weaponSheet;
                spriteX = 154;
                spriteY = 1278;
                spriteW = 32;
                spriteH = 32;
                break;

            case LONG_SWORD:
                sheet = itemSheet;
                spriteX = 380;
                spriteY = 860;
                spriteW = 32;
                spriteH = 32;
                break;

            case KNIGHT_SWORD:
                sheet = itemSheet;
                spriteX = 420;
                spriteY = 860;
                spriteW = 32;
                spriteH = 32;
                break;
        }

        try {
            if (sheet != null)
                weaponImage = sheet.getSubimage(spriteX, spriteY, spriteW, spriteH);
        } catch (Exception e) {
            System.out.println("⚠️ 무기 이미지 로드 오류 (" + type.getName() + "): " + e.getMessage());
            weaponImage = null;
        }
    }

    public BufferedImage getWeaponImage() {
        return weaponImage;
    }

    public String getName() {
        return type.getName();
    }

    // [김선욱님 코드] 무기 그리기
    public void draw(Graphics2D g2, double x, double y) {
        if (weaponImage != null)
            g2.drawImage(weaponImage, (int)x, (int)y, null);
    }
}
