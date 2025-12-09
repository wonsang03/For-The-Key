package map;

import common.Constants;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * 타일 렌더링을 담당하는 클래스
 */
public class TileManager {

    private boolean useSprites = false;

    public TileManager() {
        // 스프라이트 로드 시도
        TileSprites.loadSprites();
        useSprites = TileSprites.isLoaded();
    }

    /**
     * 맵을 화면에 렌더링
     * @param g2 Graphics2D 객체
     * @param map 20x12 크기의 맵 데이터
     */
    public void render(Graphics2D g2, char[][] map) {
        if (map == null) {
            return;
        }

        int tileSize = Constants.TILE_SIZE;

        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 20; x++) {
                char tile = map[y][x];
                TileType tileType = TileType.fromSymbol(tile);

                int screenX = x * tileSize;
                int screenY = y * tileSize;

                if (useSprites) {
                    // 스프라이트 사용
                    renderWithSprite(g2, tile, x, y, map, screenX, screenY, tileSize);
                } else {
                    // 색상만 사용 (폴백)
                    g2.setColor(tileType.getColor());
                    g2.fillRect(screenX, screenY, tileSize, tileSize);
                }
            }
        }
    }

    /**
     * 스프라이트로 타일 렌더링
     */
    private void renderWithSprite(Graphics2D g2, char tile, int x, int y, char[][] map,
                                   int screenX, int screenY, int tileSize) {
        BufferedImage sprite = null;

        if (tile == 'W') {
            // 벽 타일 - 위치에 따라 다른 스프라이트 사용
            sprite = getWallSprite(x, y, map);
        } else if (tile == 'D' || tile == 'E') {
            // 문 또는 출구 - 위치에 따라 회전된 문 스프라이트 사용
            sprite = getDoorSprite(x, y, map);
        } else if (tile == '.') {
            // 바닥 - 랜덤 바닥 타일 (좌표 기반으로 일관성 유지)
            sprite = TileSprites.getFloorTile(x, y);
        }

        if (sprite != null) {
            g2.drawImage(sprite, screenX, screenY, tileSize, tileSize, null);
        } else {
            // 스프라이트가 없으면 색상으로 대체
            TileType tileType = TileType.fromSymbol(tile);
            g2.setColor(tileType.getColor());
            g2.fillRect(screenX, screenY, tileSize, tileSize);
        }
    }

    /**
     * 벽 타일의 위치에 따른 스프라이트 반환
     */
    private BufferedImage getWallSprite(int x, int y, char[][] map) {
        int width = map[0].length;
        int height = map.length;

        // 모서리 체크
        if (x == 0 && y == 0) {
            // 왼쪽 위 모서리
            return TileSprites.getCornerTopLeft();
        } else if (x == width - 1 && y == 0) {
            // 오른쪽 위 모서리
            return TileSprites.getCornerTopRight();
        } else if (x == 0 && y == height - 1) {
            // 왼쪽 아래 모서리
            return TileSprites.getCornerBottomLeft();
        } else if (x == width - 1 && y == height - 1) {
            // 오른쪽 아래 모서리
            return TileSprites.getCornerBottomRight();
        }

        // 벽 방향 체크
        if (y == 0) {
            // 위쪽 벽
            return TileSprites.getWallTop(x, y);
        } else if (y == height - 1) {
            // 아래쪽 벽
            return TileSprites.getWallBottom(x, y);
        } else if (x == 0) {
            // 왼쪽 벽
            return TileSprites.getWallLeft(x, y);
        } else if (x == width - 1) {
            // 오른쪽 벽
            return TileSprites.getWallRight(x, y);
        }

        // 기본 벽 (내부 벽)
        return TileSprites.getWallTop(x, y);
    }

    /**
     * 문의 위치에 따른 회전된 스프라이트 반환
     */
    private BufferedImage getDoorSprite(int x, int y, char[][] map) {
        int width = map[0].length;
        int height = map.length;

        if (y == 0) {
            // 위쪽 벽에 있는 문
            return TileSprites.getDoorTop();
        } else if (y == height - 1) {
            // 아래쪽 벽에 있는 문
            return TileSprites.getDoorBottom();
        } else if (x == 0) {
            // 왼쪽 벽에 있는 문
            return TileSprites.getDoorLeft();
        } else if (x == width - 1) {
            // 오른쪽 벽에 있는 문
            return TileSprites.getDoorRight();
        }

        // 기본 문 (위쪽)
        return TileSprites.getDoorTop();
    }
}
