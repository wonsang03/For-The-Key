package main;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Image;
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
import player.KeyHandler;
import ui.UIRenderer;
// [김선욱님 코드] 전투 시스템 (총알, 아이템, 무기)
import item.Bullet;
import item.Item;
import item.ItemType;
import item.Key;
import item.Weapon;
import item.DamageText;
import item.WeaponType;
// [서충만님 코드] 맵 타일 및 방 시스템
import map.TileManager;
import map.MapLoader;
import map.RoomData;
import map.TileType;

public class GamePanel extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener { 

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
    public Player player; // UIRenderer 접근을 위해 public으로 변경
    private KeyHandler keyH = new KeyHandler();
    
    // [김선욱님 코드] 무기 시스템
    private WeaponType currentWeapon = WeaponType.PISTOL;
    
    // [서상원님 코드] 적 시스템
    public ArrayList<enemy.Enemy> enemies = new ArrayList<>(); // [민정님 수정] UIRenderer 접근을 위해 public으로 변경
    
    // [김선욱님 코드] 전투 시스템 리스트 (총알, 아이템, 데미지 텍스트)
    private ArrayList<Bullet> bullets = new ArrayList<>(); // 총알
    private ArrayList<Item> items = new ArrayList<>(); // 아이템 
    private ArrayList<ItemType> acquiredItems = new ArrayList<>(); // 획득한 아이템
    private ArrayList<DamageText> damageTexts = new ArrayList<>(); // 데미지 텍스트
    private ArrayList<Key> keys = new ArrayList<>();

    private boolean keyW, keyA, keyS, keyD; // WASD 키 상태
    
    // [김선욱님 코드] 마우스 입력 (총알 발사용)
    private int mouseX = Constants.WINDOW_WIDTH / 2; // 마우스 X 좌표
    private int mouseY = Constants.WINDOW_HEIGHT / 2; // 마우스 Y 좌표
    private long lastShootTime = 0; // 마지막 발사 시간
    
    // [서상원님 코드] 카메라 시스템 (LERP 추적)
    public double cameraX = 0; // 카메라 X 좌표
    public double cameraY = 0; // 카메라 Y 좌표
    private final double CAMERA_LERP = 0.05; // 카메라 부드러움
    
    // [서상원님 코드] 적 스폰 시스템 (맵 기반)
    private ArrayList<int[]> enemySpawnPoints = new ArrayList<>(); // E 타일 위치들 (타일 좌표)
    
    // [서충만님 코드] 맵 타일 및 방 관리
    public RoomData currentRoom; // UIRenderer 접근을 위해 public으로 변경
    private TileManager tileManager;

    //생성자: GamePanel 초기화 및 리스너 설정
    // [민정님 추가] 생성자 주석

    public GamePanel() {
        // [서상원님 코드] 패널 기본 설정
        this.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(this);
        this.setFocusable(true);

        // [김민정님 코드] KeyHandler 리스너 추가 (플레이어 이동용)
        this.addKeyListener(keyH);
        
        // [김선욱님 코드] 마우스 리스너 추가 (총알 발사용)
        addMouseMotionListener(this);
        addMouseListener(this);

        setupGame();
    }

     //게임 초기화: 맵, 플레이어, 카메라, 초기 적 생성
     
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
        
        // [서상원님 코드] 현재 방의 적 스폰 포인트 찾기 및 스폰
        findEnemySpawnPoints();
        spawnEnemiesFromMap();
        
        // 리스트 초기화 (재시작 대비)
        bullets.clear();
        items.clear();
        damageTexts.clear();

        // [민정님 추가] 게임 시작 시 타이틀 화면 상태로 설정
        gameState = titleState;
        
