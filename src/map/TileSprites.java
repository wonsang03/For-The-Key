package map;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

// [서충만님 코드] 타일 스프라이트 관리 클래스: 스테이지별로 다른 타일셋을 로드
public class TileSprites {
    private static BufferedImage spriteSheet;
    private static BufferedImage keyRoomSheet;
    private static final int TILE_SIZE = 204;
    private static final int TILES_PER_ROW = 10;
    private static int currentStage = 1;

    private static BufferedImage cornerTopLeft;
    private static BufferedImage[] wallTopTiles;
    private static BufferedImage cornerTopRight;
    private static BufferedImage[] wallLeftTiles;
    private static BufferedImage[] wallRightTiles;
    private static BufferedImage cornerBottomLeft;
    private static BufferedImage[] wallBottomTiles;
    private static BufferedImage cornerBottomRight;
    private static BufferedImage doorTop;
    private static BufferedImage doorBottom;
    private static BufferedImage doorLeft;
    private static BufferedImage doorRight;

    private static BufferedImage exitDoorTop;
    private static BufferedImage exitDoorBottom;
    private static BufferedImage exitDoorLeft;
    private static BufferedImage exitDoorRight;

    private static BufferedImage floorTile;
    private static BufferedImage chestTile;

    // [서충만님 코드] 기본 스프라이트 시트 로드 (Stage 1)
    public static void loadSprites() {
        loadSprites(1);
    }

    // [서충만님 코드] 특정 스테이지의 스프라이트 시트 로드
    public static void loadSprites(int stageNumber) {
        try {
            currentStage = stageNumber;

            String imagePath;
            if (stageNumber >= 1 && stageNumber <= 4) {
                imagePath = "src/map/assets/stage" + stageNumber + ".png";
            } else {
                imagePath = "src/map/assets/stage1.png";
            }

            spriteSheet = ImageIO.read(new File(imagePath));
            keyRoomSheet = ImageIO.read(new File("src/map/assets/keyRoom.png"));

            cornerTopLeft = getTileByIndex(0);
            cornerTopRight = getTileByIndex(5);
            cornerBottomLeft = getTileByIndex(40);
            cornerBottomRight = getTileByIndex(45);

            wallTopTiles = new BufferedImage[]{getTileByIndex(1), getTileByIndex(2), getTileByIndex(3), getTileByIndex(4)};
            wallLeftTiles = new BufferedImage[]{getTileByIndex(10), getTileByIndex(20), getTileByIndex(30)};
            wallRightTiles = new BufferedImage[]{getTileByIndex(15), getTileByIndex(25), getTileByIndex(35)};
            wallBottomTiles = new BufferedImage[]{getTileByIndex(41), getTileByIndex(42), getTileByIndex(43), getTileByIndex(44)};

            doorTop = getTileByIndex(36);
            doorBottom = getTileByIndex(37);
            doorLeft = getTileByIndex(48);
            doorRight = getTileByIndex(47);

            exitDoorTop = getTileByIndexFromKeyRoom(36);
            exitDoorBottom = getTileByIndexFromKeyRoom(37);
            exitDoorLeft = getTileByIndexFromKeyRoom(48);
            exitDoorRight = getTileByIndexFromKeyRoom(47);

            floorTile = getTileByIndex(18);
            chestTile = getTileByIndex(82);

            System.out.println("✓ Stage " + stageNumber + " 타일 스프라이트 로드 완료 (204x204)");
            System.out.println("  - 이미지: " + imagePath);
            System.out.println("  - 모서리: 좌상(0), 우상(5), 좌하(40), 우하(45)");
            System.out.println("  - 벽: 위(1-4), 아래(41-44), 좌(10,20,30), 우(15,25,35)");
            System.out.println("  - 문: 위(36), 아래(37), 좌(48), 우(47)");
            System.out.println("  - 바닥: 18번 고정");
            System.out.println("  - 상자: 82번");

        } catch (IOException e) {
            System.err.println("✗ 스프라이트 시트 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // [서충만님 코드] 인덱스로 타일 가져오기
    private static BufferedImage getTileByIndex(int index) {
        int x = (index % TILES_PER_ROW) * TILE_SIZE;
        int y = (index / TILES_PER_ROW) * TILE_SIZE;
        return spriteSheet.getSubimage(x, y, TILE_SIZE, TILE_SIZE);
    }

    // [서충만님 코드] KeyRoom.png에서 인덱스로 타일 가져오기
    private static BufferedImage getTileByIndexFromKeyRoom(int index) {
        int x = (index % TILES_PER_ROW) * TILE_SIZE;
        int y = (index / TILES_PER_ROW) * TILE_SIZE;
        return keyRoomSheet.getSubimage(x, y, TILE_SIZE, TILE_SIZE);
    }

    public static BufferedImage getCornerTopLeft() {
        return cornerTopLeft;
    }

    public static BufferedImage getWallTop(int x, int y) {
        int seed = x * 1000 + y;
        Random r = new Random(seed);
        return wallTopTiles[r.nextInt(wallTopTiles.length)];
    }

    public static BufferedImage getCornerTopRight() {
        return cornerTopRight;
    }

    public static BufferedImage getWallLeft(int x, int y) {
        int seed = x * 1000 + y;
        Random r = new Random(seed);
        return wallLeftTiles[r.nextInt(wallLeftTiles.length)];
    }

    public static BufferedImage getWallRight(int x, int y) {
        int seed = x * 1000 + y;
        Random r = new Random(seed);
        return wallRightTiles[r.nextInt(wallRightTiles.length)];
    }

    public static BufferedImage getCornerBottomLeft() {
        return cornerBottomLeft;
    }

    public static BufferedImage getWallBottom(int x, int y) {
        int seed = x * 1000 + y;
        Random r = new Random(seed);
        return wallBottomTiles[r.nextInt(wallBottomTiles.length)];
    }

    public static BufferedImage getCornerBottomRight() {
        return cornerBottomRight;
    }

    public static BufferedImage getDoorTop() {
        return doorTop;
    }

    public static BufferedImage getDoorLeft() {
        return doorLeft;
    }

    public static BufferedImage getDoorRight() {
        return doorRight;
    }

    public static BufferedImage getDoorBottom() {
        return doorBottom;
    }

    // [서충만님 코드] 바닥 타일 반환
    public static BufferedImage getFloorTile(int x, int y) {
        return floorTile;
    }

    // [서충만님 코드] 상자 타일 반환
    public static BufferedImage getChestTile() {
        return chestTile;
    }

    // [서충만님 코드] EXIT 문 타일 반환
    public static BufferedImage getExitDoorTop() {
        return exitDoorTop;
    }

    public static BufferedImage getExitDoorBottom() {
        return exitDoorBottom;
    }

    public static BufferedImage getExitDoorLeft() {
        return exitDoorLeft;
    }

    public static BufferedImage getExitDoorRight() {
        return exitDoorRight;
    }

    public static boolean isLoaded() {
        return cornerTopLeft != null && wallTopTiles != null && doorTop != null && floorTile != null;
    }

    // [서충만님 코드] 현재 로드된 스테이지 번호 반환
    public static int getCurrentStage() {
        return currentStage;
    }
}
