package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import common.Constants;
import enemy.Enemy;
import enemy.EnemyType;

public class GamePanel extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener { 

    Thread gameThread;
    final int FPS = Constants.FPS;

    // 플레이어 관련
    private Player player;
    private WeaponType currentWeapon = WeaponType.PISTOL;
    
    // 게임 오브젝트
    private ArrayList<enemy.Enemy> enemies = new ArrayList<>();
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<ItemType> acquiredItems = new ArrayList<>();
    private ArrayList<DamageText> damageTexts = new ArrayList<>();

    // 입력 관련
    private boolean keyW, keyA, keyS, keyD;
    private int mouseX = Constants.WINDOW_WIDTH / 2;
    private int mouseY = Constants.WINDOW_HEIGHT / 2;
    private long lastShootTime = 0;
    
    // 카메라 관련
    public double cameraX = 0;
    public double cameraY = 0;
    private final double CAMERA_LERP = 0.05;
    
    // 적 스폰 관련
    private long lastSpawnTime = 0;
    private long spawnInterval = 3000; // 3초마다 생성
    private int maxEnemies = 5;
    
    public GamePanel() {
        this.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(this); 
        this.setFocusable(true);
        addMouseMotionListener(this);
        addMouseListener(this);

        setupGame();
    }

    public void setupGame() {
        // 플레이어 초기화
        player = new Player(300, 300);
        
        // 카메라 초기화
        cameraX = player.getX() - Constants.WINDOW_WIDTH / 2.0;
        cameraY = player.getY() - Constants.WINDOW_HEIGHT / 2.0;
        
        // 초기 적 생성
        enemies.add(new Enemy(EnemyType.MUDGOLEM, 1000, 200));
        
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
        // 1. 플레이어 이동 처리
        player.move(keyW, keyS, keyA, keyD);
        
        // 플레이어의 크기(40x40, 반지름 20)를 고려하여 맵 경계를 설정합니다.
        final int playerRadius = 20;
        double playerX = player.getX();
        double playerY = player.getY();
        
        // X 경계
        if (playerX < playerRadius) {
            player.setX(playerRadius);
            playerX = playerRadius;
        }
        if (playerX > Constants.WORLD_WIDTH - playerRadius) {
            player.setX(Constants.WORLD_WIDTH - playerRadius);
            playerX = Constants.WORLD_WIDTH - playerRadius;
        }

        // Y 경계
        if (playerY < playerRadius) {
            player.setY(playerRadius);
            playerY = playerRadius;
        }
        if (playerY > Constants.WORLD_HEIGHT - playerRadius) {
            player.setY(Constants.WORLD_HEIGHT - playerRadius);
            playerY = Constants.WORLD_HEIGHT - playerRadius;
        }

        // 2. 부드러운 카메라 이동 (LERP)
        double targetCameraX = playerX - Constants.WINDOW_WIDTH / 2.0;
        double targetCameraY = playerY - Constants.WINDOW_HEIGHT / 2.0;
        
        // 카메라의 현재 위치를 목표 위치로 CAMERA_LERP만큼 천천히 이동
        cameraX += (targetCameraX - cameraX) * CAMERA_LERP;
        cameraY += (targetCameraY - cameraY) * CAMERA_LERP;
        
        // 3. 적 스폰 로직
        long now = System.currentTimeMillis();
        if (now - lastSpawnTime > spawnInterval && enemies.size() < maxEnemies) {
            spawnEnemy();
            lastSpawnTime = now;
        }
        
        // 4. 적 업데이트 
        for (Enemy enemy : enemies) {
            enemy.update((int)playerX, (int)playerY); 
        }
        
        // 5. 죽은 적 처리 및 MAGMA_SLIME_BIG 분열 처리
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
                
                // 아이템 드롭
                if (Math.random() < 0.6) {
                    ItemType drop = ItemType.getRandom();
                    items.add(new Item(enemy.x, enemy.y, drop));
                }
                
                enemiesToRemove.add(enemy);
            }
        }
        
        // 죽은 적 제거
        enemies.removeAll(enemiesToRemove);
        // 새로 생성된 적 추가
        enemies.addAll(enemiesToAdd);
        
        // 6. 총알 업데이트
        bullets.removeIf(b -> { b.update(); return !b.isActive(); });
        
        // 7. 충돌 감지 (총알-적)
        checkBulletCollisions();
        
        // 8. 플레이어-적 충돌 감지 및 밀어내기
        final double pushBackSpeed = 3.0;
        
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
                    
                    player.setX(playerX + pushX);
                    player.setY(playerY + pushY);
                }
            }
        }
        
        // 9. 아이템 획득 체크
        checkItemPickups();
        
        // 10. 데미지 텍스트 업데이트
        damageTexts.removeIf(dt -> {
            dt.update();
            return dt.isExpired();
        });
    }
    
    /**
     * 🎲 랜덤 위치로 적 생성
     */
    private void spawnEnemy() {
        int margin = 100; // 가장자리로부터 거리
        int side = (int)(Math.random() * 4); // 0:상, 1:하, 2:좌, 3:우
        double x = 0, y = 0;
        double playerX = player.getX();
        double playerY = player.getY();

        // 플레이어 주변에 스폰
        switch (side) {
            case 0: // 위쪽
                x = playerX + (Math.random() - 0.5) * Constants.WINDOW_WIDTH;
                y = playerY - Constants.WINDOW_HEIGHT / 2 - margin;
                break;
            case 1: // 아래쪽
                x = playerX + (Math.random() - 0.5) * Constants.WINDOW_WIDTH;
                y = playerY + Constants.WINDOW_HEIGHT / 2 + margin;
                break;
            case 2: // 왼쪽
                x = playerX - Constants.WINDOW_WIDTH / 2 - margin;
                y = playerY + (Math.random() - 0.5) * Constants.WINDOW_HEIGHT;
                break;
            case 3: // 오른쪽
                x = playerX + Constants.WINDOW_WIDTH / 2 + margin;
                y = playerY + (Math.random() - 0.5) * Constants.WINDOW_HEIGHT;
                break;
        }
        
        // 월드 경계 체크
        x = Math.max(50, Math.min(Constants.WORLD_WIDTH - 50, x));
        y = Math.max(50, Math.min(Constants.WORLD_HEIGHT - 50, y));

        enemies.add(new Enemy(EnemyType.MUDGOLEM, x, y));
        System.out.println("👾 적 스폰: (" + (int)x + ", " + (int)y + ")");
    }
    
    private void shoot() {
        long now = System.currentTimeMillis();
        long delay = (long)(currentWeapon.getAttackSpeed() * 1000 / (1 + player.getAttackSpeedBonus()));
        if (now - lastShootTime < delay) return;
        lastShootTime = now;

        // 마우스 위치를 월드 좌표로 변환
        double worldMouseX = mouseX + cameraX;
        double worldMouseY = mouseY + cameraY;
        
        double px = player.getX() + 20;
        double py = player.getY() + 25;
        double angle = Math.atan2(worldMouseY - py, worldMouseX - px);
        double bulletSpeed = 10;

        if (currentWeapon == WeaponType.SHOTGUN) {
            int pellets = 5;
            double spread = Math.toRadians(15);
            double start = angle - spread / 2;
            double step = spread / (pellets - 1);
            for (int i = 0; i < pellets; i++) {
                double a = start + step * i;
                bullets.add(new Bullet(px, py, a, bulletSpeed, currentWeapon.getDamage(), currentWeapon.getRange()));
            }
        } else {
            bullets.add(new Bullet(px, py, angle, bulletSpeed, currentWeapon.getDamage(), currentWeapon.getRange()));
        }
    }
    
    private void checkBulletCollisions() {
        for (Bullet b : bullets) {
            if (!b.isActive()) continue;
            for (Enemy e : enemies) {
                if (!e.alive) continue;
                
                // 스프라이트 중심 좌표 계산
                double drawY_world = e.y - (e.hitHeight - 48);
                double spriteCenterX = e.x + (e.drawWidth / 2.0);
                double spriteCenterY = drawY_world + (e.drawHeight / 2.0);
                
                // 히트박스 영역
                double enemyLeft = spriteCenterX - (e.hitWidth / 2.0);
                double enemyRight = spriteCenterX + (e.hitWidth / 2.0);
                double enemyTop = spriteCenterY - (e.hitHeight / 2.0);
                double enemyBottom = spriteCenterY + (e.hitHeight / 2.0);
                
                // 총알과 적의 충돌 검사
                if (b.getX() >= enemyLeft && b.getX() <= enemyRight &&
                    b.getY() >= enemyTop && b.getY() <= enemyBottom) {
                    double dmg = currentWeapon.getDamage() * player.getAttackMultiplier();
                    e.takeDamage((int)dmg);
                    b.deactivate();

                    Color dmgColor = dmg >= 50 ? Color.RED : Color.YELLOW;
                    damageTexts.add(new DamageText(spriteCenterX, spriteCenterY - 10,
                            String.valueOf((int)dmg), dmgColor));
                }
            }
        }
    }
    
    private void checkItemPickups() {
        double playerX = player.getX();
        double playerY = player.getY();
        Rectangle playerRect = new Rectangle((int)playerX - 20, (int)playerY - 20, 40, 40);
        for (Item item : items) {
            if (!item.isPicked() && playerRect.intersects(item.getBounds())) {
                item.pickUp();
                acquiredItems.add(item.getType());
                applyItemEffect(item.getType());
            }
        }
    }

    private void applyItemEffect(ItemType type) {
        if (type == null) return;

        if (type.getAttackBuff() != 0) player.addAttackBonus(type.getAttackBuff());
        if (type.getHpBuff() != 0) player.addMaxHP(type.getHpBuff());
        if (type.getSpeedBuff() != 0) player.addSpeedBonus(type.getSpeedBuff());
        if (type.getAttackSpeedBuff() != 0) player.addAttackSpeedBonus(type.getAttackSpeedBuff());

        if (type == ItemType.RED_POTION) player.heal(30);
        if (type == ItemType.ELIXIR) player.heal(player.getMaxHP());

        damageTexts.add(new DamageText(player.getX(), player.getY() - 20,
                "+" + type.getName(), Color.CYAN));
    }
    
    private void changeWeapon(boolean next) {
        WeaponType[] weapons = WeaponType.values();
        int currentIdx = -1;
        for (int i = 0; i < weapons.length; i++) {
            if (weapons[i] == currentWeapon) {
                currentIdx = i;
                break;
            }
        }
        if (currentIdx == -1) return;
        
        if (next) {
            currentIdx = (currentIdx + 1) % weapons.length;
        } else {
            currentIdx = (currentIdx - 1 + weapons.length) % weapons.length;
        }
        currentWeapon = weapons[currentIdx];
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 적 그리기 (카메라 오프셋 적용)
        for (Enemy enemy : enemies) {
            enemy.draw(g2, (int)cameraX, (int)cameraY); 
        }

        // 총알 그리기 (카메라 오프셋 적용)
        for (Bullet bullet : bullets) {
            int screenX = (int)bullet.getX() - (int)cameraX;
            int screenY = (int)bullet.getY() - (int)cameraY;
            if (screenX >= -10 && screenX <= Constants.WINDOW_WIDTH + 10 &&
                screenY >= -10 && screenY <= Constants.WINDOW_HEIGHT + 10) {
                Graphics2D g2Copy = (Graphics2D) g2.create();
                g2Copy.translate(-(int)cameraX, -(int)cameraY);
                bullet.draw(g2Copy);
                g2Copy.dispose();
            }
        }
        
        // 아이템 그리기 (카메라 오프셋 적용)
        for (Item item : items) {
            Rectangle bounds = item.getBounds();
            int screenX = (int)bounds.getX() - (int)cameraX;
            int screenY = (int)bounds.getY() - (int)cameraY;
            if (screenX >= -25 && screenX <= Constants.WINDOW_WIDTH + 25 &&
                screenY >= -25 && screenY <= Constants.WINDOW_HEIGHT + 25) {
                Graphics2D g2Copy = (Graphics2D) g2.create();
                g2Copy.translate(-(int)cameraX, -(int)cameraY);
                item.draw(g2Copy);
                g2Copy.dispose();
            }
        }
        
        // 플레이어 그리기 (화면 중앙에 고정)
        int screenPlayerX = Constants.WINDOW_WIDTH / 2 - 20;
        int screenPlayerY = Constants.WINDOW_HEIGHT / 2 - 20;
        Graphics2D g2Player = (Graphics2D) g2.create();
        g2Player.translate(screenPlayerX - player.getX(), screenPlayerY - player.getY());
        player.draw(g2Player);
        g2Player.dispose();
        
        // 데미지 텍스트 그리기 (카메라 오프셋 적용)
        for (DamageText dt : damageTexts) {
            Graphics2D g2Copy = (Graphics2D) g2.create();
            g2Copy.translate(-(int)cameraX, -(int)cameraY);
            dt.draw(g2Copy);
            g2Copy.dispose();
        }

        // HUD 그리기 (카메라와 무관하게 화면에 고정)
        drawPlayerHUD(g2);
        
        g2.dispose();
    }

    private void drawPlayerHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(20, 20, 330, 160, 15, 15);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        g2.drawString("❤️ HP: " + player.getHP() + " / " + player.getMaxHP(), 40, 50);

        g2.drawString("🔫 Weapon: " + currentWeapon.getName(), 40, 75);
        g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        g2.drawString(String.format(" - Damage: %.0f | Range: %.0fpx | Speed: %.2fs",
                currentWeapon.getDamage(), currentWeapon.getRange(), currentWeapon.getAttackSpeed()), 55, 95);

        g2.drawString(String.format("⚔️ ATK Multiplier: x%.2f", player.getAttackMultiplier()), 40, 120);
        g2.drawString(String.format("💨 Move Speed: %.2f", player.getMoveSpeed()), 40, 140);
        g2.drawString(String.format("⚡ Attack Speed Bonus: +%.2f", player.getAttackSpeedBonus()), 40, 160);
    }

    // KeyListener 메소드
    @Override
    public void keyTyped(KeyEvent e) {
        // 사용하지 않음
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        if (code == KeyEvent.VK_W) keyW = true;
        if (code == KeyEvent.VK_S) keyS = true;
        if (code == KeyEvent.VK_A) keyA = true;
        if (code == KeyEvent.VK_D) keyD = true;
        if (code == KeyEvent.VK_Q) changeWeapon(false);
        if (code == KeyEvent.VK_E) changeWeapon(true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        
        if (code == KeyEvent.VK_W) keyW = false;
        if (code == KeyEvent.VK_S) keyS = false;
        if (code == KeyEvent.VK_A) keyA = false;
        if (code == KeyEvent.VK_D) keyD = false;
    }
    
    // MouseMotionListener 메소드
    @Override
    public void mouseMoved(MouseEvent e) { 
        mouseX = e.getX(); 
        mouseY = e.getY(); 
    }
    
    @Override
    public void mouseDragged(MouseEvent e) { 
        mouseMoved(e); 
    }
    
    // MouseListener 메소드
    @Override
    public void mousePressed(MouseEvent e) { 
        if (SwingUtilities.isLeftMouseButton(e)) shoot(); 
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    @Override
    public void mouseClicked(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
}
