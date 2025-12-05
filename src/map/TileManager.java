package map;

import game.Constants;

import java.awt.Graphics2D;

/**
 * 타일 렌더링을 담당하는 클래스
 */
public class TileManager {

    /**
     * 방 전체를 화면에 렌더링
     * @param g2 Graphics2D 객체
     * @param room 렌더링할 방
     */
    public void render(Graphics2D g2, Room room) {
        if (room == null) {
            return;
        }

        int tileSize = Constants.TILE_SIZE;

        for (int y = 0; y < room.getHeight(); y++) {
            for (int x = 0; x < room.getWidth(); x++) {
                TileType tile = room.getTile(x, y);

                // 타일 색상 설정
                g2.setColor(tile.getColor());

                // 타일 그리기
                int screenX = x * tileSize;
                int screenY = y * tileSize;
                g2.fillRect(screenX, screenY, tileSize, tileSize);

                // 타일 테두리 (선택사항)
                // g2.setColor(Color.BLACK);
                // g2.drawRect(screenX, screenY, tileSize, tileSize);
            }
        }
    }

    /**
     * 특정 위치가 벽(충돌)인지 확인
     * @param room 체크할 방
     * @param x 타일 X 좌표
     * @param y 타일 Y 좌표
     * @return true면 통과 불가
     */
    public boolean isSolid(Room room, int x, int y) {
        if (room == null) {
            return true;
        }
        TileType tile = room.getTile(x, y);
        return tile.isSolid();
    }
}
