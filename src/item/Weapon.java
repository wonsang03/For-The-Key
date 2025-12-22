package item;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import common.Constants;
import main.GamePanel;

public class Weapon {
    private WeaponType type;
    private BufferedImage weaponImage;
    
    // items.png는 여러 무기가 공유하므로 static으로 메모리 절약
    private static BufferedImage itemSheet;
    
    // 바닥에 떨어졌을 때 위치 및 히트박스
    public int worldX, worldY;
    public Rectangle solidArea = new Rectangle(0, 0, 32, 32);
    
    // [seonuk 추가] 커서 애니메이션 관련 필드
    private ArrayList<BufferedImage> cursorFrames = new ArrayList<>();
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private boolean animating = false;
    private int frameDelay = 60; // 프레임 간격(ms)
    private int totalFrames = 1;

    public Weapon(WeaponType type) {
        this.type = type;
        loadSpriteSheets();
        loadWeaponImage();
        loadCursorFrames(); // ✅ 커서 애니메이션용 프레임 로드
    }

    // 1. 시트 로드 (검 종류를 위해 items.png만 로드)
    private void loadSpriteSheets() {
        try {
            if (itemSheet == null) {
                // items.png 로드 (경로: res/item/items.png)
                File file = new File("res/item/items.png");
                if (file.exists()) {
                    itemSheet = ImageIO.read(file);
                }
            }
        } catch (Exception e) {
            // 이미지 로드 실패 시 조용히 처리
        }
    }

    // 2. 무기 이미지 설정 (개별 파일 vs 시트 구분)
    private void loadWeaponImage() {
        String path = "";
        try {
            switch (type) {
                case PISTOL:
                    path = "res/item/pistol1.png";
                    File pistolFile = new File(path);
                    if (pistolFile.exists()) {
                        weaponImage = ImageIO.read(pistolFile);
                    }
                    break;

                case SHOTGUN:
                    path = "res/item/shotgun1.png";
                    File shotgunFile = new File(path);
                    if (shotgunFile.exists()) {
                        weaponImage = ImageIO.read(shotgunFile);
                    }
                    break;

                case SNIPER:
                    path = "res/item/sniper1.png";
                    File sniperFile = new File(path);
                    if (sniperFile.exists()) {
                        weaponImage = ImageIO.read(sniperFile);
                    }
                    break;

                case DAGGER:
                    path = "res/item/dagger1.png";
                    File daggerFile = new File(path);
                    if (daggerFile.exists()) {
                        weaponImage = ImageIO.read(daggerFile);
                    }
                    break;

                // ------------------------------------------
                // [그룹 B] items.png 시트에서 잘라쓰는 무기들
                // ------------------------------------------
                case LONG_SWORD:
                    if (itemSheet != null) {
                        weaponImage = itemSheet.getSubimage(702, 1507, 32, 32);
                    }
                    break;

                case KNIGHT_SWORD:
                    if (itemSheet != null) {
                        weaponImage = itemSheet.getSubimage(1025, 1472, 32, 32);
                    }
                    break;
            }
        } catch (Exception e) {
            weaponImage = null; // 오류 시 null
        }
    }

    // [seonuk 추가] 🔹 커서용 프레임 로드 (무기별 개수 자동)
    private void loadCursorFrames() {
        int frameCount = switch (type) {
            case PISTOL -> 6;
            case SHOTGUN -> 3;
            case SNIPER -> 4;
            case DAGGER -> 2;
            default -> 1;
        };
        totalFrames = frameCount;

        for (int i = 1; i <= frameCount; i++) {
            String path = "res/item/" + type.name().toLowerCase() + i + ".png";
            File file = new File(path);
            if (file.exists()) {
                try {
                    cursorFrames.add(ImageIO.read(file));
                } catch (IOException e) {
                    // 커서 프레임 로드 실패 시 조용히 처리
                }
            }
        }

        if (cursorFrames.isEmpty() && weaponImage != null)
            cursorFrames.add(weaponImage); // 프레임 없으면 기본 이미지 사용
    }

    // [seonuk 추가] 🎞️ 애니메이션 시작 (마우스 클릭 시 호출)
    public void playCursorAnimation() {
        if (cursorFrames.size() <= 1) return;
        animating = true;
        currentFrame = 0;
        lastFrameTime = System.currentTimeMillis();
    }

    // [seonuk 추가] 🔁 프레임 업데이트
    private void updateAnimation() {
        if (!animating || cursorFrames.isEmpty()) return;

        long now = System.currentTimeMillis();
        if (now - lastFrameTime > frameDelay) {
            currentFrame++;
            lastFrameTime = now;
            if (currentFrame >= totalFrames) {
                currentFrame = 0;
                animating = false; // 한 번 돌고 정지
            }
        }
    }

    // [seonuk 추가] 🖱️ 마우스 커서로 그리기
    public void drawCursor(Graphics2D g, int mouseX, int mouseY, boolean isAttacking) {
        updateAnimation();
        BufferedImage img = cursorFrames.isEmpty() ? weaponImage : cursorFrames.get(currentFrame);
        if (img == null) return;

        int size = isAttacking ? 96 : 72;
        int drawX = mouseX - size / 2;
        int drawY = mouseY - size / 2;

        g.drawImage(img, drawX, drawY, size, size, null);
    }

    // [seonuk 추가] 애니메이션 상태 확인
    public boolean isAnimating() {
        return animating;
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
