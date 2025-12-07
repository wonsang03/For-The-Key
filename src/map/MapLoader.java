package map;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 맵 데이터 파일을 로드하는 클래스
 */
public class MapLoader {

    private static Map<Integer, RoomData> roomCache = new HashMap<>();

    /**
     * stage1.txt의 모든 방 데이터를 로드
     */
    public static void loadAllRooms() {
        roomCache.clear();

        try {
            // 파일 시스템에서 직접 읽기
            java.io.File file = new java.io.File("src/map/data/stage1.txt");
            if (!file.exists()) {
                // bin 폴더에서도 시도
                file = new java.io.File("bin/map/data/stage1.txt");
            }
            if (!file.exists()) {
                System.err.println("stage1.txt 파일을 찾을 수 없습니다. 경로: " + file.getAbsolutePath());
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF-8"))) {
                String line;
                Integer currentRoomId = null;
                List<String> mapLines = new ArrayList<>();
                List<String[]> connections = new ArrayList<>();

                while ((line = reader.readLine()) != null) {
                    // 주석 및 빈 줄 무시
                    if (line.trim().startsWith("#") || line.trim().isEmpty()) {
                        continue;
                    }

                    // @ROOM 태그
                    if (line.startsWith("@ROOM")) {
                        // 이전 방 저장
                        if (currentRoomId != null) {
                            saveRoom(currentRoomId, mapLines, connections);
                        }

                        // 새 방 시작
                        String[] parts = line.split("\\s+");
                        currentRoomId = Integer.parseInt(parts[1]);
                        mapLines.clear();
                        connections.clear();
                    }
                    // @CONNECT 태그
                    else if (line.startsWith("@CONNECT")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 3) {
                            connections.add(new String[]{parts[1], parts[2]});
                        }
                    }
                    // @STAGE 등 다른 태그는 무시
                    else if (line.startsWith("@")) {
                        continue;
                    }
                    // 맵 데이터
                    else {
                        mapLines.add(line);
                    }
                }

                // 마지막 방 저장
                if (currentRoomId != null) {
                    saveRoom(currentRoomId, mapLines, connections);
                }

                System.out.println("총 " + roomCache.size() + "개의 방이 로드되었습니다.");
            }
        } catch (Exception e) {
            System.err.println("맵 로드 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 방 데이터를 RoomData 객체로 변환하여 저장
     */
    private static void saveRoom(int roomId, List<String> mapLines, List<String[]> connections) {
        char[][] map = new char[12][20];

        // 맵 데이터 변환
        for (int y = 0; y < 12 && y < mapLines.size(); y++) {
            String mapLine = mapLines.get(y);
            for (int x = 0; x < 20 && x < mapLine.length(); x++) {
                map[y][x] = mapLine.charAt(x);
            }
            // 부족한 부분은 바닥으로 채움
            for (int x = mapLine.length(); x < 20; x++) {
                map[y][x] = '.';
            }
        }

        // 부족한 줄은 바닥으로 채움
        for (int y = mapLines.size(); y < 12; y++) {
            for (int x = 0; x < 20; x++) {
                map[y][x] = '.';
            }
        }

        RoomData roomData = new RoomData(roomId, map);

        // 연결 정보 추가
        for (String[] conn : connections) {
            String direction = conn[0];
            int targetRoomId = Integer.parseInt(conn[1]);
            roomData.addConnection(direction, targetRoomId);
        }

        roomCache.put(roomId, roomData);
    }

    /**
     * 특정 방 번호의 RoomData 가져오기
     */
    public static RoomData getRoom(int roomId) {
        return roomCache.get(roomId);
    }

    /**
     * 맵을 콘솔에 출력 (디버깅용)
     */
    public static void printMap(char[][] map) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                System.out.print(map[y][x]);
            }
            System.out.println();
        }
    }
}
