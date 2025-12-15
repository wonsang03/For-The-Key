package item;

import java.awt.*;
import java.awt.image.BufferedImage;
//import java.io.IOException;
import javax.imageio.ImageIO;

import common.Constants; // [민정 추가]
import main.GamePanel;  // [민정 추가]

//// [김선욱님 코드] 무기 시스템
//public class Weapon {
//    private WeaponType type;
//    private BufferedImage weaponImage;
//    private static BufferedImage weaponSheet;
//    private static BufferedImage itemSheet;
//    
//    // [민정 추가] 바닥에 떨어졌을 때 위치를 저장할 변수
//    public int worldX, worldY;
//    public Rectangle solidArea = new Rectangle(0, 0, 32, 32); // 히트박스
//
//    // [김선욱님 코드] 무기 생성자
//    public Weapon(WeaponType type) {
//        this.type = type;
//        loadSpriteSheets();
//        loadWeaponImage();
//    }
//
//    // [김선욱님 코드] 시트 2종류 로드 (weapons + items)
//    private void loadSpriteSheets() {
//        try {
//            if (weaponSheet == null)
//                weaponSheet = ImageIO.read(getClass().getResource("/item/weapons.png"));
//            if (itemSheet == null)
//                itemSheet = ImageIO.read(getClass().getResource("/item/items.png"));
//        } catch (IOException | IllegalArgumentException e) {
//            System.out.println("⚠️ 시트 로드 실패: " + e.getMessage());
//        }
//    }
//
//    // [김선욱님 코드] WeaponType별로 어느 시트 쓸지 + 좌표 지정
//    private void loadWeaponImage() {
//        int spriteX = 0, spriteY = 0, spriteW = 32, spriteH = 32;
//        BufferedImage sheet = null;
//
//        switch (type) {
//            case PISTOL:
//                sheet = weaponSheet;
//                spriteX = 34;
//                spriteY = 1278;
//                spriteW = 32;
//                spriteH = 32;
//                break;
//
//            case SHOTGUN:
//                sheet = weaponSheet;
//                spriteX = 74;
//                spriteY = 1278;
//                spriteW = 32;
//                spriteH = 32;
//                break;
//
//            case SNIPER:
//                sheet = weaponSheet;
//                spriteX = 114;
//                spriteY = 1278;
//                spriteW = 32;
//                spriteH = 32;
//                break;
//
//            case DAGGER:
//                sheet = weaponSheet;
//                spriteX = 154;
//                spriteY = 1278;
//                spriteW = 32;
//                spriteH = 32;
//                break;
//
//            case LONG_SWORD:
//                sheet = itemSheet;
//                spriteX = 380;
//                spriteY = 860;
//                spriteW = 32;
//                spriteH = 32;
//                break;
//
//            case KNIGHT_SWORD:
//                sheet = itemSheet;
//                spriteX = 420;
//                spriteY = 860;
//                spriteW = 32;
//                spriteH = 32;
//                break;
//        }
//
//        try {
//            if (sheet != null)
//                weaponImage = sheet.getSubimage(spriteX, spriteY, spriteW, spriteH);
//        } catch (Exception e) {
//            System.out.println("⚠️ 무기 이미지 로드 오류 (" + type.getName() + "): " + e.getMessage());
//            weaponImage = null;
//        }
//    }
//
//    public BufferedImage getWeaponImage() {
//        return weaponImage;
//    }
//
//    public String getName() {
//        return type.getName();
//    }
//
//    // [김선욱님 코드] 무기 그리기
//    public void draw(Graphics2D g2, double x, double y) {
//        if (weaponImage != null)
//            g2.drawImage(weaponImage, (int)x, (int)y, null);
//    }
//    
//    // [민정 추가] Player에서 타입 확인을 위해 필요 (반드시 추가!)
//    public WeaponType getType() {
//        return type;
//    }
//    
//    // [민정 수정] 바닥에 떨어진 무기 그리기
//    public void draw(Graphics2D g2, GamePanel gp) {
//        if (weaponImage != null) {
//            int screenX = worldX - (int)gp.cameraX;
//            int screenY = worldY - (int)gp.cameraY;
//
//            // [수정] gp.tileSize 대신 Constants.TILE_SIZE 사용
//            // gp.getWidth() 대신 Constants.WINDOW_WIDTH 사용 (더 확실함)
//            if (worldX + Constants.TILE_SIZE > gp.cameraX - Constants.TILE_SIZE && 
//                worldX - Constants.TILE_SIZE < gp.cameraX + Constants.WINDOW_WIDTH + Constants.TILE_SIZE &&
//                worldY + Constants.TILE_SIZE > gp.cameraY - Constants.TILE_SIZE && 
//                worldY - Constants.TILE_SIZE < gp.cameraY + Constants.WINDOW_HEIGHT + Constants.TILE_SIZE) {
//                
//                // 김선욱님 함수 재사용
//                draw(g2, (double)screenX, (double)screenY);
//            }
//        }
//    }
//}

public class Weapon {
    private WeaponType type;
    private BufferedImage weaponImage;
    
    // items.png는 여러 무기가 공유하므로 static으로 메모리 절약
    private static BufferedImage itemSheet;
    
    // 바닥에 떨어졌을 때 위치 및 히트박스
    public int worldX, worldY;
    public Rectangle solidArea = new Rectangle(0, 0, 32, 32); 

    public Weapon(WeaponType type) {
        this.type = type;
        loadSpriteSheets();
        loadWeaponImage();
    }

    // 1. 시트 로드 (검 종류를 위해 items.png만 로드)
    private void loadSpriteSheets() {
        try {
            if (itemSheet == null) {
                // items.png 로드 (경로: src/item/items.png)
                itemSheet = ImageIO.read(getClass().getResourceAsStream("/item/items.png"));
            }
        } catch (Exception e) {
            System.out.println("⚠️ items.png 로드 실패: " + e.getMessage());
        }
    }

    // 2. 무기 이미지 설정 (개별 파일 vs 시트 구분)
    private void loadWeaponImage() {
        String path = "";
        try {
            switch (type) {
                // ------------------------------------------
                // [그룹 A] 개별 파일로 존재하는 무기들
                // ------------------------------------------
                case PISTOL:
                    path = "/item/pistol.png";
                    weaponImage = ImageIO.read(getClass().getResourceAsStream(path));
                    break;

                case SHOTGUN:
                    path = "/item/shotgun.png";
                    weaponImage = ImageIO.read(getClass().getResourceAsStream(path));
                    break;

                case SNIPER:
                    path = "/item/sniper.png";
                    weaponImage = ImageIO.read(getClass().getResourceAsStream(path));
                    break;

                case DAGGER: // 단검도 개별 파일이라고 하셨습니다!
                    path = "/item/dagger.png";
                    weaponImage = ImageIO.read(getClass().getResourceAsStream(path));
                    break;

                // ------------------------------------------
                // [그룹 B] items.png 시트에서 잘라쓰는 무기들
                // ------------------------------------------
                case LONG_SWORD:
                    if (itemSheet != null) {
                        // 기존 좌표 유지 (x:380, y:860)
                        weaponImage = itemSheet.getSubimage(380, 860, 32, 32);
                    }
                    break;

                case KNIGHT_SWORD:
                    if (itemSheet != null) {
                        // 기존 좌표 유지 (x:420, y:860)
                        weaponImage = itemSheet.getSubimage(420, 860, 32, 32);
                    }
                    break;
            }
        } catch (Exception e) {
            System.out.println("❌ 무기 이미지 로드 오류 (" + type + "): " + path);
            e.printStackTrace();
            weaponImage = null; // 오류 시 null
        }
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