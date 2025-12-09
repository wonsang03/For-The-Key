package map;

import common.Constants;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;

/**
 * 맵 테스트용 패널
 */
public class MapTestPanel extends JPanel implements Runnable, KeyListener {

    private TileManager tileManager;
    private RoomData currentRoom;
    private Minimap minimap;

    private Thread gameThread;
    private final int FPS = 60;

    public MapTestPanel() {
        this.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(this);

        // 초기화
        tileManager = new TileManager();

        // 모든 방 로드 (먼저 로드!)
        MapLoader.loadAllRooms();

        // 미니맵 생성 (방 데이터 로드 후)
        minimap = new Minimap();

        // 시작 방 (Room 0)
        currentRoom = MapLoader.getRoom(0);

        // 콘솔에 맵 출력
        printCurrentRoom();
    }

    private void printCurrentRoom() {
        System.out.println("\n========== Room " + currentRoom.getRoomId() + " ==========");
        MapLoader.printMap(currentRoom.getMap());
        System.out.println("연결된 방:");
        for (Map.Entry<String, Integer> entry : currentRoom.getConnections().entrySet()) {
            System.out.println("  " + entry.getKey() + " -> Room " + entry.getValue());
        }
        System.out.println("================================\n");
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                repaint();
                delta--;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 맵 렌더링
        tileManager.render(g2, currentRoom.getMap());

        // 미니맵 렌더링
        minimap.render(g2, currentRoom.getRoomId());

        // 현재 방 ID 표시
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("Room " + currentRoom.getRoomId(), 10, 30);

        g2.dispose();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        String direction = null;

        switch(key) {
            case KeyEvent.VK_UP:
                direction = "NORTH";
                break;
            case KeyEvent.VK_DOWN:
                direction = "SOUTH";
                break;
            case KeyEvent.VK_LEFT:
                direction = "WEST";
                break;
            case KeyEvent.VK_RIGHT:
                direction = "EAST";
                break;
            case KeyEvent.VK_Q:
                System.out.println("테스트 종료");
                System.exit(0);
                break;
        }

        // 해당 방향에 연결된 방이 있으면 이동
        if (direction != null && currentRoom.hasConnection(direction)) {
            Integer targetRoomId = currentRoom.getConnectedRoom(direction);
            if (targetRoomId != null) {
                RoomData nextRoom = MapLoader.getRoom(targetRoomId);
                if (nextRoom != null) {
                    currentRoom = nextRoom;
                    System.out.println("→ " + direction + " 방향으로 Room " + targetRoomId + "로 이동");
                    printCurrentRoom();
                }
            }
        } else if (direction != null) {
            System.out.println("✗ " + direction + " 방향에는 연결된 방이 없습니다.");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}
