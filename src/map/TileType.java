package map;

import java.awt.Color;

/**
 * 타일의 종류를 정의하는 enum
 */
public enum TileType {
    WALL('W', Color.DARK_GRAY, true),      // 벽
    FLOOR('.', Color.LIGHT_GRAY, false),   // 바닥
    DOOR('D', Color.ORANGE, false),        // 방 이동 문
    EXIT('X', Color.YELLOW, false);        // 다음 스테이지 문

    private final char symbol;      // 맵 파일에서 사용하는 심볼
    private final Color color;      // 렌더링 색상
    private final boolean solid;    // 충돌 여부 (true = 통과 불가)

    TileType(char symbol, Color color, boolean solid) {
        this.symbol = symbol;
        this.color = color;
        this.solid = solid;
    }

    public char getSymbol() {
        return symbol;
    }

    public Color getColor() {
        return color;
    }

    public boolean isSolid() {
        return solid;
    }

    /**
     * 심볼 문자로부터 TileType을 찾음
     */
    public static TileType fromSymbol(char symbol) {
        for (TileType type : values()) {
            if (type.symbol == symbol) {
                return type;
            }
        }
        return FLOOR; // 기본값
    }
}
