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
import enemy.Boss;
// [김민정님 코드] 플레이어 시스템
import player.Player;
import player.KeyHandler;
import ui.UIRenderer;
// [김선욱님 코드] 전투 시스템 (총알, 아이템, 무기)
import item.Bullet;
import item.Item;
import item.ItemType;
import item.Key;
import item.DamageText;
import item.WeaponType;
// [서충만님 코드] 맵 타일 및 방 시스템
import map.TileManager;
import map.MapLoader;
import map.RoomData;
import map.TileType;
// [사운드 시스템]
import system.SoundManager;

public class GamePanel extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener { 

    private static final long serialVersionUID = 1L;
    Thread gameThread;
    final int FPS = Constants.FPS;

    // [김민정님 코드] UI 렌더러 및 게임 상태 관리
    public UIRenderer ui = new UIRenderer(this);
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int gameOverState = 2;
    public final int loadingState = 3;
    
    // [로딩 화면] 로딩 관련 변수
    public long loadingStartTime = 0;
    public final long STAGE_NAME_DURATION = 1500;
    public final long FADE_IN_DURATION = 1000;
    public final long TOTAL_LOADING_DURATION = STAGE_NAME_DURATION + FADE_IN_DURATION;

    // [김민정님 코드] 플레이어 및 KeyHandler
    public Player player;
    private KeyHandler keyH = new KeyHandler();
    
    // [김선욱님 코드] 무기 시스템
    private WeaponType currentWeapon = WeaponType.PISTOL;
    
    // [서상원님 코드] 적 시스템
    public ArrayList<Enemy> enemies = new ArrayList<>();
    // [보스 시스템]
    public Boss boss = null;
    
    // [김선욱님 코드] 전투 시스템 리스트 (총알, 아이템, 데미지 텍스트)
    public ArrayList<Bullet> bullets = new ArrayList<>();
    public ArrayList<Item> items = new ArrayList<>();
    private ArrayList<ItemType> acquiredItems = new ArrayList<>();
    public ArrayList<DamageText> damageTexts = new ArrayList<>();
    private ArrayList<Key> keys = new ArrayList<>();

    private boolean keyW, keyA, keyS, keyD;
    
    // [김선욱님 코드] 마우스 입력 (총알 발사용)
    private int mouseX = Constants.WINDOW_WIDTH / 2;
    private int mouseY = Constants.WINDOW_HEIGHT / 2;
    private long lastShootTime = 0;
    
    // [서상원님 코드] 카메라 시스템 (LERP 추적)
    public double cameraX = 0;
    public double cameraY = 0;
    private final double CAMERA_LERP = 0.05;
    
    // [서상원님 코드] 적 스폰 시스템 (맵 기반)
    private ArrayList<int[]> enemySpawnPoints = new ArrayList<>();
    
    // [서충만님 코드] 맵 타일 및 방 관리
    public RoomData currentRoom;
    public TileManager tileManager;
    
    // [사운드 시스템]
    public SoundManager soundManager; // [김민정님 코드] Player에서 접근하기 위해 public으로 변경

    public GamePanel() {
        // [서상원님 코드] 패널 기본 설정
        this.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(this);
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);  // [김민정님 코드] Tab 누를 시 정보창 띄우기 위함

        // [김민정님 코드] KeyHandler 리스너 추가 (플레이어 이동용)
        this.addKeyListener(keyH);
        // [김선욱님 코드] 마우스 리스너 추가 (총알 발사용)
        addMouseMotionListener(this);
        addMouseListener(this);
        
