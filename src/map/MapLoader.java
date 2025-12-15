package map;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// [서충만님 코드] 맵 데이터 파일을 로드하는 클래스
public class MapLoader {

    private static Map<Integer, RoomData> roomCache = new HashMap<>();
    private static int currentStage = 1;

    // [서충만님 코드] 특정 스테이지의 모든 방 데이터를 로드
    public static void loadAllRooms(int stageNumber) {
        roomCache.clear();
        currentStage = stageNumber;

        TileSprites.loadSprites(stageNumber);

        try {
            java.io.File file = new java.io.File("src/map/data/stage" + stageNumber + ".txt");
            if (!file.exists()) {
                file = new java.io.File("bin/map/data/stage" + stageNumber + ".txt");
            }
            if (!file.exists()) {
                System.err.println("stage" + stageNumber + ".txt 파일을 찾을 수 없습니다. 경로: " + file.getAbsolutePath());
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF-8"))) {
                String line;
                Integer currentRoomId = null;
                String currentRoomType = "NORMAL";
                List<String> mapLines = new ArrayList<>();
                List<String[]> connections = new ArrayList<>();

                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("#") || line.trim().isEmpty()) {
                        continue;
                    }

                    if (line.startsWith("@ROOM")) {
                        if (currentRoomId != null) {
                            saveRoom(currentRoomId, mapLines, connections, currentRoomType);
                        }

                        String[] parts = line.split("\\s+");
                        currentRoomId = Integer.parseInt(parts[1]);
                        currentRoomType = parts.length >= 3 ? parts[2] : "NORMAL";
                        mapLines.clear();
                        connections.clear();
                    }
                    else if (line.startsWith("@CONNECT")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 3) {
                            connections.add(new String[]{parts[1], parts[2]});
                        }
                    }
                    else if (line.startsWith("@")) {
                        continue;
                    }
                    else {
                        mapLines.add(line);
                    }
                }

                if (currentRoomId != null) {
                    saveRoom(currentRoomId, mapLines, connections, currentRoomType);
                }

                System.out.println("총 " + roomCache.size() + "개의 방이 로드되었습니다.");
            }
        } catch (Exception e) {
            System.err.println("맵 로드 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // [서충만님 코드] 방 데이터를 RoomData 객체로 변환하여 저장
    private static void saveRoom(int roomId, List<String> mapLines, List<String[]> connections, String roomType) {
        if (mapLines.isEmpty()) {
            System.err.println("Room " + roomId + ": 맵 데이터가 없습니다.");
            return;
        }

        int height = mapLines.size();
        int width = 0;
        for (String line : mapLines) {
            width = Math.max(width, line.length());
        }

        char[][] map = new char[height][width];

        for (int y = 0; y < height; y++) {
            String mapLine = mapLines.get(y);
            for (int x = 0; x < width; x++) {
                if (x < mapLine.length()) {
                    map[y][x] = mapLine.charAt(x);
                } else {
                    map[y][x] = '.';
                }
            }
        }

        RoomData roomData = new RoomData(roomId, map, roomType);

        for (String[] conn : connections) {
            String direction = conn[0];
            int targetRoomId = Integer.parseInt(conn[1]);
            roomData.addConnection(direction, targetRoomId);
        }

        roomCache.put(roomId, roomData);

        System.out.println("  Room " + roomId + " 로드됨: " + width + "x" + height);
    }

    public static void loadAllRooms() {
        loadAllRooms(1);
    }

    // [서충만님 코드] 특정 방 번호의 RoomData 가져오기
    public static RoomData getRoom(int roomId) {
        return roomCache.get(roomId);
    }

    // [서충만님 코드] 현재 로드된 스테이지 번호 반환
    public static int getCurrentStage() {
        return currentStage;
    }

    // [서충만님 코드] 맵을 콘솔에 출력 (디버깅용)
    public static void printMap(char[][] map) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                System.out.print(map[y][x]);
            }
            System.out.println();
        }
    }
}
