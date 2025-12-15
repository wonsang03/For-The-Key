package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.JPanel;

import enemy.Enemy;
import enemy.EnemyType;

public class GamePanel extends JPanel implements Runnable, KeyListener { 

    Thread gameThread;
    final int FPS = 60;

    ArrayList<Enemy> enemies = new ArrayList<>();

    public double playerX = 100; 
    public double playerY = 100;
    
    private final double playerSpeed = 5.0; 
    
    private boolean keyW, keyA, keyS, keyD;
    
    public double cameraX = 0;
    public double cameraY = 0;
    
    private final double CAMERA_LERP = 0.05; 
    
    public GamePanel() {
        this.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(this); 
        this.setFocusable(true);

        setupGame();
    }

    public void setupGame() {
        playerX = 300; 
        playerY = 300;
        
        cameraX = playerX - Constants.WINDOW_WIDTH / 2.0;
        cameraY = playerY - Constants.WINDOW_HEIGHT / 2.0;
        
        //enemies.add(new Enemy(EnemyType.SLIME, 100, 200));
        //enemies.add(new Enemy(EnemyType.MAGMA_SLIME_BIG, 200, 200));
        //enemies.add(new Enemy(EnemyType.WOLF, 300, 200));
        //enemies.add(new Enemy(EnemyType.ORC, 400, 200));
        //enemies.add(new Enemy(EnemyType.SPORE_FLOWER, 400, 200));
        //enemies.add(new Enemy(EnemyType.GOLEM, 500, 200));
        //enemies.add(new Enemy(EnemyType.CROCODILE, 500, 200));
        //enemies.add(new Enemy(EnemyType.YETI, 600, 200));
        //enemies.add(new Enemy(EnemyType.FROZEN_KNIGHT, 700, 200));
        //enemies.add(new Enemy(EnemyType.SNOW_MAGE, 800, 200));
        //enemies.add(new Enemy(EnemyType.ICE_GOLEM, 900, 200));
        enemies.add(new Enemy(EnemyType.MUDGOLEM, 1000, 200));
        //enemies.add(new Enemy(EnemyType.BOMB_SKULL, 100, 400));
        //enemies.add(new Enemy(EnemyType.HELL_HOUND, 1000, 400));
        //enemies.add(new Enemy(EnemyType.FIRE_IMP, 200, 600));
        //enemies.add(new Enemy(EnemyType.HELL_KNIGHT, 400, 600));
        //enemies.add(new Enemy(EnemyType.GOBLIN, 600, 600));
        //enemies.add(new Enemy(EnemyType.MAGMA_SLIME_SMALL, 100, 200));
        //enemies.add(new Enemy(EnemyType.MAGMA_SLIME_BIG, 100, 200));
        startGameThread();
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
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
        // 1. WASD 키 입력에 따른 플레이어 이동 처리
        if (keyW) playerY -= playerSpeed;
        if (keyS) playerY += playerSpeed;
        if (keyA) playerX -= playerSpeed;
        if (keyD) playerX += playerSpeed;
        
        // 플레이어의 크기(32x32, 반지름 16)를 고려하여 맵 경계를 설정합니다.
        final int playerRadius = 16; 
        
        // X 경계
        if (playerX < playerRadius) {
            playerX = playerRadius;
        }
        if (playerX > Constants.WORLD_WIDTH - playerRadius) {
            playerX = Constants.WORLD_WIDTH - playerRadius;
        }

        // Y 경계
        if (playerY < playerRadius) {
            playerY = playerRadius;
        }
        if (playerY > Constants.WORLD_HEIGHT - playerRadius) {
            playerY = Constants.WORLD_HEIGHT - playerRadius;
        }
        // ★★★ [경계 제한 종료] ★★★


        // 2. 부드러운 카메라 이동 (LERP)
        double targetCameraX = playerX - Constants.WINDOW_WIDTH / 2.0;
        double targetCameraY = playerY - Constants.WINDOW_HEIGHT / 2.0;
        
        // 카메라의 현재 위치를 목표 위치로 CAMERA_LERP만큼 천천히 이동
        cameraX += (targetCameraX - cameraX) * CAMERA_LERP;
        cameraY += (targetCameraY - cameraY) * CAMERA_LERP;
        
        
        // 3. 적 업데이트 
        for (Enemy enemy : enemies) {
            enemy.update((int)playerX, (int)playerY); 
        }
        
        // 4. 죽은 적 처리 및 MAGMA_SLIME_BIG 분열 처리
        ArrayList<Enemy> enemiesToRemove = new ArrayList<>();
        ArrayList<Enemy> enemiesToAdd = new ArrayList<>();
        
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                // MAGMA_SLIME_BIG이 죽으면 MAGMA_SLIME_SMALL 2마리 생성
                if (enemy.type == EnemyType.MAGMA_SLIME_BIG) {
                    // 원래 위치에서 약간 떨어진 위치에 2마리 생성
                    double offsetX1 = enemy.x - 30;
                    double offsetY1 = enemy.y - 30;
                    double offsetX2 = enemy.x + 30;
                    double offsetY2 = enemy.y + 30;
                    
                    enemiesToAdd.add(new Enemy(EnemyType.MAGMA_SLIME_SMALL, offsetX1, offsetY1));
                    enemiesToAdd.add(new Enemy(EnemyType.MAGMA_SLIME_SMALL, offsetX2, offsetY2));
                }
                enemiesToRemove.add(enemy);
            }
        }
        
        // 죽은 적 제거
        enemies.removeAll(enemiesToRemove);
        // 새로 생성된 적 추가
        enemies.addAll(enemiesToAdd);
        
        // [플레이어-적 충돌 감지 및 밀어내기] 
        final double pushBackSpeed = 3.0; // 밀어내기 속도 (조절 가능: 값이 클수록 더 강하게 밀어냄)
        
        for (Enemy enemy : enemies) {
            if (!enemy.alive) continue;
            
            // 스프라이트 중심 좌표 계산 (월드 좌표 기준)
            double drawY_world = enemy.y - (enemy.hitHeight - 48);
            double spriteCenterX = enemy.x + (enemy.drawWidth / 2.0);
            double spriteCenterY = drawY_world + (enemy.drawHeight / 2.0);
            
            // 히트박스는 스프라이트 중심에 맞춰서 그려지므로, 히트박스 영역 계산
            double enemyLeft = spriteCenterX - (enemy.hitWidth / 2.0);
            double enemyRight = spriteCenterX + (enemy.hitWidth / 2.0);
            double enemyTop = spriteCenterY - (enemy.hitHeight / 2.0);
            double enemyBottom = spriteCenterY + (enemy.hitHeight / 2.0);
            
            // 플레이어의 히트박스 영역 계산
            double playerLeft = playerX - playerRadius;
            double playerRight = playerX + playerRadius;
            double playerTop = playerY - playerRadius;
            double playerBottom = playerY + playerRadius;
            
            // 충돌 감지: AABB (Axis-Aligned Bounding Box) 충돌 검사
            boolean isColliding = (playerRight > enemyLeft && playerLeft < enemyRight &&
                                   playerBottom > enemyTop && playerTop < enemyBottom);
            
            if (isColliding) {
                // 플레이어와 적의 중심점(스프라이트 중심) 사이의 거리와 방향 계산
                double dx = playerX - spriteCenterX;
                double dy = playerY - spriteCenterY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                // 거리가 0이면 방향을 계산할 수 없으므로 기본 방향 사용
                if (distance > 0) {
                    // 정규화된 방향 벡터로 플레이어를 밀어냄
                    double pushX = (dx / distance) * pushBackSpeed;
                    double pushY = (dy / distance) * pushBackSpeed;
                    
                    playerX += pushX;
                    playerY += pushY;
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // ★ [적 그리기] 적 그리기 시 카메라 오프셋을 전달
        for (Enemy enemy : enemies) {
            enemy.draw(g2, (int)cameraX, (int)cameraY); 
        }

        // ----------------------------------------------------------------------
        // ★ [플레이어 그리기] (임시)
        // ----------------------------------------------------------------------
        // 플레이어의 화면 좌표: 월드 좌표 - 카메라 오프셋
        int screenPlayerX = (int)playerX - (int)cameraX;
        int screenPlayerY = (int)playerY - (int)cameraY;
        
        g2.setColor(Color.BLUE);
        // 플레이어를 화면 중앙에 그립니다.
        g2.fillRect(screenPlayerX - 16, screenPlayerY - 16, 32, 32); 
        // ----------------------------------------------------------------------

        g2.dispose();
    }

    // ★ [KeyListener 메소드]
    @Override
    public void keyTyped(KeyEvent e) {
        // 사용하지 않음
    }

    // 키가 눌렸을 때 해당 키 플래그를 true로 설정
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        if (code == KeyEvent.VK_W) keyW = true;
        if (code == KeyEvent.VK_S) keyS = true;
        if (code == KeyEvent.VK_A) keyA = true;
        if (code == KeyEvent.VK_D) keyD = true;
    }

    // 키가 놓였을 때 해당 키 플래그를 false로 설정
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        
        if (code == KeyEvent.VK_W) keyW = false;
        if (code == KeyEvent.VK_S) keyS = false;
        if (code == KeyEvent.VK_A) keyA = false;
        if (code == KeyEvent.VK_D) keyD = false;
    }
}