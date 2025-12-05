package map;

import java.util.ArrayList;
import java.util.List;

/**
 * 방 하나를 나타내는 클래스
 */
public class Room {
    private int id;
    private RoomType type;
    private int width;
    private int height;
    private TileType[][] tileMap; // 타일 데이터 (2D 배열)
    private List<RoomConnection> connections; // 다른 방과의 연결 정보

    public Room(int id, RoomType type, int width, int height) {
        this.id = id;
        this.type = type;
        this.width = width;
        this.height = height;
        this.tileMap = new TileType[height][width];
        this.connections = new ArrayList<>();
    }

    /**
     * 특정 위치에 타일 설정
     */
    public void setTile(int x, int y, TileType tile) {
        if (isValidPosition(x, y)) {
            tileMap[y][x] = tile;
        }
    }

    /**
     * 특정 위치의 타일 가져오기
     */
    public TileType getTile(int x, int y) {
        if (isValidPosition(x, y)) {
            return tileMap[y][x];
        }
        return TileType.WALL; // 범위 밖은 벽 처리
    }

    /**
     * 유효한 좌표인지 확인
     */
    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    // Getters
    public int getId() {
        return id;
    }

    public RoomType getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * 방 연결 추가
     */
    public void addConnection(Direction direction, int targetRoomId) {
        connections.add(new RoomConnection(direction, targetRoomId));
    }

    /**
     * 특정 방향의 연결된 방 ID 가져오기
     */
    public Integer getConnectedRoom(Direction direction) {
        for (RoomConnection conn : connections) {
            if (conn.getDirection() == direction) {
                return conn.getTargetRoomId();
            }
        }
        return null; // 연결 없음
    }

    /**
     * 모든 연결 정보 가져오기
     */
    public List<RoomConnection> getConnections() {
        return connections;
    }

    /**
     * 방 정보 출력 (디버깅용)
     */
    public void printInfo() {
        System.out.println("======================");
        System.out.println("Room ID: " + id);
        System.out.println("Type: " + type);
        System.out.println("Size: " + width + " x " + height);
        System.out.println("======================");
    }

    /**
     * 방을 콘솔에 시각적으로 출력 (디버깅용)
     */
    public void printMap() {
        System.out.println("Room " + id + " (" + type + ") - " + width + "x" + height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TileType tile = getTile(x, y);
                System.out.print(tile.getSymbol());
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * 테스트용: 간단한 방 생성 (하드코딩)
     */
    public static Room createTestRoom() {
        Room room = new Room(0, RoomType.START, 10, 8);

        // 벽으로 둘러싸인 방 생성
        for (int y = 0; y < room.height; y++) {
            for (int x = 0; x < room.width; x++) {
                if (x == 0 || x == room.width - 1 ||
                    y == 0 || y == room.height - 1) {
                    room.setTile(x, y, TileType.WALL);
                } else {
                    room.setTile(x, y, TileType.FLOOR);
                }
            }
        }

        // 아래쪽 중앙에 문 추가
        room.setTile(room.width / 2, room.height - 1, TileType.DOOR);

        return room;
    }
}
