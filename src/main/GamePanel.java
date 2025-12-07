package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

import common.Constants;
import player.KeyHandler;
import player.Player;
import ui.UIRenderer;

// Runnable 추가 -> 게임이 계속 돌아감 (게임 루프)
public class GamePanel extends JPanel implements Runnable {

    private static final long serialVersionUID = 1L;

    final int screenWidth = Constants.WINDOW_WIDTH;
    final int screenHeight = Constants.WINDOW_HEIGHT;
    Thread gameThread;
    
    KeyHandler keyH = new KeyHandler();
    Player player = new Player(this, keyH);
    
    // UI 객체 생성
    public UIRenderer ui = new UIRenderer(this);

    public GamePanel() {
        // 1. 패널 크기 설정
        this.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        
        // 2. 배경색 검은색
        this.setBackground(Color.BLACK);
        
        // 3. 성능 설정
        this.setDoubleBuffered(true);
        this.setFocusable(true);

        // 4. 키보드 입력 받기
        this.addKeyListener(keyH);
    }

    // 게임 엔진 시작 함수
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        // 게임 속도 조절 (60 FPS)
        double drawInterval = 1000000000 / Constants.FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        // 플레이어 움직임 업데이트
        player.update();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 플레이어 그리기
        player.draw(g2);
        
        // UI 그리기 (플레이어보다 나중에 그려야 화면 맨 위에 뜸)
        ui.draw(g2);

        g2.dispose();
    }
}