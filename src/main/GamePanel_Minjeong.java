package main;

import java.awt.Color;
import java.awt.Dimension;
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
// [김민정님 코드] 플레이어 시스템
import player.Player;
import ui.UIRenderer;
import player.KeyHandler;
// [김선욱님 코드] 전투 시스템
import item.Bullet;
import item.Item;
import item.ItemType;
import item.DamageText;
import item.WeaponType;
// [서충만님 코드] 맵 시스템
import map.TileManager;
import map.MapLoader;
import map.RoomData;
import map.TileType;

public class GamePanel_Minjeong extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener { 

    private static final long serialVersionUID = 1L;
	Thread gameThread;
    final int FPS = Constants.FPS;

    // [민정님 추가] UI 렌더러 및 게임 상태 관리
    public UIRenderer ui = new UIRenderer(this);
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int gameOverState = 2;

    // [김민정님 코드] 플레이어 및 KeyHandler
    public Player player; // UIRenderer 접근을 위해 public으로 변경 권장, 혹은 getter 사용
    private KeyHandler keyH = new KeyHandler();
    
    // [김선욱님 코드] 무기 시스템
    private WeaponType currentWeapon = WeaponType.PISTOL;
    
    // [서상원님 코드] 적 시스템
    public ArrayList<enemy.Enemy> enemies = new ArrayList<>();
    
    // [김선욱님 코드] 전투 시스템 리스트
    private ArrayList<Bullet> bullets = new ArrayList<>(); 
    private ArrayList<Item> items = new ArrayList<>(); 
    private ArrayList<ItemType> acquiredItems = new ArrayList<>(); 
    private ArrayList<DamageText> damageTexts = new ArrayList<>(); 

    private boolean keyW, keyA, keyS, keyD; 
    
    // [김선욱님 코드] 마우스 입력
    private int mouseX = Constants.WINDOW_WIDTH / 2; 
    private int mouseY = Constants.WINDOW_HEIGHT / 2; 
    private long lastShootTime = 0; 
    
    // [서상원님 코드] 카메라 시스템
    public double cameraX = 0; 
    public double cameraY = 0; 
    private final double CAMERA_LERP = 0.05; 
    
    // [김선욱님 코드] 적 스폰 시스템
    private long lastSpawnTime = 0; 
    private long spawnInterval = 3000; 
    private int maxEnemies = 5; 
    
    // [서충만님 코드] 맵 타일 및 방 관리
    public RoomData currentRoom; // UIRenderer 접근을 위해 public 권장
    private TileManager tileManager;

    // 생성자
    public GamePanel_Minjeong() {
        this.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(this);
        this.setFocusable(true);

        // 리스너 추가
        this.addKeyListener(keyH);
        addMouseMotionListener(this);
        addMouseListener(this);

        setupGame();
    }

    // 게임 초기화
    public void setupGame() {
        // [서충만님 코드] 맵 초기화
        tileManager = new TileManager();
        MapLoader.loadAllRooms();
        currentRoom = MapLoader.getRoom(0);
        
        // [김민정님 코드] 플레이어 초기화
        player = new Player(this, keyH);
        player.x = Constants.TILE_SIZE * 10;
        player.y = Constants.TILE_SIZE * 6;
        
        // [서상원님 코드] 카메라 초기화
        cameraX = player.x - Constants.WINDOW_WIDTH / 2.0;
        cameraY = player.y - Constants.WINDOW_HEIGHT / 2.0;
        
        // [서상원님 코드] 초기 적 생성
        enemies.clear(); // 재시작 시 기존 적 삭제
        enemies.add(new Enemy(EnemyType.MUDGOLEM, 1000, 200));
        
        // 리스트 초기화 (재시작 대비)
        bullets.clear();
        items.clear();
        damageTexts.clear();

        // [민정님 추가] 게임 시작 시 타이틀 화면 상태로 설정
        gameState = titleState; 
        
        startGameThread();
    }

    public void startGameThread() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
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

