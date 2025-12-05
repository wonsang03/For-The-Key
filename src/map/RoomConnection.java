package map;

/**
 * 방과 방 사이의 연결 정보를 담는 클래스
 */
public class RoomConnection {
    private Direction direction;  // 연결 방향
    private int targetRoomId;     // 연결된 방 ID

    public RoomConnection(Direction direction, int targetRoomId) {
        this.direction = direction;
        this.targetRoomId = targetRoomId;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getTargetRoomId() {
        return targetRoomId;
    }
}
