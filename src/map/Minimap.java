package map;

import common.Constants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.util.HashMap;
import java.util.Map;

// [서충만님 코드] 미니맵 렌더링 클래스: 화면 오른쪽 상단에 방 구조를 표시
public class Minimap {

    private static final int MINIMAP_PADDING = 20;
    private static final int ROOM_SIZE = 30;
    private static final int ROOM_SPACING = 10;
    private static final int CONNECTION_WIDTH = 4;

    private static final Color CURRENT_ROOM_COLOR = new Color(100, 200, 255);
    private static final Color VISITED_ROOM_COLOR = new Color(150, 150, 150);
    private static final Color KEY_ROOM_COLOR = new Color(255, 215, 0);
    private static final Color ELITE_ROOM_COLOR = new Color(220, 20, 60);
    private static final Color CONNECTION_COLOR = new Color(100, 100, 100);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);

    private Map<Integer, int[]> roomPositions;

    public Minimap() {
        roomPositions = new HashMap<>();
        calculateRoomPositions();
    }

    // [서충만님 코드] 각 방의 미니맵 상의 위치 계산 (BFS 방식)
    private void calculateRoomPositions() {
        roomPositions.put(0, new int[]{2, 2});

        java.util.Queue<Integer> queue = new java.util.LinkedList<>();
        java.util.Set<Integer> visited = new java.util.HashSet<>();

        queue.add(0);
        visited.add(0);

        while (!queue.isEmpty()) {
            int currentRoomId = queue.poll();
            RoomData currentRoom = MapLoader.getRoom(currentRoomId);

            if (currentRoom == null) continue;

            int[] currentPos = roomPositions.get(currentRoomId);

            for (java.util.Map.Entry<String, Integer> connection : currentRoom.getConnections().entrySet()) {
                String direction = connection.getKey();
                Integer nextRoomId = connection.getValue();

                if (nextRoomId == null || visited.contains(nextRoomId)) continue;

                int[] nextPos = new int[2];
                switch (direction) {
                    case "NORTH":
                        nextPos[0] = currentPos[0];
                        nextPos[1] = currentPos[1] - 1;
                        break;
                    case "SOUTH":
                        nextPos[0] = currentPos[0];
                        nextPos[1] = currentPos[1] + 1;
                        break;
                    case "EAST":
                        nextPos[0] = currentPos[0] + 1;
                        nextPos[1] = currentPos[1];
                        break;
                    case "WEST":
                        nextPos[0] = currentPos[0] - 1;
                        nextPos[1] = currentPos[1];
                        break;
                }

                roomPositions.put(nextRoomId, nextPos);
                visited.add(nextRoomId);
                queue.add(nextRoomId);
            }
        }
    }

    // [서충만님 코드] 미니맵 렌더링
    public void render(Graphics2D g2, int currentRoomId) {
        int minimapX = Constants.WINDOW_WIDTH - MINIMAP_PADDING - (ROOM_SIZE + ROOM_SPACING) * 5;
        int minimapY = Constants.WINDOW_HEIGHT - MINIMAP_PADDING - (ROOM_SIZE + ROOM_SPACING) * 5;

        renderConnections(g2, minimapX, minimapY);

        for (Map.Entry<Integer, int[]> entry : roomPositions.entrySet()) {
            int roomId = entry.getKey();
            int[] gridPos = entry.getValue();

            int x = minimapX + gridPos[0] * (ROOM_SIZE + ROOM_SPACING);
            int y = minimapY + gridPos[1] * (ROOM_SIZE + ROOM_SPACING);

            renderRoom(g2, x, y, roomId, currentRoomId);
        }
    }

    // [서충만님 코드] 방 사이의 연결선 렌더링
    private void renderConnections(Graphics2D g2, int offsetX, int offsetY) {
        g2.setColor(CONNECTION_COLOR);
        g2.setStroke(new BasicStroke(CONNECTION_WIDTH));

        java.util.Set<String> drawnConnections = new java.util.HashSet<>();

        for (Map.Entry<Integer, int[]> entry : roomPositions.entrySet()) {
            int roomId = entry.getKey();
            RoomData room = MapLoader.getRoom(roomId);

            if (room == null) continue;

            for (Map.Entry<String, Integer> connection : room.getConnections().entrySet()) {
                String direction = connection.getKey();
                Integer connectedRoomId = connection.getValue();

                if (connectedRoomId == null) continue;

                String connectionId = Math.min(roomId, connectedRoomId) + "-" + Math.max(roomId, connectedRoomId);

                if (drawnConnections.contains(connectionId)) continue;

                drawConnection(g2, offsetX, offsetY, roomId, connectedRoomId, direction);
                drawnConnections.add(connectionId);
            }
        }
    }

    // [서충만님 코드] 두 방 사이의 연결선 그리기
    private void drawConnection(Graphics2D g2, int offsetX, int offsetY,
                                int fromRoom, int toRoom, String direction) {
        int[] fromPos = roomPositions.get(fromRoom);
        int[] toPos = roomPositions.get(toRoom);

        if (fromPos == null || toPos == null) return;

        int fromX = offsetX + fromPos[0] * (ROOM_SIZE + ROOM_SPACING) + ROOM_SIZE / 2;
        int fromY = offsetY + fromPos[1] * (ROOM_SIZE + ROOM_SPACING) + ROOM_SIZE / 2;
        int toX = offsetX + toPos[0] * (ROOM_SIZE + ROOM_SPACING) + ROOM_SIZE / 2;
        int toY = offsetY + toPos[1] * (ROOM_SIZE + ROOM_SPACING) + ROOM_SIZE / 2;

        if (direction.equals("SOUTH") || direction.equals("NORTH")) {
            g2.drawLine(fromX, fromY, fromX, toY);
        } else {
            g2.drawLine(fromX, fromY, toX, fromY);
        }
    }

    // [서충만님 코드] 개별 방 렌더링
    private void renderRoom(Graphics2D g2, int x, int y, int roomId, int currentRoomId) {
        boolean isCurrent = (roomId == currentRoomId);

        RoomData roomData = MapLoader.getRoom(roomId);
        String roomType = (roomData != null) ? roomData.getRoomType() : "NORMAL";

        if (isCurrent) {
            g2.setColor(CURRENT_ROOM_COLOR);
        } else if ("KEY".equals(roomType)) {
            g2.setColor(KEY_ROOM_COLOR);
        } else if ("ELITE".equals(roomType)) {
            g2.setColor(ELITE_ROOM_COLOR);
        } else {
            g2.setColor(VISITED_ROOM_COLOR);
        }
        g2.fillRect(x, y, ROOM_SIZE, ROOM_SIZE);

        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, ROOM_SIZE, ROOM_SIZE);

        if (isCurrent) {
            g2.setColor(Color.WHITE);
            g2.drawString(String.valueOf(roomId), x + ROOM_SIZE / 2 - 5, y + ROOM_SIZE / 2 + 5);
        }
    }
}
