package map;

import game.Constants;

import java.awt.Graphics2D;

/**
 * 타일 렌더링을 담당하는 클래스
 */
public class TileManager {

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

                // 타일 색상 설정
                g2.setColor(tileType.getColor());

                // 타일 그리기
                int screenX = x * tileSize;
                int screenY = y * tileSize;
                g2.fillRect(screenX, screenY, tileSize, tileSize);
            }
        }
    }
}
