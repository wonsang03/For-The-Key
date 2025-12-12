package map;

import common.Constants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.util.HashMap;
import java.util.Map;

/**
 * 미니맵 렌더링 클래스
 * 화면 오른쪽 상단에 방 구조를 표시
 */
public class Minimap {

    // 미니맵 설정
    private static final int MINIMAP_PADDING = 20;  // 화면 가장자리로부터의 여백
    private static final int ROOM_SIZE = 30;         // 각 방의 크기
    private static final int ROOM_SPACING = 10;      // 방 사이의 간격
    private static final int CONNECTION_WIDTH = 4;   // 연결선 두께

    // 색상 설정
    private static final Color CURRENT_ROOM_COLOR = new Color(100, 200, 255);  // 현재 방 (밝은 파랑)
    private static final Color VISITED_ROOM_COLOR = new Color(150, 150, 150);  // 방문한 방 (회색)
    private static final Color KEY_ROOM_COLOR = new Color(255, 215, 0);        // 열쇠 방 (금색)
    private static final Color CONNECTION_COLOR = new Color(100, 100, 100);     // 연결선 (어두운 회색)
    private static final Color BORDER_COLOR = new Color(200, 200, 200);         // 테두리

    // 각 방의 위치를 저장 (roomId -> {x, y})
    private Map<Integer, int[]> roomPositions;

    public Minimap() {
        roomPositions = new HashMap<>();
        calculateRoomPositions();
    }

    /**
     * 각 방의 미니맵 상의 위치 계산 (MapLoader 데이터 기반)
     * Room 0을 중심으로 상대적 위치를 자동 계산
     */
    private void calculateRoomPositions() {
        // Room 0을 시작점으로 설정
        roomPositions.put(0, new int[]{2, 2});

        // BFS 방식으로 연결된 방들의 위치 계산
        java.util.Queue<Integer> queue = new java.util.LinkedList<>();
        java.util.Set<Integer> visited = new java.util.HashSet<>();

        queue.add(0);
        visited.add(0);

        while (!queue.isEmpty()) {
            int currentRoomId = queue.poll();
            RoomData currentRoom = MapLoader.getRoom(currentRoomId);

            if (currentRoom == null) continue;

            int[] currentPos = roomPositions.get(currentRoomId);

            // 연결된 방들의 위치 계산
            for (java.util.Map.Entry<String, Integer> connection : currentRoom.getConnections().entrySet()) {
                String direction = connection.getKey();
                Integer nextRoomId = connection.getValue();

                if (nextRoomId == null || visited.contains(nextRoomId)) continue;

                // 방향에 따라 다음 방의 위치 계산
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

    /**
     * 미니맵 렌더링
     * @param g2 Graphics2D 객체
     * @param currentRoomId 현재 플레이어가 있는 방 ID
     */
    public void render(Graphics2D g2, int currentRoomId) {
        // 미니맵 위치 (오른쪽 하단)
        int minimapX = Constants.WINDOW_WIDTH - MINIMAP_PADDING - (ROOM_SIZE + ROOM_SPACING) * 5;
        int minimapY = Constants.WINDOW_HEIGHT - MINIMAP_PADDING - (ROOM_SIZE + ROOM_SPACING) * 5;

        // 먼저 연결선 그리기 (방 아래에 표시)
        renderConnections(g2, minimapX, minimapY);

        // 방들 그리기
        for (Map.Entry<Integer, int[]> entry : roomPositions.entrySet()) {
            int roomId = entry.getKey();
            int[] gridPos = entry.getValue();

            int x = minimapX + gridPos[0] * (ROOM_SIZE + ROOM_SPACING);
            int y = minimapY + gridPos[1] * (ROOM_SIZE + ROOM_SPACING);

            renderRoom(g2, x, y, roomId, currentRoomId);
        }
    }

    /**
     * 방 사이의 연결선 렌더링 (MapLoader 데이터 기반)
     */
    private void renderConnections(Graphics2D g2, int offsetX, int offsetY) {
        g2.setColor(CONNECTION_COLOR);
        g2.setStroke(new BasicStroke(CONNECTION_WIDTH));

        // 이미 그린 연결선 추적 (중복 방지)
        java.util.Set<String> drawnConnections = new java.util.HashSet<>();

        // 모든 방의 연결 정보를 순회하며 연결선 그리기
        for (Map.Entry<Integer, int[]> entry : roomPositions.entrySet()) {
            int roomId = entry.getKey();
            RoomData room = MapLoader.getRoom(roomId);

            if (room == null) continue;

            // 각 방의 연결된 방향을 확인
            for (Map.Entry<String, Integer> connection : room.getConnections().entrySet()) {
                String direction = connection.getKey();
                Integer connectedRoomId = connection.getValue();

                if (connectedRoomId == null) continue;

                // 연결선 ID 생성 (작은 번호를 앞에 배치하여 중복 방지)
                String connectionId = Math.min(roomId, connectedRoomId) + "-" + Math.max(roomId, connectedRoomId);

                // 이미 그린 연결선이면 스킵
                if (drawnConnections.contains(connectionId)) continue;

                // 연결선 그리기
                drawConnection(g2, offsetX, offsetY, roomId, connectedRoomId, direction);
                drawnConnections.add(connectionId);
            }
        }
    }

    /**
     * 두 방 사이의 연결선 그리기
     */
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
            // 세로 연결
            g2.drawLine(fromX, fromY, fromX, toY);
        } else {
            // 가로 연결
            g2.drawLine(fromX, fromY, toX, fromY);
        }
    }

    /**
     * 개별 방 렌더링
     */
    private void renderRoom(Graphics2D g2, int x, int y, int roomId, int currentRoomId) {
        // 현재 방인지 확인
        boolean isCurrent = (roomId == currentRoomId);

        // 방 타입 확인
        RoomData roomData = MapLoader.getRoom(roomId);
        String roomType = (roomData != null) ? roomData.getRoomType() : "NORMAL";

        // 방 채우기
        if (isCurrent) {
            g2.setColor(CURRENT_ROOM_COLOR);
        } else if ("KEY".equals(roomType)) {
            g2.setColor(KEY_ROOM_COLOR);
        } else {
            g2.setColor(VISITED_ROOM_COLOR);
        }
        g2.fillRect(x, y, ROOM_SIZE, ROOM_SIZE);

        // 방 테두리
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, ROOM_SIZE, ROOM_SIZE);

        // 방 번호 표시 (선택사항)
        if (isCurrent) {
            g2.setColor(Color.WHITE);
            g2.drawString(String.valueOf(roomId), x + ROOM_SIZE / 2 - 5, y + ROOM_SIZE / 2 + 5);
        }
    }
}