        startGameThread();
    }

    // 게임 스레드 시작
    public void startGameThread() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    // 게임 루프: 60 FPS로 update()와 repaint()를 반복 호출
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

    //게임 상태 업데이트: 플레이어 이동, 맵 충돌, 카메라, 적, 총알, 아이템 등 모든 게임 오브젝트 업데이트
    public void update() {
        // [민정님 추가] 플레이 상태가 아니면(타이틀, 게임오버 등) 게임 로직 멈춤
        if (gameState != playState) {
            return;
        }
        
        // [김민정님 코드] 플레이어 이동 업데이트
        double oldX = player.x;
        double oldY = player.y;
        player.update();
        
        // [수정: 기존 playerRadius = 20 (고정값) → Constants.TILE_SIZE / 2 (타일 크기 기준, 64/2 = 32)로 변경하여 타일 시스템과 일관성 유지]
        final int playerRadius = Constants.TILE_SIZE / 2;
        
        // [서상원님 코드] 플레이어 위치 가져오기 [수정: 기존 player.getX()/getY() 메서드 호출 → player.x/y 필드 직접 접근으로 변경]
        double playerX = player.x;
        double playerY = player.y;
        
        // [서충만님 코드] 맵 충돌 체크 [서상원님 코드: 원래는 WORLD_WIDTH/HEIGHT 기준으로 경계 체크]
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
            
            // [서충만님 코드] 벽 충돌 체크
            char currentTile = map[tileY][tileX];
            TileType tileType = TileType.fromSymbol(currentTile);
            
            if (tileType.isSolid()) {
                player.x = (int)oldX;
                player.y = (int)oldY;
                playerX = oldX;
                playerY = oldY;
            } else {
                // [서충만님 코드] 문 충돌 체크
                checkDoorCollision(tileX, tileY, map);
            }
            
            // [수정: 기존 WORLD_WIDTH/HEIGHT (고정 월드 크기) 기준 경계 체크 → 현재 맵 크기(mapWidth * TILE_SIZE, mapHeight * TILE_SIZE) 기준으로 경계 제한하도록 변경]
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
                EnemyType type = enemy.getType();

                // 🔹 정예 몬스터 목록
                boolean isElite =
                    type == EnemyType.ORC ||
                    type == EnemyType.MINOTAUR ||
                    type == EnemyType.GOLEM ||
                    type == EnemyType.FROZEN_KNIGHT ||
                    type == EnemyType.YETI ||
                    type == EnemyType.SNOW_MAGE ||
                    type == EnemyType.ICE_GOLEM ||
                    type == EnemyType.HELL_KNIGHT;

                // 🔹 정예 몬스터 → 무조건 열쇠 드롭 (아이템 X)
                if (isElite) {
                    keys.add(new Key(enemy.x, enemy.y));
                    System.out.println("🔑 정예몹 처치 → 열쇠 드롭 (확정)");
                }

                // 🔹 일반 몬스터만 아이템 드롭 확률 적용
                else if (Math.random() < 0.6) {
                    ItemType[] possibleDrops = {
                        ItemType.POWER_FRUIT,
                        ItemType.LIFE_SEED,
                        ItemType.WIND_CANDY
                    };
                    ItemType drop = possibleDrops[(int)(Math.random() * possibleDrops.length)];
                    items.add(new Item(enemy.x, enemy.y, drop));
                }

                enemiesToRemove.add(enemy);



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
        
        // [김선욱님 코드] 데미지 텍스트 업데이트
        damageTexts.removeIf(dt -> {
            dt.update();
            return dt.isExpired();
        });
        
        // [민정님 추가] 플레이어 사망 체크 (HP가 0 이하면 게임오버)
        if (player.getHP() <= 0) {
            gameState = gameOverState;
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
                    
                    System.out.println("→ " + direction + " 방향으로 Room " + targetRoomId + "로 이동");
                    
                    // [서상원님 코드] 새 방으로 이동 시 적 스폰 포인트 찾기 및 스폰
                    findEnemySpawnPoints();
                    spawnEnemiesFromMap();
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
        
        System.out.println("적 스폰 포인트 " + enemySpawnPoints.size() + "개 발견");
    }
    
    // [서상원님 코드] 스테이지별 적 타입 랜덤 선택
    private EnemyType getRandomEnemyTypeForStage(int stage) {
        EnemyType[] types;
        
        switch (stage) {
            case 1:
                types = new EnemyType[]{EnemyType.SLIME, EnemyType.WOLF, EnemyType.GOBLIN, EnemyType.MINOTAUR};
                break;
            case 2:
                types = new EnemyType[]{EnemyType.SNAKE, EnemyType.SPORE_FLOWER, EnemyType.MUDGOLEM, EnemyType.GOLEM};
                break;
            case 3:
                types = new EnemyType[]{EnemyType.ICE_GOLEM, EnemyType.YETI, EnemyType.SNOW_MAGE, EnemyType.FROZEN_KNIGHT};
                break;
            case 4:
                types = new EnemyType[]{EnemyType.BOMB_SKULL, EnemyType.HELL_HOUND, EnemyType.FIRE_IMP, EnemyType.HELL_KNIGHT, EnemyType.MAGMA_SLIME_BIG};
                break;
            case 5:
                // 5스테이지는 보스만 (별도 처리)
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
        
        // 기존 적들 제거 (새 방으로 이동 시)
        enemies.clear();
        
        // 5스테이지는 보스만 스폰 (첫 번째 E 타일 위치에만)
        if (currentStage == 5) {
            if (!enemySpawnPoints.isEmpty()) {
                int[] spawnPoint = enemySpawnPoints.get(0);
                int tileX = spawnPoint[0];
                int tileY = spawnPoint[1];
                double spawnX = (tileX + 0.5) * Constants.TILE_SIZE;
                double spawnY = (tileY + 0.5) * Constants.TILE_SIZE;
                
                // 보스 스폰 (Boss 클래스 사용 - 별도 구현 필요)
                // 일단 보스는 Enemy로 대체 (나중에 Boss 클래스로 변경 가능)
                // enemies.add(new Boss(spawnX, spawnY));
                System.out.println("보스 스폰 위치: (" + tileX + ", " + tileY + ") - 보스 스폰은 아직 구현되지 않았습니다.");
            }
            return;
        }
        
        // 각 스폰 포인트에서 적 스폰
        for (int[] spawnPoint : enemySpawnPoints) {
            int tileX = spawnPoint[0];
            int tileY = spawnPoint[1];
            
            // 타일 좌표를 픽셀 좌표로 변환 (타일 중심)
            double spawnX = (tileX + 0.5) * Constants.TILE_SIZE;
            double spawnY = (tileY + 0.5) * Constants.TILE_SIZE;
            
            // 스테이지별 랜덤 적 타입 선택
            EnemyType enemyType = getRandomEnemyTypeForStage(currentStage);
            if (enemyType != null) {
                enemies.add(new Enemy(enemyType, spawnX, spawnY));
                System.out.println("적 스폰: " + enemyType.getName() + " at (" + tileX + ", " + tileY + ")");
            }
        }
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
    
    
    //총알 발사: 마우스 위치를 향해 총알을 발사 (공격 속도 제한, 샷건은 여러 발 동시 발사)
    // [김선욱님 코드] 총알 발사
    private void shoot() {
        if (gameState != playState) return; // [민정님 추가] 플레이 중이 아니면 발사 불가
        
        long now = System.currentTimeMillis();
        long delay = (long)(currentWeapon.getAttackSpeed() * 1000 / (1 + player.getAttackSpeedBonus()));
        if (now - lastShootTime < delay) return;
        lastShootTime = now;

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
    
    //총알-적 충돌 감지: 총알이 적에 맞으면 데미지를 입히고 데미지 텍스트 생성
    // [김선욱님 코드] 총알-적 충돌 감지 [수정: 기존 단순 거리 기반 충돌 감지 → 서상원님 코드의 AABB(축 정렬 경계 상자) 히트박스 방식으로 변경 (스프라이트 중심 좌표 계산, enemyLeft/Right/Top/Bottom으로 정확한 충돌 영역 체크)]
    private void checkBulletCollisions() {
        for (Bullet b : bullets) {
            if (!b.isActive()) continue;
            for (Enemy e : enemies) {
                if (!e.alive) continue;
                
                // [서상원님 코드] 스프라이트 중심 좌표 계산
                double drawY_world = e.y - (e.hitHeight - 48);
                double spriteCenterX = e.x + (e.drawWidth / 2.0);
                double spriteCenterY = drawY_world + (e.drawHeight / 2.0);
                
                double enemyLeft = spriteCenterX - (e.hitWidth / 2.0);
                double enemyRight = spriteCenterX + (e.hitWidth / 2.0);
                double enemyTop = spriteCenterY - (e.hitHeight / 2.0);
                double enemyBottom = spriteCenterY + (e.hitHeight / 2.0);
                
                if (b.getX() >= enemyLeft && b.getX() <= enemyRight &&
                    b.getY() >= enemyTop && b.getY() <= enemyBottom) {
                    // [김선욱님 코드] 데미지 계산 및 적용
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
    
    //아이템 획득 체크: 플레이어가 아이템과 겹치면 아이템을 획득하고 효과 적용
    // [김선욱님 코드] 아이템 획득 체크 [수정: 기존 player.getX()/getY() 메서드 호출 → player.x/y 필드 직접 접근으로 변경, 기존 고정 크기(40x40) → 타일 크기 기준(Constants.TILE_SIZE)으로 플레이어 충돌 영역 크기 변경]
    private void checkItemPickups() {
        double playerX = player.x;
        double playerY = player.y;
        Rectangle playerRect = new Rectangle((int)playerX - Constants.TILE_SIZE / 2, (int)playerY - Constants.TILE_SIZE / 2, Constants.TILE_SIZE, Constants.TILE_SIZE);
        for (Item item : items) {
            if (!item.isPicked() && playerRect.intersects(item.getBounds())) {
                item.pickUp();
                acquiredItems.add(item.getType());
                applyItemEffect(item.getType());
            }
        }
    }

    //아이템 효과 적용: 획득한 아이템의 스탯 보너스를 플레이어에 적용하고 회복 아이템은 체력 회복
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
    
    //무기 변경: 현재 무기를 다음/이전 무기로 순환 변경
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
    
    // [민정님 추가] Getter (UIRenderer에서 사용)
    public WeaponType getCurrentWeapon() {
        return currentWeapon;
    }
    public RoomData getCurrentRoom() {
        return currentRoom;
    }

    //화면 렌더링: 맵, 적, 총알, 아이템, 플레이어, 데미지 텍스트, HUD를 순서대로 그리기
    // 화면 렌더링: 맵, 적, 총알, 아이템, 플레이어, 데미지 텍스트, HUD를 순서대로 그리기
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // [민정님 추가] 타이틀 화면이면 UI만 그리고 리턴
        if (gameState == titleState) {
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

        // [김선욱님 코드] 총알 그리기 [수정: 기존 카메라 오프셋 없이 그리기 → 카메라 오프셋(cameraX, cameraY) 적용하여 월드 좌표를 화면 좌표로 변환, 화면 밖 총알은 그리지 않도록 필터링 추가 (screenX/Y 범위 체크)]
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
        
        // [김선욱님 코드] 아이템 그리기 [수정: 기존 카메라 오프셋 없이 그리기 → 카메라 오프셋(cameraX, cameraY) 적용하여 월드 좌표를 화면 좌표로 변환, 화면 밖 아이템은 그리지 않도록 필터링 추가 (screenX/Y 범위 체크)]
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

        // [김민정님 코드] 플레이어 그리기 [서상원님 코드: 원래는 화면 중앙에 고정] [수정: 기존 화면 중앙 고정 방식 → 카메라 오프셋(cameraX, cameraY)을 사용하여 플레이어를 월드 좌표 기준으로 그리도록 변경]
        Graphics2D g2Player = (Graphics2D) g2.create();
        g2Player.translate(-(int)cameraX, -(int)cameraY);
        player.draw(g2Player);
        g2Player.dispose();
        
        // [김선욱님 코드] 데미지 텍스트 그리기 [수정: 기존 카메라 오프셋 없이 그리기 → 카메라 오프셋(cameraX, cameraY) 적용하여 월드 좌표를 화면 좌표로 변환]
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

    // 플레이어 HUD 그리기: 화면 왼쪽 상단에 HP, 무기 정보, 스탯, 방 정보 등을 표시
    // [김선욱님 코드] HUD 그리기 [수정: 기존 HUD 높이 160 → 180으로 변경, 기존 직접 이모티콘 사용 → 이모티콘 지원 폰트 자동 탐지 및 Unicode escape sequence 사용, "Q/E: 무기 변경" 안내 텍스트 추가, 현재 방 정보(Room ID) 표시 추가]
    private void drawPlayerHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(20, 20, 330, 180, 15, 15);

        g2.setColor(Color.WHITE);
        
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, 
                           java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        Font baseFont = new Font("Malgun Gothic", Font.BOLD, 16);
        Font emojiFont = null;
        
        String[] emojiFontNames = {"Segoe UI Emoji", "Segoe UI Symbol", "Malgun Gothic", 
                                   "Microsoft YaHei", "Arial Unicode MS"};
        
        String testEmoji = "\u2764\uFE0F";
        
        for (String fontName : emojiFontNames) {
            Font testFont = new Font(fontName, Font.BOLD, 16);
            if (testFont.canDisplayUpTo(testEmoji) == -1) {
                emojiFont = testFont;
                break;
            }
        }
        
        if (emojiFont == null) {
            emojiFont = baseFont;
        }
        
        g2.setFont(emojiFont);
        g2.drawString("\u2764\uFE0F HP: " + player.getHP() + " / " + player.getMaxHP(), 40, 50);
        g2.drawString("\uD83D\uDD2B Weapon: " + currentWeapon.getName(), 40, 75);
        
        g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        g2.drawString(String.format(" - Damage: %.0f | Range: %.0fpx | Speed: %.2fs",
                currentWeapon.getDamage(), currentWeapon.getRange(), currentWeapon.getAttackSpeed()), 55, 95);

        g2.setFont(emojiFont);
        g2.drawString(String.format("\u2694\uFE0F ATK Multiplier: x%.2f", player.getAttackMultiplier()), 40, 120);
        g2.drawString(String.format("\uD83D\uDCA8 Move Speed: %.2f", player.getMoveSpeed()), 40, 140);
        g2.drawString(String.format("\u26A1\uFE0F Attack Speed Bonus: +%.2f", player.getAttackSpeedBonus()), 40, 160);
        
        g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        g2.drawString("Q/E: 무기 변경", 40, 180);
        
        // [서충만님 코드] 현재 방 정보 표시
        if (currentRoom != null) {
            g2.drawString("Room: " + currentRoom.getRoomId(), 40, 200);
        }
    }
    
    //키보드 입력 처리: WASD 키는 플래그 설정, Q/E 키는 무기 변경
    // [서상원님 코드] 키보드 입력 처리
    @Override
    public void keyTyped(KeyEvent e) {
    }

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
        
        // [서상원님 코드] WASD 키 해제
        if (code == KeyEvent.VK_W) keyW = false;
        if (code == KeyEvent.VK_S) keyS = false;
        if (code == KeyEvent.VK_A) keyA = false;
        if (code == KeyEvent.VK_D) keyD = false;
    }
    
    //마우스 입력 처리: 마우스 위치 추적 및 왼쪽 버튼 클릭 시 총알 발사
    // [김선욱님 코드] 마우스 위치 추적
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
}
