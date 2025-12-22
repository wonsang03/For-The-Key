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
        // 디버깅용 메서드 - 필요시 사용
    }

    /**
     * 특정 스테이지를 로드
     * @param stageNumber 스테이지 번호 (1-5)
     */
    private void loadStage(int stageNumber) {
        // 스테이지 로드
        MapLoader.loadAllRooms(stageNumber);

        // 미니맵 재생성
        minimap = new Minimap();

        // 시작 방으로 이동
        currentRoom = MapLoader.getRoom(0);
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

        // 현재 스테이지 및 방 ID 표시
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("Stage " + MapLoader.getCurrentStage() + " - Room " + currentRoom.getRoomId(), 10, 30);

        // 조작 안내
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.drawString("Keys: 1-5 (Change Stage)  Q (Quit)", 10, 55);

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
                System.exit(0);
                break;
            case KeyEvent.VK_1:
            case KeyEvent.VK_2:
            case KeyEvent.VK_3:
            case KeyEvent.VK_4:
            case KeyEvent.VK_5:
                int stageNum = key - KeyEvent.VK_0;
                loadStage(stageNum);
                return;
        }

        // 해당 방향에 연결된 방이 있으면 이동
        if (direction != null && currentRoom.hasConnection(direction)) {
            Integer targetRoomId = currentRoom.getConnectedRoom(direction);
            if (targetRoomId != null) {
                RoomData nextRoom = MapLoader.getRoom(targetRoomId);
                if (nextRoom != null) {
                    currentRoom = nextRoom;
                    printCurrentRoom();
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}
