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
     * 맵을 화면에 렌더링 (가변 크기 지원)
     * @param g2 Graphics2D 객체
     * @param map 가변 크기의 맵 데이터
     */
    public void render(Graphics2D g2, char[][] map) {
        if (map == null || map.length == 0) {
            return;
        }

        int tileSize = Constants.TILE_SIZE;
        int height = map.length;
        int width = map[0].length;

        // 맵을 화면 중앙에 배치하기 위한 오프셋 계산
        int offsetX = (Constants.WINDOW_WIDTH - (width * tileSize)) / 2;
        int offsetY = (Constants.WINDOW_HEIGHT - (height * tileSize)) / 2;

        // 1단계: 기본 타일 렌더링 (벽, 바닥, 문 등)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char tile = map[y][x];
                TileType tileType = TileType.fromSymbol(tile);

                int screenX = offsetX + (x * tileSize);
                int screenY = offsetY + (y * tileSize);

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

        // 2단계: 오버레이 렌더링 (상자 등)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char tile = map[y][x];
                int screenX = offsetX + (x * tileSize);
                int screenY = offsetY + (y * tileSize);

                // 상자 타일 (C)인 경우 바닥 위에 상자 렌더링
                if (tile == 'C' && useSprites) {
                    BufferedImage chestSprite = TileSprites.getChestTile();
                    if (chestSprite != null) {
                        g2.drawImage(chestSprite, screenX, screenY, tileSize, tileSize, null);
                    }
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
        } else if (tile == '.' || tile == 'C') {
            // 바닥 - 랜덤 바닥 타일 (좌표 기반으로 일관성 유지)
            // 'C' (상자) 위치도 바닥 타일로 렌더링 (상자는 나중에 오버레이)
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
