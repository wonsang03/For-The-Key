package map;

import common.Constants;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import main.GamePanel; // [김선욱님 코드] 상자 등록용 GamePanel 참조 추가

// [서충만님 코드] 타일 렌더링을 담당하는 클래스
public class TileManager {

    private boolean useSprites = false;
    private GamePanel gamePanel; // [김선욱님 코드] Box 등록을 위한 참조

    // [김선욱님 코드] 생성자 수정 (GamePanel 연결)
    public TileManager(GamePanel panel) {
        this.gamePanel = panel;
        TileSprites.loadSprites();
        useSprites = TileSprites.isLoaded();
    }

    public TileManager() {
		// TODO Auto-generated constructor stub
	}

	// [서충만님 코드] 맵을 화면에 렌더링
    public void render(Graphics2D g2, char[][] map) {
        if (map == null || map.length == 0) {
            return;
        }

        int tileSize = Constants.TILE_SIZE;
        int height = map.length;
        int width = map[0].length;

        int offsetX = (Constants.WINDOW_WIDTH - (width * tileSize)) / 2;
        int offsetY = (Constants.WINDOW_HEIGHT - (height * tileSize)) / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char tile = map[y][x];
                TileType tileType = TileType.fromSymbol(tile);

                int screenX = offsetX + (x * tileSize);
                int screenY = offsetY + (y * tileSize);

                if (useSprites) {
                    renderWithSprite(g2, tile, x, y, map, screenX, screenY, tileSize);
                } else {
                    g2.setColor(tileType.getColor());
                    g2.fillRect(screenX, screenY, tileSize, tileSize);
                }
            }
        }

        // [김선욱님 코드] 상자(C) 스프라이트 + Box 객체 등록
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char tile = map[y][x];
                int screenX = offsetX + (x * tileSize);
                int screenY = offsetY + (y * tileSize);

                if (tile == 'C' && useSprites) {
                    BufferedImage chestSprite = TileSprites.getChestTile();
                    if (chestSprite != null) {
                        g2.drawImage(chestSprite, screenX, screenY, tileSize, tileSize, null);

                        // [김선욱님 코드] Box 객체를 GamePanel에 자동 등록 (중복 방지)
                        if (gamePanel != null && !gamePanel.boxExistsAt(screenX, screenY)) {
                            gamePanel.addBoxAt(screenX, screenY);
                        }
                    }
                }
            }
        }
    }

    // [서충만님 코드] 스프라이트로 타일 렌더링
    private void renderWithSprite(Graphics2D g2, char tile, int x, int y, char[][] map,
                                   int screenX, int screenY, int tileSize) {
        BufferedImage sprite = null;

        if (tile == 'W') {
            sprite = getWallSprite(x, y, map);
        } else if (tile == 'D') {
            sprite = getDoorSprite(x, y, map);
        } else if (tile == 'X') {
            sprite = getExitDoorSprite(x, y, map);
        } else if (tile == '.' || tile == 'C' || tile == 'E' || tile == 'L') {
            sprite = TileSprites.getFloorTile(x, y);
        }

        if (sprite != null) {
            g2.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
        } else {
            TileType tileType = TileType.fromSymbol(tile);
            g2.setColor(tileType.getColor());
            g2.fillRect(screenX, screenY, tileSize, tileSize);
        }
    }

    // [서충만님 코드] 벽 타일의 위치에 따른 스프라이트 반환
    private BufferedImage getWallSprite(int x, int y, char[][] map) {
        int width = map[0].length;
        int height = map.length;

        if (x == 0 && y == 0) {
            return TileSprites.getCornerTopLeft();
        } else if (x == width - 1 && y == 0) {
            return TileSprites.getCornerTopRight();
        } else if (x == 0 && y == height - 1) {
            return TileSprites.getCornerBottomLeft();
        } else if (x == width - 1 && y == height - 1) {
            return TileSprites.getCornerBottomRight();
        }

        if (y == 0) {
            return TileSprites.getWallTop(x, y);
        } else if (y == height - 1) {
            return TileSprites.getWallBottom(x, y);
        } else if (x == 0) {
            return TileSprites.getWallLeft(x, y);
        } else if (x == width - 1) {
            return TileSprites.getWallRight(x, y);
        }

        return TileSprites.getWallTop(x, y);
    }

    // [서충만님 코드] 문의 위치에 따른 회전된 스프라이트 반환
    private BufferedImage getDoorSprite(int x, int y, char[][] map) {
        int width = map[0].length;
        int height = map.length;

        if (y == 0) {
            return TileSprites.getDoorTop();
        } else if (y == height - 1) {
            return TileSprites.getDoorBottom();
        } else if (x == 0) {
            return TileSprites.getDoorLeft();
        } else if (x == width - 1) {
            return TileSprites.getDoorRight();
        }

        return TileSprites.getDoorTop();
    }

    // [서충만님 코드] EXIT 문의 위치에 따른 회전된 스프라이트 반환
    private BufferedImage getExitDoorSprite(int x, int y, char[][] map) {
        int width = map[0].length;
        int height = map.length;

        if (y == 0) {
            return TileSprites.getExitDoorTop();
        } else if (y == height - 1) {
            return TileSprites.getExitDoorBottom();
        } else if (x == 0) {
            return TileSprites.getExitDoorLeft();
        } else if (x == width - 1) {
            return TileSprites.getExitDoorRight();
        }

        return TileSprites.getExitDoorTop();
    }
}
