package map;

import game.Constants;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;

/**
 * 맵 테스트용 패널
 * 생성된 맵 구조를 화면에 표시
 */
public class MapTestPanel extends JPanel implements Runnable {

    // 맵 시스템
    private TileManager tileManager;
    private MapManager mapManager;

    // 게임 루프
    private Thread gameThread;
    private final int FPS = 60;

    public MapTestPanel() {
        // 패널 설정
        this.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);

        // 맵 초기화
        tileManager = new TileManager();
        mapManager = MapManager.createTestMap(); // 3개 방 생성

        // 콘솔에 맵 정보 출력
        System.out.println("\n========== 맵 생성 확인 ==========");
        for (int i = 0; i < 3; i++) {
            Room room = mapManager.changeRoom(i) ? mapManager.getCurrentRoom() : null;
            if (room != null) {
                room.printMap();
            }
        }
        mapManager.changeRoom(0); // 시작 방으로 돌아가기
        System.out.println("=================================\n");
    }

    /**
     * 게임 루프 시작
     */
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

    /**
     * 화면 그리기
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // 맵 렌더링
        Room currentRoom = mapManager.getCurrentRoom();
        tileManager.render(g2, currentRoom);

        // 디버그 정보 표시
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.drawString("Room: " + mapManager.getCurrentRoomId() + " (" + currentRoom.getType() + ")", 10, 20);
        g2.drawString("Size: " + currentRoom.getWidth() + "x" + currentRoom.getHeight(), 10, 40);

        g2.dispose();
    }
}