    // 게임 상태 업데이트
    public void update() {
        // [민정님 추가] 플레이 상태가 아니면(타이틀, 게임오버 등) 게임 로직 멈춤
        if (gameState != playState) {
            return;
        }

        // [김민정님 코드] 플레이어 이동 및 충돌 체크
        double oldX = player.x;
        double oldY = player.y;
        player.update();
        
        final int playerRadius = Constants.TILE_SIZE / 2;
        double playerX = player.x;
        double playerY = player.y;
        
        // 맵 충돌 체크
        if (currentRoom != null) {
            char[][] map = currentRoom.getMap();
            int mapWidth = map[0].length;
            int mapHeight = map.length;
            
            int tileX = (int)(playerX / Constants.TILE_SIZE);
            int tileY = (int)(playerY / Constants.TILE_SIZE);
            
            if (tileX < 0) tileX = 0;
            if (tileX >= mapWidth) tileX = mapWidth - 1;
            if (tileY < 0) tileY = 0;
            if (tileY >= mapHeight) tileY = mapHeight - 1;
            
            char currentTile = map[tileY][tileX];
            TileType tileType = TileType.fromSymbol(currentTile);
            
            if (tileType.isSolid()) {
                player.x = (int)oldX;
                player.y = (int)oldY;
                playerX = oldX;
                playerY = oldY;
            } else {
                checkDoorCollision(tileX, tileY, map);
            }
            
            // 맵 경계 체크
            if (playerX < playerRadius) player.x = playerRadius;
            if (playerX > mapWidth * Constants.TILE_SIZE - playerRadius) player.x = mapWidth * Constants.TILE_SIZE - playerRadius;
            if (playerY < playerRadius) player.y = playerRadius;
            if (playerY > mapHeight * Constants.TILE_SIZE - playerRadius) player.y = mapHeight * Constants.TILE_SIZE - playerRadius;
        }

        // 카메라 추적
        double targetCameraX = player.x - Constants.WINDOW_WIDTH / 2.0;
        double targetCameraY = player.y - Constants.WINDOW_HEIGHT / 2.0;
        cameraX += (targetCameraX - cameraX) * CAMERA_LERP;
        cameraY += (targetCameraY - cameraY) * CAMERA_LERP;
        
        // 적 스폰
        long now = System.currentTimeMillis();
        if (now - lastSpawnTime > spawnInterval && enemies.size() < maxEnemies) {
            spawnEnemy();
            lastSpawnTime = now;
        }
        
        // 적 업데이트
        for (Enemy enemy : enemies) {
            enemy.update((int)player.x, (int)player.y); 
        }
        
        // 적 사망 처리
        ArrayList<Enemy> enemiesToRemove = new ArrayList<>();
        ArrayList<Enemy> enemiesToAdd = new ArrayList<>();
        
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                if (enemy.type == EnemyType.MAGMA_SLIME_BIG) {
                    enemiesToAdd.add(new Enemy(EnemyType.MAGMA_SLIME_SMALL, enemy.x - 30, enemy.y - 30));
                    enemiesToAdd.add(new Enemy(EnemyType.MAGMA_SLIME_SMALL, enemy.x + 30, enemy.y + 30));
                }
                if (Math.random() < 0.6) {
                    ItemType drop = ItemType.getRandom();
                    items.add(new Item(enemy.x, enemy.y, drop));
                }
                enemiesToRemove.add(enemy);
            }
        }
        enemies.removeAll(enemiesToRemove);
        enemies.addAll(enemiesToAdd);
        
        // 총알 업데이트
        bullets.removeIf(b -> { b.update(); return !b.isActive(); });
        
        // 충돌 체크
        checkBulletCollisions();
        checkItemPickups();
        
        // 데미지 텍스트 업데이트
        damageTexts.removeIf(dt -> { dt.update(); return dt.isExpired(); });

        // [민정님 추가] 플레이어 사망 체크 (HP가 0 이하면 게임오버)
        if (player.getHP() <= 0) {
            gameState = gameOverState;
        }
    }
    
    // 문 충돌 체크 및 방 이동
    private void checkDoorCollision(int tileX, int tileY, char[][] map) {
        if (currentRoom == null) return;
        char tile = map[tileY][tileX];
        if (tile != 'D') return;
        
        String direction = null;
        if (tileY <= 1 && tileX >= 8 && tileX <= 11) direction = "NORTH";
        else if (tileY >= 10 && tileX >= 8 && tileX <= 11) direction = "SOUTH";
        else if (tileX <= 1 && tileY >= 5 && tileY <= 6) direction = "WEST";
        else if (tileX >= 18 && tileY >= 5 && tileY <= 6) direction = "EAST";
        
        if (direction != null && currentRoom.hasConnection(direction)) {
            Integer targetRoomId = currentRoom.getConnectedRoom(direction);
            if (targetRoomId != null) {
                RoomData nextRoom = MapLoader.getRoom(targetRoomId);
                if (nextRoom != null) {
                    currentRoom = nextRoom;
                    char[][] nextMap = nextRoom.getMap();
                    int nextMapWidth = nextMap[0].length;
                    int nextMapHeight = nextMap.length;
                    
                    switch (direction) {
                        case "NORTH": player.x = (nextMapWidth / 2) * Constants.TILE_SIZE; player.y = (nextMapHeight - 2) * Constants.TILE_SIZE; break;
                        case "SOUTH": player.x = (nextMapWidth / 2) * Constants.TILE_SIZE; player.y = 2 * Constants.TILE_SIZE; break;
                        case "WEST": player.x = (nextMapWidth - 2) * Constants.TILE_SIZE; player.y = (nextMapHeight / 2) * Constants.TILE_SIZE; break;
                        case "EAST": player.x = 2 * Constants.TILE_SIZE; player.y = (nextMapHeight / 2) * Constants.TILE_SIZE; break;
                    }
                }
            }
        }
    }
    
    // 맵 경계 그리기
    private void drawMapBorder(Graphics2D g2, char[][] map) {
        if (map == null) return;
        int mapWidth = map[0].length;
        int mapHeight = map.length;
        int tileSize = Constants.TILE_SIZE;
        g2.setColor(Color.GREEN);
        g2.setStroke(new java.awt.BasicStroke(3.0f));
        g2.drawRect(0, 0, mapWidth * tileSize, mapHeight * tileSize);
    }
    
    // 적 생성
    private void spawnEnemy() {
        int margin = 100;
        int side = (int)(Math.random() * 4);
        double x = 0, y = 0;
        
        switch (side) {
            case 0: x = player.x + (Math.random() - 0.5) * Constants.WINDOW_WIDTH; y = player.y - Constants.WINDOW_HEIGHT / 2 - margin; break;
            case 1: x = player.x + (Math.random() - 0.5) * Constants.WINDOW_WIDTH; y = player.y + Constants.WINDOW_HEIGHT / 2 + margin; break;
            case 2: x = player.x - Constants.WINDOW_WIDTH / 2 - margin; y = player.y + (Math.random() - 0.5) * Constants.WINDOW_HEIGHT; break;
            case 3: x = player.x + Constants.WINDOW_WIDTH / 2 + margin; y = player.y + (Math.random() - 0.5) * Constants.WINDOW_HEIGHT; break;
        }
        x = Math.max(50, Math.min(Constants.WORLD_WIDTH - 50, x));
        y = Math.max(50, Math.min(Constants.WORLD_HEIGHT - 50, y));
        enemies.add(new Enemy(EnemyType.MUDGOLEM, x, y));
    }
    
    // 총알 발사
    private void shoot() {
        if (gameState != playState) return; // [추가] 플레이 중이 아니면 발사 불가

        long now = System.currentTimeMillis();
        long delay = (long)(currentWeapon.getAttackSpeed() * 1000 / (1 + player.getAttackSpeedBonus()));
        if (now - lastShootTime < delay) return;
        lastShootTime = now;

        double worldMouseX = mouseX + cameraX;
        double worldMouseY = mouseY + cameraY;
        
        double px = player.x + Constants.TILE_SIZE / 2;
        double py = player.y + Constants.TILE_SIZE / 2;
        double angle = Math.atan2(worldMouseY - py, worldMouseX - px);
        double bulletSpeed = 10;

        if (currentWeapon == WeaponType.SHOTGUN) {
            int pellets = 5;
            double spread = Math.toRadians(15);
            double start = angle - spread / 2;
            double step = spread / (pellets - 1);
            for (int i = 0; i < pellets; i++) {
                bullets.add(new Bullet(px, py, start + step * i, bulletSpeed, currentWeapon.getDamage(), currentWeapon.getRange()));
            }
        } else {
            bullets.add(new Bullet(px, py, angle, bulletSpeed, currentWeapon.getDamage(), currentWeapon.getRange()));
        }
    }
    
    // 총알 충돌
    private void checkBulletCollisions() {
        for (Bullet b : bullets) {
            if (!b.isActive()) continue;
            for (Enemy e : enemies) {
                if (!e.alive) continue;
                
                double drawY_world = e.y - (e.hitHeight - 48);
                double spriteCenterX = e.x + (e.drawWidth / 2.0);
                double spriteCenterY = drawY_world + (e.drawHeight / 2.0);
                double enemyLeft = spriteCenterX - (e.hitWidth / 2.0);
                double enemyRight = spriteCenterX + (e.hitWidth / 2.0);
                double enemyTop = spriteCenterY - (e.hitHeight / 2.0);
                double enemyBottom = spriteCenterY + (e.hitHeight / 2.0);
                
                if (b.getX() >= enemyLeft && b.getX() <= enemyRight &&
                    b.getY() >= enemyTop && b.getY() <= enemyBottom) {
                    
                    double dmg = currentWeapon.getDamage() * player.getAttackMultiplier();
                    e.takeDamage((int)dmg);
                    b.deactivate();

                    Color dmgColor = dmg >= 50 ? Color.RED : Color.YELLOW;
                    damageTexts.add(new DamageText(spriteCenterX, spriteCenterY - 10, String.valueOf((int)dmg), dmgColor));
                }
            }
        }
    }
    
    // 아이템 획득
    private void checkItemPickups() {
        Rectangle playerRect = new Rectangle((int)player.x - Constants.TILE_SIZE / 2, (int)player.y - Constants.TILE_SIZE / 2, Constants.TILE_SIZE, Constants.TILE_SIZE);
        for (Item item : items) {
            if (!item.isPicked() && playerRect.intersects(item.getBounds())) {
                item.pickUp();
                acquiredItems.add(item.getType());
                applyItemEffect(item.getType());
            }
        }
    }

    // 아이템 효과
    private void applyItemEffect(ItemType type) {
        if (type == null) return;
        if (type.getAttackBuff() != 0) player.addAttackBonus(type.getAttackBuff());
        if (type.getHpBuff() != 0) player.addMaxHP(type.getHpBuff());
        if (type.getSpeedBuff() != 0) player.addSpeedBonus(type.getSpeedBuff());
        if (type.getAttackSpeedBuff() != 0) player.addAttackSpeedBonus(type.getAttackSpeedBuff());
        if (type == ItemType.RED_POTION) player.heal(30);
        if (type == ItemType.ELIXIR) player.heal(player.getMaxHP());

        damageTexts.add(new DamageText(player.x, player.y - 20, "+" + type.getName(), Color.CYAN));
    }
    
    // 무기 변경
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
        
        if (next) currentIdx = (currentIdx + 1) % weapons.length;
        else currentIdx = (currentIdx - 1 + weapons.length) % weapons.length;
        
        currentWeapon = weapons[currentIdx];
    }

    // [민정님 추가] Getter (UIRenderer에서 사용)
    public WeaponType getCurrentWeapon() {
        return currentWeapon;
    }
    public RoomData getCurrentRoom() {
        return currentRoom;
    }

    // 화면 렌더링
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // [민정님 추가] 타이틀 화면이면 UI만 그리고 리턴
        if (gameState == titleState) {
            ui.draw(g2);
            return;
        }

        // 맵 그리기
        if (currentRoom != null && tileManager != null) {
            Graphics2D g2Map = (Graphics2D) g2.create();
            g2Map.translate(-(int)cameraX, -(int)cameraY);
            tileManager.render(g2Map, currentRoom.getMap());
            drawMapBorder(g2Map, currentRoom.getMap());
            g2Map.dispose();
        }

        // 적 그리기
        for (Enemy enemy : enemies) {
            enemy.draw(g2, (int)cameraX, (int)cameraY); 
        }

        // 총알 그리기
        for (Bullet bullet : bullets) {
            int screenX = (int)bullet.getX() - (int)cameraX;
            int screenY = (int)bullet.getY() - (int)cameraY;
            if (screenX >= -10 && screenX <= Constants.WINDOW_WIDTH + 10 && screenY >= -10 && screenY <= Constants.WINDOW_HEIGHT + 10) {
                Graphics2D g2Copy = (Graphics2D) g2.create();
                g2Copy.translate(-(int)cameraX, -(int)cameraY);
                bullet.draw(g2Copy);
                g2Copy.dispose();
            }
        }
        
        // 아이템 그리기
        for (Item item : items) {
            Rectangle bounds = item.getBounds();
            int screenX = (int)bounds.getX() - (int)cameraX;
            int screenY = (int)bounds.getY() - (int)cameraY;
            if (screenX >= -25 && screenX <= Constants.WINDOW_WIDTH + 25 && screenY >= -25 && screenY <= Constants.WINDOW_HEIGHT + 25) {
                Graphics2D g2Copy = (Graphics2D) g2.create();
                g2Copy.translate(-(int)cameraX, -(int)cameraY);
                item.draw(g2Copy);
                g2Copy.dispose();
            }
        }

        // 플레이어 그리기
        Graphics2D g2Player = (Graphics2D) g2.create();
        g2Player.translate(-(int)cameraX, -(int)cameraY);
        player.draw(g2Player);
        g2Player.dispose();
        
        // 데미지 텍스트
        for (DamageText dt : damageTexts) {
            Graphics2D g2Copy = (Graphics2D) g2.create();
            g2Copy.translate(-(int)cameraX, -(int)cameraY);
            dt.draw(g2Copy);
            g2Copy.dispose();
        }

        // [민정님 수정] HUD 그리기 (기존 drawPlayerHUD 대신 ui.draw 사용)
        ui.draw(g2);
        
        g2.dispose();
    }

    // [민정님 수정] 키보드 입력 처리 (1,2,3,Q,E 및 상태 전환)
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        // 1. 타이틀 화면 (엔터 -> 시작)
        if (gameState == titleState) {
            if (code == KeyEvent.VK_ENTER) {
                gameState = playState;
            }
        }
        // 2. 플레이 중
        else if (gameState == playState) {
            // WASD 이동
            if (code == KeyEvent.VK_W) keyW = true;
            if (code == KeyEvent.VK_S) keyS = true;
            if (code == KeyEvent.VK_A) keyA = true;
            if (code == KeyEvent.VK_D) keyD = true;
            
            // [요청 기능] 소모품 선택
            if (code == KeyEvent.VK_1) System.out.println("소모품 1번 선택");
            if (code == KeyEvent.VK_2) System.out.println("소모품 2번 선택");
            if (code == KeyEvent.VK_3) System.out.println("소모품 3번 선택");

            // [요청 기능] 소모품 사용 (E)
            if (code == KeyEvent.VK_E) {
                System.out.println("아이템 사용!");
                // 추후 아이템 사용 로직 연결
            }
            
            // [요청 기능] 무기 변경 (Q)
            if (code == KeyEvent.VK_Q) {
                changeWeapon(true);
            }
            
            // 테스트: P 누르면 강제 게임오버
            if (code == KeyEvent.VK_P) gameState = gameOverState;
        }
        // 3. 게임 오버 (R -> 재시작)
        else if (gameState == gameOverState) {
            if (code == KeyEvent.VK_R) {
                setupGame();
                gameState = playState;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) keyW = false;
        if (code == KeyEvent.VK_S) keyS = false;
        if (code == KeyEvent.VK_A) keyA = false;
        if (code == KeyEvent.VK_D) keyD = false;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}

    // 마우스 입력 (기존 유지)
    @Override
    public void mouseMoved(MouseEvent e) { 
        mouseX = e.getX(); 
        mouseY = e.getY(); 
    }
    
    @Override
    public void mouseDragged(MouseEvent e) { 
        mouseMoved(e); 
    }
    
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