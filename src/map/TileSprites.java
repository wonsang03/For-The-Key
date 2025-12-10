package map;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import common.Constants;

/**
 * 타일 스프라이트 관리 클래스
 * 스테이지별로 다른 타일셋을 로드
 */
public class TileSprites {
    private static BufferedImage spriteSheet;
    private static final int TILE_SIZE = 204;
    private static final int TILES_PER_ROW = 10; // 가로 10개 타일
    private static int currentStage = 1; // 현재 로드된 스테이지

    // 각 타일 스프라이트
    private static BufferedImage cornerTopLeft;      // 0번
    private static BufferedImage[] wallTopTiles;     // 1,2,3,4번
    private static BufferedImage cornerTopRight;     // 5번
    private static BufferedImage[] wallLeftTiles;    // 10,20,30번
    private static BufferedImage[] wallRightTiles;   // 15,25,35번
    private static BufferedImage cornerBottomLeft;   // 40번
    private static BufferedImage[] wallBottomTiles;  // 41,42,43,44번
    private static BufferedImage cornerBottomRight;  // 45번
    private static BufferedImage doorTop;            // 36번
    private static BufferedImage doorBottom;         // 37번
    private static BufferedImage doorLeft;           // 48번
    private static BufferedImage doorRight;          // 47번

    // 바닥 타일 (18번으로 고정)
    private static BufferedImage floorTile;

    /**
     * 기본 스프라이트 시트 로드 (Stage 1)
     */
    public static void loadSprites() {
        loadSprites(1);
    }

    /**
     * 특정 스테이지의 스프라이트 시트 로드
     * @param stageNumber 스테이지 번호 (1-5)
     */
    public static void loadSprites(int stageNumber) {
        try {
            currentStage = stageNumber;

            // 스테이지별 이미지 파일 경로
            String imagePath;
            if (stageNumber >= 1 && stageNumber <= 4) {
                imagePath = "src/map/assets/stage" + stageNumber + ".png";
            } else {
                // stage5는 이미지가 없으므로 stage1 재사용
                imagePath = "src/map/assets/stage1.png";
            }

            File imageFile = Constants.getResourceFile(imagePath);
            spriteSheet = ImageIO.read(imageFile);

            // 모서리 타일
            cornerTopLeft = getTileByIndex(0);
            cornerTopRight = getTileByIndex(5);
            cornerBottomLeft = getTileByIndex(40);
            cornerBottomRight = getTileByIndex(45);

            // 벽 타일 배열
            wallTopTiles = new BufferedImage[]{getTileByIndex(1), getTileByIndex(2), getTileByIndex(3), getTileByIndex(4)};
            wallLeftTiles = new BufferedImage[]{getTileByIndex(10), getTileByIndex(20), getTileByIndex(30)};
            wallRightTiles = new BufferedImage[]{getTileByIndex(15), getTileByIndex(25), getTileByIndex(35)};
            wallBottomTiles = new BufferedImage[]{getTileByIndex(41), getTileByIndex(42), getTileByIndex(43), getTileByIndex(44)};

            // 문 타일
            doorTop = getTileByIndex(36);
            doorBottom = getTileByIndex(37);
            doorLeft = getTileByIndex(48);
            doorRight = getTileByIndex(47);

            // 바닥 타일 (18번으로 고정)
            floorTile = getTileByIndex(18);

            System.out.println("✓ Stage " + stageNumber + " 타일 스프라이트 로드 완료 (204x204)");
            System.out.println("  - 이미지: " + imagePath);
            System.out.println("  - 모서리: 좌상(0), 우상(5), 좌하(40), 우하(45)");
            System.out.println("  - 벽: 위(1-4), 아래(41-44), 좌(10,20,30), 우(15,25,35)");
            System.out.println("  - 문: 위(36), 아래(37), 좌(48), 우(47)");
            System.out.println("  - 바닥: 18번 고정");

        } catch (IOException e) {
            System.err.println("✗ 스프라이트 시트 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 인덱스로 타일 가져오기
     * @param index 타일 인덱스 (0부터 시작, 좌->우, 위->아래)
     */
    private static BufferedImage getTileByIndex(int index) {
        int x = (index % TILES_PER_ROW) * TILE_SIZE;
        int y = (index / TILES_PER_ROW) * TILE_SIZE;
        return spriteSheet.getSubimage(x, y, TILE_SIZE, TILE_SIZE);
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

    /**
     * 바닥 타일 반환 (18번으로 고정)
     */
    public static BufferedImage getFloorTile(int x, int y) {
        return floorTile;
    }

    public static boolean isLoaded() {
        return cornerTopLeft != null && wallTopTiles != null && doorTop != null && floorTile != null;
    }

    /**
     * 현재 로드된 스테이지 번호 반환
     */
    public static int getCurrentStage() {
        return currentStage;
    }
}
