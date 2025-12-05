package map;

import java.util.HashMap;
import java.util.Map;

/**
 * 여러 방을 관리하는 클래스
 */
public class MapManager {
    private Map<Integer, Room> rooms; // 방 ID -> Room 객체
    private int currentRoomId;         // 현재 있는 방 ID

    public MapManager() {
        this.rooms = new HashMap<>();
        this.currentRoomId = 0;
    }

    /**
     * 방 추가
     */
    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    /**
     * 현재 방 가져오기
     */
    public Room getCurrentRoom() {
        return rooms.get(currentRoomId);
    }

    /**
     * 특정 방으로 이동
     */
    public boolean changeRoom(int roomId) {
        if (rooms.containsKey(roomId)) {
            currentRoomId = roomId;
            System.out.println(">>> 방 이동: Room " + roomId);
            return true;
        }
        return false;
    }

    /**
     * 현재 방 ID 가져오기
     */
    public int getCurrentRoomId() {
        return currentRoomId;
    }

    /**
     * 특정 방향으로 이동 시도
     */
    public boolean tryMoveDirection(Direction direction) {
        Room current = getCurrentRoom();
        if (current == null) {
            return false;
        }

        Integer targetRoomId = current.getConnectedRoom(direction);
        if (targetRoomId != null) {
            return changeRoom(targetRoomId);
        }
        return false;
    }

    /**
     * 테스트용: 3개 방 생성 (START -> NORMAL -> KEY)
     * 각 방은 화면 전체 크기(20x12)로 생성
     */
    public static MapManager createTestMap() {
        MapManager manager = new MapManager();

        // Room 0: START (20x12 - 화면 전체)
        Room room0 = new Room(0, RoomType.START, 20, 12);
        for (int y = 0; y < room0.getHeight(); y++) {
            for (int x = 0; x < room0.getWidth(); x++) {
                if (x == 0 || x == room0.getWidth() - 1 ||
                    y == 0 || y == room0.getHeight() - 1) {
                    room0.setTile(x, y, TileType.WALL);
                } else {
                    room0.setTile(x, y, TileType.FLOOR);
                }
            }
        }
        room0.setTile(room0.getWidth() / 2, room0.getHeight() - 1, TileType.DOOR); // 아래쪽 문
        room0.addConnection(Direction.SOUTH, 1);

        // Room 1: NORMAL (20x12 - 화면 전체)
        Room room1 = new Room(1, RoomType.NORMAL, 20, 12);
        for (int y = 0; y < room1.getHeight(); y++) {
            for (int x = 0; x < room1.getWidth(); x++) {
                if (x == 0 || x == room1.getWidth() - 1 ||
                    y == 0 || y == room1.getHeight() - 1) {
                    room1.setTile(x, y, TileType.WALL);
                } else {
                    room1.setTile(x, y, TileType.FLOOR);
                }
            }
        }
        room1.setTile(room1.getWidth() / 2, 0, TileType.DOOR); // 위쪽 문
        room1.setTile(room1.getWidth() - 1, room1.getHeight() / 2, TileType.DOOR); // 오른쪽 문
        room1.addConnection(Direction.NORTH, 0);
        room1.addConnection(Direction.EAST, 2);

        // Room 2: KEY (20x12 - 화면 전체)
        Room room2 = new Room(2, RoomType.KEY, 20, 12);
        for (int y = 0; y < room2.getHeight(); y++) {
            for (int x = 0; x < room2.getWidth(); x++) {
                if (x == 0 || x == room2.getWidth() - 1 ||
                    y == 0 || y == room2.getHeight() - 1) {
                    room2.setTile(x, y, TileType.WALL);
                } else {
                    room2.setTile(x, y, TileType.FLOOR);
                }
            }
        }
        room2.setTile(0, room2.getHeight() / 2, TileType.DOOR); // 왼쪽 문
        room2.addConnection(Direction.WEST, 1);

        manager.addRoom(room0);
        manager.addRoom(room1);
        manager.addRoom(room2);

        return manager;
    }
}
