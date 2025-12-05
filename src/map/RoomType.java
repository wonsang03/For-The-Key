package map;

/**
 * 방의 타입을 정의하는 enum
 */
public enum RoomType {
    START,   // 시작 방
    NORMAL,  // 일반 전투 방
    KEY,     // 열쇠 방
    EXIT;    // 다음 스테이지로 가는 방

    /**
     * 문자열로부터 RoomType을 찾음
     */
    public static RoomType fromString(String str) {
        try {
            return RoomType.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NORMAL; // 기본값
        }
    }
}
