package map;

import java.awt.Color;

// [서충만님 코드] 타일의 종류를 정의하는 enum
public enum TileType {
    WALL('W', Color.DARK_GRAY, true),
    FLOOR('.', Color.LIGHT_GRAY, false),
    DOOR('D', Color.ORANGE, false),
    EXIT('X', Color.YELLOW, false);

    private final char symbol;
    private final Color color;
    private final boolean solid;

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

    // [서충만님 코드] 심볼 문자로부터 TileType을 찾음
    public static TileType fromSymbol(char symbol) {
        for (TileType type : values()) {
            if (type.symbol == symbol) {
                return type;
            }
        }
        return FLOOR;
    }
}