        // [추가] 마우스 클릭으로 정보창 토글
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (ui != null) {
                    ui.checkStatusIconClick(e.getX(), e.getY());
                }
            }
        });
        
        // [사운드 시스템] 초기화
        soundManager = new SoundManager();
        setupGame();
    }

    public void setupGame() {
        // [서충만님 코드] 맵 초기화
        tileManager = new TileManager();
        MapLoader.loadAllRooms(1);
        currentRoom = MapLoader.getRoom(0);
        
        // [김민정님 코드] 플레이어 초기화
        player = new Player(this, keyH);
        player.x = Constants.TILE_SIZE * 10;
        player.y = Constants.TILE_SIZE * 6;
        
        // [서상원님 코드] 카메라 초기화
        cameraX = player.x - Constants.WINDOW_WIDTH / 2.0;
        cameraY = player.y - Constants.WINDOW_HEIGHT / 2.0;
        
        bullets.clear();
        items.clear();
        damageTexts.clear();
        keys.clear();

        // [김민정님 코드] 게임 시작 시 타이틀 화면 상태로 설정
        gameState = titleState;
        soundManager.playMusic(29); // [김민정님 코드] 타이틀 화면 BGM 재생
        
        startGameThread();
    }

    public void startGameThread() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    // [서상원님 코드] 게임 루프 (델타 타임 기반)
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
        // [로딩 화면] 로딩 상태 처리
        if (gameState == loadingState) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - loadingStartTime >= TOTAL_LOADING_DURATION) {
                spawnEnemiesAfterLoading();
                gameState = playState;
            }
            return;
        }
        
        // [김민정님 코드] 플레이 상태가 아니면(타이틀, 게임오버 등) 게임 로직 멈춤
        if (gameState != playState) {
            return;
        }

        // [김민정님 코드] 테스트용: K키가 눌려있으면 데미지 입음
        if (keyH.kPressed == true) {
            player.receiveDamage(10); 
        }
        
        // [김민정님 코드] 플레이어 이동 업데이트
        double oldX = player.x;
        double oldY = player.y;
        player.update();
        
        final int playerRadius = Constants.TILE_SIZE / 2;
        double playerX = player.x;
        double playerY = player.y;
        
        // [서충만님 코드] 맵 충돌 체크
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
            if (playerX < playerRadius) {
                player.x = playerRadius;
                playerX = playerRadius;
            }
            if (playerX > mapWidth * Constants.TILE_SIZE - playerRadius) {
                player.x = mapWidth * Constants.TILE_SIZE - playerRadius;
                playerX = mapWidth * Constants.TILE_SIZE - playerRadius;
            }
            if (playerY < playerRadius) {
                player.y = playerRadius;
                playerY = playerRadius;
            }
            if (playerY > mapHeight * Constants.TILE_SIZE - playerRadius) {
                player.y = mapHeight * Constants.TILE_SIZE - playerRadius;
                playerY = mapHeight * Constants.TILE_SIZE - playerRadius;
            }
        }

        // [서상원님 코드] 카메라 추적 (LERP)
        double targetCameraX = playerX - Constants.WINDOW_WIDTH / 2.0;
        double targetCameraY = playerY - Constants.WINDOW_HEIGHT / 2.0;
        cameraX += (targetCameraX - cameraX) * CAMERA_LERP;
        cameraY += (targetCameraY - cameraY) * CAMERA_LERP;
        
        // [서상원님 코드] 적 업데이트 (플레이어 추적)
        for (Enemy enemy : enemies) {
            enemy.update((int)playerX, (int)playerY); 
        }
        
        // [서상원님 코드] 죽은 적 처리 및 MAGMA_SLIME_BIG 분열
        ArrayList<Enemy> enemiesToRemove = new ArrayList<>();
        ArrayList<Enemy> enemiesToAdd = new ArrayList<>();
        
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                // [서상원님 코드] MAGMA_SLIME_BIG 분열 처리
                if (enemy.type == EnemyType.MAGMA_SLIME_BIG) {
                    double offsetX1 = enemy.x - 30;
                    double offsetY1 = enemy.y - 30;
                    double offsetX2 = enemy.x + 30;
                    double offsetY2 = enemy.y + 30;
                    enemiesToAdd.add(new Enemy(EnemyType.MAGMA_SLIME_SMALL, offsetX1, offsetY1));
                    enemiesToAdd.add(new Enemy(EnemyType.MAGMA_SLIME_SMALL, offsetX2, offsetY2));
                }
                
                // [김선욱님 코드] 드롭 처리
                EnemyType type = enemy.type;

                boolean isElite =
                    type == EnemyType.ORC ||
                    type == EnemyType.MINOTAUR ||
                    type == EnemyType.GOLEM ||
                    type == EnemyType.FROZEN_KNIGHT ||
                    type == EnemyType.YETI ||
                    type == EnemyType.SNOW_MAGE ||
                    type == EnemyType.ICE_GOLEM ||
                    type == EnemyType.HELL_KNIGHT;

                if (isElite) {
                    keys.add(new Key(enemy.x, enemy.y));
                } else if (Math.random() < 0.6) {
                    ItemType[] possibleDrops = {
                        ItemType.POWER_FRUIT,
                        ItemType.LIFE_SEED,
                        ItemType.WIND_CANDY
                    };
                    ItemType drop = possibleDrops[(int)(Math.random() * possibleDrops.length)];
                    items.add(new Item(enemy.x, enemy.y, drop));
                }

                enemiesToRemove.add(enemy);
                soundManager.playSE(28); // [김민정님 코드] 적 사망 효과음
            }
        }
        
        enemies.removeAll(enemiesToRemove);
        enemies.addAll(enemiesToAdd);
        
        // [김선욱님 코드] 총알 업데이트
        bullets.removeIf(b -> { b.update(); return !b.isActive(); });
        
        // [김선욱님 코드] 총알-적 충돌 감지
        checkBulletCollisions();
        // [김선욱님 코드] 아이템 획득 체크
        checkItemPickups();
        // [김선욱님 코드] 열쇠 획득 체크
        checkKeyPickups();
        // [적 공격 체크] 일반 적의 근접 공격 및 투사체 충돌 체크
        checkEnemyAttacks();
        
        // [김선욱님 코드] 데미지 텍스트 업데이트
        damageTexts.removeIf(dt -> {
            dt.update();
            return dt.isExpired();
        });
        
        // [김민정님 코드] 플레이어 사망 체크 (HP가 0 이하면 게임오버)
        if (player.getHP() <= 0) {
            gameState = gameOverState;
            soundManager.stop();     // [김민정님 코드] 배경음악 정지
            soundManager.playSE(21); // [김민정님 코드] 플레이어 사망(게임오버) 효과음
        }
    }
    
    // [서충만님 코드] 문 충돌 체크 및 방 이동: 플레이어가 문 타일('D')에 닿으면 연결된 방으로 이동
    private void checkDoorCollision(int tileX, int tileY, char[][] map) {
        if (currentRoom == null) return;
        
        char tile = map[tileY][tileX];
        if (tile != 'D') return;
        
        String direction = null;
        
        if (tileY <= 1 && tileX >= 8 && tileX <= 11) {
            direction = "NORTH";
        }
        else if (tileY >= 10 && tileX >= 8 && tileX <= 11) {
            direction = "SOUTH";
        }
        else if (tileX <= 1 && tileY >= 5 && tileY <= 6) {
            direction = "WEST";
        }
        else if (tileX >= 18 && tileY >= 5 && tileY <= 6) {
            direction = "EAST";
        }
        
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
                        case "NORTH":
                            player.x = (nextMapWidth / 2) * Constants.TILE_SIZE;
                            player.y = (nextMapHeight - 2) * Constants.TILE_SIZE;
                            break;
                        case "SOUTH":
                            player.x = (nextMapWidth / 2) * Constants.TILE_SIZE;
                            player.y = 2 * Constants.TILE_SIZE;
                            break;
                        case "WEST":
                            player.x = (nextMapWidth - 2) * Constants.TILE_SIZE;
                            player.y = (nextMapHeight / 2) * Constants.TILE_SIZE;
                            break;
                        case "EAST":
                            player.x = 2 * Constants.TILE_SIZE;
                            player.y = (nextMapHeight / 2) * Constants.TILE_SIZE;
                            break;
                    }
                    
                    // [서상원님 코드] 새 방으로 이동 시 적 스폰 포인트 찾기 및 스폰
                    findEnemySpawnPoints();
                    spawnEnemiesFromMap();
                    playStageMusic();
                    soundManager.playSE(18); // [김민정님 코드] 문 열리는 소리
                }
            }
        }
    }
    
    // [서상원님 코드] 맵에서 E 타일 위치 찾기
    private void findEnemySpawnPoints() {
        enemySpawnPoints.clear();
        if (currentRoom == null) return;
        
        char[][] map = currentRoom.getMap();
        if (map == null) return;
        
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                if (map[y][x] == 'E') {
                    enemySpawnPoints.add(new int[]{x, y});
                }
            }
        }
    }
    
    // [서상원님 코드] 스테이지별 적 타입 랜덤 선택
    private EnemyType getRandomEnemyTypeForStage(int stage) {
        EnemyType[] types;
        
        switch (stage) {
            case 1:
                types = new EnemyType[]{EnemyType.SLIME, EnemyType.WOLF, EnemyType.GOBLIN, EnemyType.MINOTAUR};
                break;
            case 2:
                types = new EnemyType[]{EnemyType.SNAKE, EnemyType.MUDGOLEM, EnemyType.GOLEM, EnemyType.SPORE_FLOWER};
                break;
            case 3:
                types = new EnemyType[]{EnemyType.FROZEN_KNIGHT, EnemyType.YETI, EnemyType.SNOW_MAGE, EnemyType.ICE_GOLEM};
                break;
            case 4:
                types = new EnemyType[]{
                    EnemyType.BOMB_SKULL, 
                    EnemyType.HELL_HOUND, 
                    EnemyType.FIRE_IMP, 
                    EnemyType.HELL_KNIGHT, 
                    EnemyType.MAGMA_SLIME_BIG,
                    EnemyType.MAGMA_SLIME_SMALL,
                    EnemyType.ORC
                };
                break;
            case 5:
                return null;
            default:
                types = new EnemyType[]{EnemyType.SLIME};
        }
        
        return types[(int)(Math.random() * types.length)];
    }
    
    // [서상원님 코드] 맵의 E 타일 위치에서 적 스폰
    private void spawnEnemiesFromMap() {
        if (enemySpawnPoints.isEmpty()) return;
        
        int currentStage = MapLoader.getCurrentStage();
        enemies.clear();
        
        if (currentStage == 5) {
            if (!enemySpawnPoints.isEmpty()) {
                int[] spawnPoint = enemySpawnPoints.get(0);
                int tileX = spawnPoint[0];
                int tileY = spawnPoint[1];
                System.out.println("보스 스폰 위치: (" + tileX + ", " + tileY + ")");
            }
            return;
        }
        
        for (int[] spawnPoint : enemySpawnPoints) {
            int tileX = spawnPoint[0];
            int tileY = spawnPoint[1];
            
            double spawnX = (tileX + 0.5) * Constants.TILE_SIZE;
            double spawnY = (tileY + 0.5) * Constants.TILE_SIZE;
            
            EnemyType enemyType = getRandomEnemyTypeForStage(currentStage);
            if (enemyType != null) {
                enemies.add(new Enemy(enemyType, spawnX, spawnY));
            }
        }
    }
    
    // [자폭해골] 자폭 처리: 범위 내 플레이어에게 데미지 적용
    private void handleBombSkullExplosion(Enemy bombSkull) {
        double drawY_world = bombSkull.y - (bombSkull.hitHeight - 48);
        double explosionX = bombSkull.x + (bombSkull.drawWidth / 2.0);
        double explosionY = drawY_world + (bombSkull.drawHeight / 2.0);
        
        int explosionRange = 200;
        double playerX = player.x;
        double playerY = player.y;
        
        double dx = playerX - explosionX;
        double dy = playerY - explosionY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance <= explosionRange) {
            int explosionDamage = bombSkull.type.getAttack();
            player.receiveDamage(explosionDamage);
            damageTexts.add(new DamageText(playerX, playerY - 10,
                    String.valueOf(explosionDamage), Color.ORANGE));
        }
    }
    
    // [로딩 화면] 로딩 완료 후 몹 소환
    private void spawnEnemiesAfterLoading() {
        // [서상원님 코드] 현재 방의 적 스폰 포인트 찾기 및 스폰
        findEnemySpawnPoints();
        spawnEnemiesFromMap();
        boss = null;
    }
    
    // [서충만님 코드] 맵 경계 테두리 그리기: 맵 경계를 초록색 선과 모서리 사각형으로 표시
    private void drawMapBorder(Graphics2D g2, char[][] map) {
        if (map == null) return;
        
        int mapWidth = map[0].length;
        int mapHeight = map.length;
        int tileSize = Constants.TILE_SIZE;
        
        g2.setColor(Color.GREEN);
        g2.setStroke(new java.awt.BasicStroke(3.0f));
        
        int mapPixelWidth = mapWidth * tileSize;
        int mapPixelHeight = mapHeight * tileSize;
        
        g2.drawRect(0, 0, mapPixelWidth, mapPixelHeight);
        
        int cornerSize = 20;
        g2.fillRect(0, 0, cornerSize, cornerSize);
        g2.fillRect(mapPixelWidth - cornerSize, 0, cornerSize, cornerSize);
        g2.fillRect(0, mapPixelHeight - cornerSize, cornerSize, cornerSize);
        g2.fillRect(mapPixelWidth - cornerSize, mapPixelHeight - cornerSize, cornerSize, cornerSize);
    }
    
    // [김선욱님 코드] 총알 발사
    private void shoot() {
        if (gameState != playState) return; // [김민정님 코드] 플레이 중이 아니면 발사 불가
        
        long now = System.currentTimeMillis();
        long delay = (long)(currentWeapon.getAttackSpeed() * 1000 / (1 + player.getAttackSpeedBonus()));
        if (now - lastShootTime < delay) return;
        lastShootTime = now;
        
        // [김민정님 코드] 무기 발사 사운드
        switch (currentWeapon) {
            // 근접 무기 (검)
        	case DAGGER: soundManager.playSE(0); break; // [김민정님 코드] 단검 소리
            case KNIGHT_SWORD: soundManager.playSE(1); break; // [김민정님 코드] 롱소드 소리
            case LONG_SWORD: soundManager.playSE(2); break; // [김민정님 코드] 대검 소리
            
            // 원거리 무기 (총)
            case PISTOL: soundManager.playSE(3); break; // [김민정님 코드] 권총 소리
            case SHOTGUN: soundManager.playSE(4); break; // [김민정님 코드] 샷건 소리
            case SNIPER: soundManager.playSE(5); break; // [김민정님 코드] 스나이퍼 소리
            default: soundManager.playSE(3); break; 
        }

        // [서상원님 코드] 마우스 위치를 월드 좌표로 변환 (카메라 오프셋 적용)
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
                double a = start + step * i;
                bullets.add(new Bullet(px, py, a, bulletSpeed, currentWeapon.getDamage(), currentWeapon.getRange(), currentWeapon));
            }
        } else {
            bullets.add(new Bullet(px, py, angle, bulletSpeed, currentWeapon.getDamage(), currentWeapon.getRange(), currentWeapon));
        }
    }
    
    // [김선욱님 코드] 총알-적 충돌 감지
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
                    damageTexts.add(new DamageText(spriteCenterX, spriteCenterY - 10,
                            String.valueOf((int)dmg), dmgColor));
                }
            }
            
            if (boss != null && boss.alive) {
                double drawY_world = boss.y - (boss.hitHeight - 48);
                double spriteCenterX = boss.x + (boss.drawWidth / 2.0);
                double spriteCenterY = drawY_world + (boss.drawHeight / 2.0);
                
                double bossLeft = spriteCenterX - (boss.hitWidth / 2.0);
                double bossRight = spriteCenterX + (boss.hitWidth / 2.0);
                double bossTop = spriteCenterY - (boss.hitHeight / 2.0);
                double bossBottom = spriteCenterY + (boss.hitHeight / 2.0);
                
                if (b.getX() >= bossLeft && b.getX() <= bossRight &&
                    b.getY() >= bossTop && b.getY() <= bossBottom) {
                    double dmg = currentWeapon.getDamage() * player.getAttackMultiplier();
                    boss.takeDamage((int)dmg);
                    b.deactivate();

                    Color dmgColor = dmg >= 50 ? Color.RED : Color.YELLOW;
                    damageTexts.add(new DamageText(spriteCenterX, spriteCenterY - 10,
                            String.valueOf((int)dmg), dmgColor));
                }
            }
        }
    }
    
    // [김선욱님 코드] 아이템 획득 체크
    private void checkItemPickups() {
        double playerX = player.x;
        double playerY = player.y;
        Rectangle playerRect = new Rectangle((int)playerX - Constants.TILE_SIZE / 2, (int)playerY - Constants.TILE_SIZE / 2, Constants.TILE_SIZE, Constants.TILE_SIZE);
        for (Item item : items) {
            if (!item.isPicked() && playerRect.intersects(item.getBounds())) {
                item.pickUp();
                acquiredItems.add(item.getType());
                applyItemEffect(item.getType());
                
                // [김민정님 코드] 아이템 획득 사운드
                soundManager.playSE(15); // [김민정님 코드] 아이템 획득 소리
            }
        }
    }
    
    // [김선욱님 코드] 열쇠 획득 체크
    private void checkKeyPickups() {
        double playerX = player.x;
        double playerY = player.y;
        Rectangle playerRect = new Rectangle((int)playerX - Constants.TILE_SIZE / 2, (int)playerY - Constants.TILE_SIZE / 2, Constants.TILE_SIZE, Constants.TILE_SIZE);
        keys.removeIf(key -> {
            if (!key.isPicked() && playerRect.intersects(key.getBounds())) {
                key.pickUp();
                soundManager.playSE(12);
                return true;
            }
            return false;
        });
    }

    // [김선욱님 코드] 아이템 효과 적용
    private void applyItemEffect(ItemType type) {
        if (type == null) return;

        if (type.getAttackBuff() != 0) {
            player.addAttackBonus(type.getAttackBuff());
        }
        if (type.getHpBuff() != 0) {
            player.addMaxHP(type.getHpBuff());
        }
        if (type.getSpeedBuff() != 0) {
            player.addSpeedBonus(type.getSpeedBuff());
        }
        if (type.getAttackSpeedBuff() != 0) {
            player.addAttackSpeedBonus(type.getAttackSpeedBuff());
        }

        if (type == ItemType.RED_POTION) {
            player.heal(30);
        }
        if (type == ItemType.ELIXIR) {
            player.heal(player.getMaxHP());
        }

        damageTexts.add(new DamageText(player.x, player.y - 20,
                "+" + type.getName(), Color.CYAN));
    }
    
    // [적 공격 체크] 일반 적의 근접 공격 및 투사체 충돌 체크
    private void checkEnemyAttacks() {
        double playerX = player.x;
        double playerY = player.y;
        
        for (Enemy enemy : enemies) {
            if (!enemy.alive) continue;
            
            if (enemy.shouldPlayAttackSound()) {
                int soundIndex = enemy.getAttackSoundIndex();
                if (soundIndex >= 0) {
                    soundManager.playSE(soundIndex);
                }
            }
            
            if (enemy.type == EnemyType.BOMB_SKULL && enemy.shouldExplode) {
                handleBombSkullExplosion(enemy);
                enemy.alive = false;
                enemy.shouldExplode = false;
                continue;
            }
            
            if (enemy.canAttackPlayer((int)playerX, (int)playerY)) {
                int damage = enemy.getAttackDamage();
                player.receiveDamage(damage);
                damageTexts.add(new DamageText(playerX, playerY - 10,
                        String.valueOf(damage), Color.RED));
            }
        }
    }
    
    // [김선욱님 코드] 무기 변경
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
    
    // [김민정님 코드] Getter (UIRenderer에서 사용)
    public WeaponType getCurrentWeapon() {
        return currentWeapon;
    }
    
    public RoomData getCurrentRoom() {
        return currentRoom;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // [김민정님 코드] 타이틀 화면이면 UI만 그리고 리턴
        if (gameState == titleState) {
            ui.draw(g2);
            return;
        }
        
        // [김민정님 코드] 로딩 화면이면 UI만 그리고 리턴
        if (gameState == loadingState) {
            ui.draw(g2);
            return;
        }

        // [서충만님 코드] 맵 그리기
        if (currentRoom != null && tileManager != null) {
            Graphics2D g2Map = (Graphics2D) g2.create();
            g2Map.translate(-(int)cameraX, -(int)cameraY);
            tileManager.render(g2Map, currentRoom.getMap());
            drawMapBorder(g2Map, currentRoom.getMap());
            g2Map.dispose();
        }

        // [서상원님 코드] 적 그리기
        for (Enemy enemy : enemies) {
            enemy.draw(g2, (int)cameraX, (int)cameraY); 
        }
        
        // [보스 시스템] 보스 그리기
        if (boss != null && boss.alive) {
            boss.draw(g2, (int)cameraX, (int)cameraY);
        }

        // [김선욱님 코드] 총알 그리기
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
        
        // [김선욱님 코드] 아이템 그리기
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
        
        // [김선욱님 코드] 열쇠 그리기
        for (Key key : keys) {
            Rectangle bounds = key.getBounds();
            int screenX = (int)bounds.getX() - (int)cameraX;
            int screenY = (int)bounds.getY() - (int)cameraY;
            if (screenX >= -25 && screenX <= Constants.WINDOW_WIDTH + 25 &&
                screenY >= -25 && screenY <= Constants.WINDOW_HEIGHT + 25) {
                Graphics2D g2Copy = (Graphics2D) g2.create();
                g2Copy.translate(-(int)cameraX, -(int)cameraY);
                key.draw(g2Copy);
                g2Copy.dispose();
            }
        }

        // [김민정님 코드] 플레이어 그리기
        Graphics2D g2Player = (Graphics2D) g2.create();
        g2Player.translate(-(int)cameraX, -(int)cameraY);
        player.draw(g2Player);
        g2Player.dispose();
        
        // [김선욱님 코드] 데미지 텍스트 그리기
        for (DamageText dt : damageTexts) {
            Graphics2D g2Copy = (Graphics2D) g2.create();
            g2Copy.translate(-(int)cameraX, -(int)cameraY);
            dt.draw(g2Copy);
            g2Copy.dispose();
        }

        // [김민정님 코드] HUD 그리기 (기존 drawPlayerHUD 대신 ui.draw 사용)
        ui.draw(g2);
        g2.dispose();
    }
    
    // [서상원님 코드] 키보드 입력 처리
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        if (gameState == titleState) {
            if (code == KeyEvent.VK_ENTER) {
                gameState = loadingState;
                loadingStartTime = System.currentTimeMillis();
                soundManager.stop();      // [김민정님 코드] 타이틀 음악 정지
                playStageMusic();         // [김민정님 코드] 스테이지 배경음 시작
            }
        }
        else if (gameState == playState) {
            if (code == KeyEvent.VK_W) keyW = true;
            if (code == KeyEvent.VK_S) keyS = true;
            if (code == KeyEvent.VK_A) keyA = true;
            if (code == KeyEvent.VK_D) keyD = true;
            
            if (code == KeyEvent.VK_1) System.out.println("소모품 1번 선택");
            if (code == KeyEvent.VK_2) System.out.println("소모품 2번 선택");
            if (code == KeyEvent.VK_3) System.out.println("소모품 3번 선택");

            if (code == KeyEvent.VK_E) {
                System.out.println("아이템 사용!");
            }
            
            if (code == KeyEvent.VK_Q) {
                changeWeapon(true);
            }
            
            if (code == KeyEvent.VK_P) gameState = gameOverState;
        }
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
        
        // [서상원님 코드] WASD 키 해제
        if (code == KeyEvent.VK_W) keyW = false;
        if (code == KeyEvent.VK_S) keyS = false;
        if (code == KeyEvent.VK_A) keyA = false;
        if (code == KeyEvent.VK_D) keyD = false;
    }
    
    // [김선욱님 코드] 마우스 위치 추적
    @Override
    public void mouseMoved(MouseEvent e) { 
        mouseX = e.getX(); 
        mouseY = e.getY(); 
    }
    
    @Override
    public void mouseDragged(MouseEvent e) { 
        mouseMoved(e); 
    }
    
    // [김선욱님 코드] 총알 발사 (마우스 왼쪽 버튼)
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
    
    // [김민정님 코드] 사운드 재생 헬퍼 메소드
    public void playStageMusic() { // [김민정님 코드] 스테이지별 음악 자동 재생 메소드
        int currentStage = MapLoader.getCurrentStage();
        if (currentStage == 0) currentStage = 1;

        // Stage 1(Forest)=6, Stage 2=7, ...
        int musicIndex = 5 + currentStage; 
        
        if (musicIndex < 6 || musicIndex > 10) {
            musicIndex = 6;
        }

        soundManager.playMusic(musicIndex);
    }
}
