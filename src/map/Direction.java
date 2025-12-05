package map;

/**
 * 방 연결 방향을 정의하는 enum
 */
public enum Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST;

    /**
     * 문자열로부터 Direction을 찾음
     */
    public static Direction fromString(String str) {
        try {
            return Direction.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
