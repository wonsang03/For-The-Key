package map;

import java.util.HashMap;
import java.util.Map;

/**
 * 방 데이터와 연결 정보를 담는 클래스
 */
public class RoomData {
    private int roomId;
    private char[][] map;
    private Map<String, Integer> connections; // 방향 -> 연결된 방 ID
    private String roomType; // 방 타입 (START, NORMAL, KEY, BOSS, EXIT)

    public RoomData(int roomId, char[][] map) {
        this(roomId, map, "NORMAL");
    }

    public RoomData(int roomId, char[][] map, String roomType) {
        this.roomId = roomId;
        this.map = map;
        this.roomType = roomType;
        this.connections = new HashMap<>();
    }

    public int getRoomId() {
        return roomId;
    }

    public char[][] getMap() {
        return map;
    }

    public void addConnection(String direction, int targetRoomId) {
        connections.put(direction.toUpperCase(), targetRoomId);
    }

    public Integer getConnectedRoom(String direction) {
        return connections.get(direction.toUpperCase());
    }

    public Map<String, Integer> getConnections() {
        return connections;
    }

    public boolean hasConnection(String direction) {
        return connections.containsKey(direction.toUpperCase());
    }

    public String getRoomType() {
        return roomType;
    }
}
