package item;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import common.Constants;
import main.GamePanel;

public class Weapon {
    private WeaponType type;
    private BufferedImage weaponImage;

    // 바닥에 떨어졌을 때 위치 및 히트박스
    public int worldX, worldY;
    public Rectangle solidArea = new Rectangle(0, 0, 32, 32);

    public Weapon(WeaponType type) {
        this.type = type;
        // WeaponType already preloads all images in static initializer
        // Just reference the cached image from WeaponType
        this.weaponImage = type.getWeaponImage();
    }

    // [seonuk 추가] 🎞️ 애니메이션 시작 (마우스 클릭 시 호출)
    // Delegates to WeaponType
    public void playCursorAnimation() {
        type.playCursorAnimation();
    }

    // [seonuk 추가] 🖱️ 마우스 커서로 그리기
    // Delegates to WeaponType
    public void drawCursor(Graphics2D g, int mouseX, int mouseY, boolean isAttacking) {
        type.drawCursor(g, mouseX, mouseY, isAttacking);
    }

    // [seonuk 추가] 애니메이션 상태 확인
    // Delegates to WeaponType
    public boolean isAnimating() {
        return type.isAnimating();
    }

    public BufferedImage getWeaponImage() {
        return weaponImage;
    }

    public String getName() {
        return type.getName();
    }
    
    public WeaponType getType() {
        return type;
    }

    // 기본 그리기 (좌표 직접 지정)
    public void draw(Graphics2D g2, double x, double y) {
        if (weaponImage != null)
            g2.drawImage(weaponImage, (int)x, (int)y, null);
    }
    
    // 바닥에 떨어진 무기 그리기 (카메라 좌표 적용)
    public void draw(Graphics2D g2, GamePanel gp) {
        if (weaponImage != null) {
            int screenX = worldX - (int)gp.cameraX;
            int screenY = worldY - (int)gp.cameraY;

            // 화면 안에 있을 때만 그리기 (성능 최적화)
            if (worldX + Constants.TILE_SIZE > gp.cameraX - Constants.TILE_SIZE && 
                worldX - Constants.TILE_SIZE < gp.cameraX + Constants.WINDOW_WIDTH + Constants.TILE_SIZE &&
                worldY + Constants.TILE_SIZE > gp.cameraY - Constants.TILE_SIZE && 
                worldY - Constants.TILE_SIZE < gp.cameraY + Constants.WINDOW_HEIGHT + Constants.TILE_SIZE) {
                
                draw(g2, (double)screenX, (double)screenY);
            }
        }
    }
}
