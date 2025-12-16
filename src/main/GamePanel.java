package main;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import item.Box;
// [김선욱님 코드] 전투 시스템 (총알, 아이템, 무기)
import item.Bullet;
import item.Item;
import item.ItemType;
import item.Weapon;
import item.Key;
import item.MeleeAttack;
import item.WeaponRarity;
import item.DamageText;
import item.HitSpark;
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
    public final int gameClearState = 4; // 🔹 보스 처치 시 클리어 화면
    
    // [로딩 화면] 로딩 관련 변수
    public long loadingStartTime = 0;
    public final long STAGE_NAME_DURATION = 1500;
    public final long FADE_IN_DURATION = 1000;
    public final long TOTAL_LOADING_DURATION = STAGE_NAME_DURATION + FADE_IN_DURATION;

    // [김민정님 코드] 플레이어 및 KeyHandler
    public Player player;
    // Player가 스테이지 이동/무기 줍기에서 keyH의 플래그에 접근하므로 public으로 둔다.
    public KeyHandler keyH = new KeyHandler(this);
    
    public ArrayList<Weapon> groundWeapons = new ArrayList<>();
    // [김선욱님 코드] 무기 시스템
    //  - 실제 장착 무기는 Player 인벤토리 기준으로 관리하고,
    //    여기엔 "현재 사용 중인 무기의 타입"만 동기화해서 사용한다.
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
    private boolean vampireEffectActive = false;
    private Set<ItemType> eliteDropsGiven = new HashSet<>();
    private List<MeleeAttack> meleeAttacks = new ArrayList<>();
    private List<HitSpark> effects = new java.util.ArrayList<>();
    private boolean isWeaponAnimating = false;
    private ArrayList<Box> boxes = new ArrayList<>();
    private ArrayList<int[]> boxSpawnPoints = new ArrayList<>();
    
    // 🔹 무기 교체 쿨다운 (밀리초)
    private long lastWeaponSwapTime = 0;
    private static final long WEAPON_SWAP_COOLDOWN = 4500; // 4.5초 쿨다운 (애니메이션 3초 + 여유 1.5초)
    
    // 🔹 현재 재생 중인 BGM 인덱스 추적 (중복 재생 방지)
    private int currentMusicIndex = -1;

    // [수정] 방별 아이템 관리 (각 방에 드롭된 아이템을 방별로 저장)
    private java.util.Map<Integer, ArrayList<Item>> roomItems = new java.util.HashMap<>();
    
    // 🔹 스테이지별 방별 상자 열림 상태 관리 (스테이지 -> 방 ID -> 상자 위치 리스트)
    private java.util.Map<Integer, java.util.Map<Integer, java.util.Set<String>>> stageBoxes = new java.util.HashMap<>();


    private boolean keyW, keyA, keyS, keyD, keyG, keyF;
    private map.Minimap minimap;
    
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
    
    // [수정] 클리어된 방 목록 (한번 클리어한 방은 다시 적이 스폰되지 않음)
    private java.util.Set<Integer> clearedRooms = new java.util.HashSet<>();
    
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

        // [김민정님 코드] KeyHandler 초기화 및 리스너 추가 (플레이어 이동용)
        keyH = new KeyHandler(this);
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
        
     // 🖱️ 기본 시스템 커서 완전히 숨기기
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            BufferedImage blankCursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Cursor blankCursor = toolkit.createCustomCursor(blankCursorImg, new Point(0, 0), "blankCursor");
            setCursor(blankCursor);
        } catch (Exception e) {
        }

    }

    public void addBoxAt(int x, int y) {
        boxes.add(new Box(x, y));
    }

    // [김선욱님 코드] 중복 방지를 위한 존재 여부 확인 함수
    public boolean boxExistsAt(int x, int y) {
        for (Box b : boxes) {
            if (b.x == x && b.y == y) return true;
        }
        return false;
    }

    public void setupGame() {
        // [서충만님 코드] 맵 초기화
        tileManager = new TileManager(this);
        MapLoader.loadAllRooms(1); // 🔹 스테이지 1에서 시작
        currentRoom = MapLoader.getRoom(0);
        
        minimap = new map.Minimap();

        // [김민정님 코드] 플레이어 초기화
        player = new Player(this, keyH);
        player.x = Constants.TILE_SIZE * 10;
        player.y = Constants.TILE_SIZE * 6;

        // 🔫 플레이어 인벤토리의 현재 무기와 GamePanel의 currentWeapon 동기화
        syncCurrentWeaponFromPlayer();
        
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
    
    // [수정] 사망 시 게임 초기화 (스테이지 1, 0번룸부터 시작)
    private void resetGameOnDeath() {
        // 모든 적 제거
        enemies.clear();
        boss = null;
        
        // 클리어된 방 목록 초기화
        clearedRooms.clear();
        
        // 방별 아이템 목록 초기화
        roomItems.clear();
        
        // 🔹 스테이지별 상자 열림 상태 초기화
        stageBoxes.clear();
        
        // 맵 초기화 (스테이지 1, 0번룸)
        tileManager = new TileManager(this);
        MapLoader.loadAllRooms(1);
        currentRoom = MapLoader.getRoom(0);
        
        // 플레이어 스탯 초기화
        player = new Player(this, keyH);
        player.x = Constants.TILE_SIZE * 10;
        player.y = Constants.TILE_SIZE * 6;
        
        // 카메라 초기화
        cameraX = player.x - Constants.WINDOW_WIDTH / 2.0;
        cameraY = player.y - Constants.WINDOW_HEIGHT / 2.0;
        
        // 무기 초기화: 플레이어 생성 후 보유 무기에 맞춰 동기화
        syncCurrentWeaponFromPlayer();
        
        // 아이템 및 기타 초기화
        bullets.clear();
        items.clear();
        damageTexts.clear();
        keys.clear();
        acquiredItems.clear();
        
        // 🔹 게임오버 시 스테이지 1 BGM 재생 (죽은 스테이지 BGM 방지)
        soundManager.stop();
        // 스테이지 1로 리셋 (BGM도 스테이지 1로 재생)
        MapLoader.loadAllRooms(1);
        playStageMusic();
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

                if (vampireEffectActive) {
                    player.heal(10);
                    damageTexts.add(new DamageText(player.x, player.y - 10, "+10 HP", Color.PINK));
                }

                EnemyType type = enemy.type;
                boolean isElite =
                type == EnemyType.MINOTAUR ||
                type == EnemyType.GOLEM ||
                type == EnemyType.ICE_GOLEM ||
                                    type == EnemyType.HELL_KNIGHT;
                
                                if (isElite) {
                                    // 🔹 정예몹: 열쇠 100%
                                    keys.add(new Key(enemy.x, enemy.y));
                
                                    // 🔹 아직 안 나온 희귀 아이템만 필터링
                                    List<ItemType> remaining = new ArrayList<>(Arrays.asList(
                                        ItemType.DEMON_HORN,
                                        ItemType.HERMES_BOOTS,
                                        ItemType.RAPID_GLOVES,
                                        ItemType.VAMPIRE_TOOTH,
                                        ItemType.DRAGON_SCALE
                                    ));
                                    remaining.removeAll(eliteDropsGiven); // 이미 드롭된 건 제외
                
                                    // 🔹 남은 게 있으면 하나 랜덤 드롭
                                    if (!remaining.isEmpty()) {
                                        ItemType drop = remaining.get((int)(Math.random() * remaining.size()));
                                        items.add(new Item(enemy.x, enemy.y, drop));
                                        eliteDropsGiven.add(drop); // ✅ 드롭 기록 추가
                                    } else {
                                        // [김선욱님 코드] 희귀 아이템을 모두 얻은 이후 → 액티브 아이템 드롭
                                        ItemType[] activeDrops = {
                                            ItemType.RED_POTION,
                                            ItemType.ELIXIR,
                                            ItemType.GHOST_CLOAK
                                        };
                                    }
                                }
                                else {
                                    // 🔹 일반몹: 80% 확률로 기본 3종 드롭, 20%는 드롭 없음
                                    if (Math.random() < 0.8) {
                                        ItemType[] commonDrops = {
                                            ItemType.POWER_FRUIT,
                                            ItemType.LIFE_SEED,
                                            ItemType.WIND_CANDY
                                        };
                                        ItemType drop = commonDrops[(int)(Math.random() * commonDrops.length)];
                                        items.add(new Item(enemy.x, enemy.y, drop));
                                    }
                                }
                
                                enemiesToRemove.add(enemy);
                                soundManager.playSE(28); // [김민정님 코드] 적 사망 효과음
                            }
                        }

        enemies.removeAll(enemiesToRemove);
        enemies.addAll(enemiesToAdd);

        // [수정] 방의 모든 적을 처치했을 때 방을 클리어된 목록에 추가
        if (currentRoom != null && !hasAliveEnemies() && !clearedRooms.contains(currentRoom.getRoomId())) {
            clearedRooms.add(currentRoom.getRoomId());
        }

        // [김선욱님 코드] 총알 업데이트
        bullets.removeIf(b -> { b.update(); return !b.isActive(); });

        // [김선욱님 코드] 근접 공격(MeleeAttack) 업데이트
        meleeAttacks.removeIf(ma -> {
            ma.update();
            return !ma.isActive();
        });
        
        if (keyH.qPressed) {
            updateCursorToWeapon();
            keyH.qPressed = false;
        }
        
        // [김선욱님 코드] 근접 공격 업데이트
        for (MeleeAttack ma : meleeAttacks) {
            ma.update(cameraX, cameraY);
        }

        // 💥 [김선욱님 코드] 히트 스파크 효과 업데이트
        effects.removeIf(e -> !e.isActive());
        
        // [김선욱님 코드] 총알-적 충돌 감지
        checkBulletCollisions();

        // [김선욱님 코드] 근접 공격-적 충돌 감지
        checkMeleeCollisions();

        // [김선욱님 코드] 아이템 획득 체크
        checkItemPickups();

        // [김선욱님 코드] 열쇠 획득 체크
        checkKeyPickups();

        // [적 공격 체크] 일반 적의 근접 공격 및 투사체 충돌 체크
        checkEnemyAttacks();
        
        checkBoxCollision(); // ✅ 상자 충돌 체크


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
        
        // [수정] 보스 업데이트 (소리는 Boss 클래스 내부에서 재생)
        if (boss != null && boss.alive) {
            boss.update((int)playerX, (int)playerY);
        }
        
        // 🔹 보스 처치 체크 (보스가 죽으면 클리어 화면)
        if (boss != null && !boss.alive) {
            gameState = gameClearState;
            soundManager.stop();
            soundManager.playSE(11); // 클리어 효과음 (stageclear.wav)
        }
    }
    
    // [서충만님 코드] 문 충돌 체크 및 방 이동: 플레이어가 문 타일('D')에 닿으면 연결된 방으로 이동
    private void checkDoorCollision(int tileX, int tileY, char[][] map) {
        if (currentRoom == null) return;
        
        // [수정] 방의 모든 적을 잡아야 문 이동 가능
        if (hasAliveEnemies()) {
            return;
        }
        
        // [수정] 플레이어가 실제로 문 타일('D')에 들어가면 이동
        String direction = null;
        
        // 플레이어의 현재 타일과 주변 타일 체크
        int mapHeight = map.length;
        int mapWidth = map[0].length;
        
        // 현재 타일이 문인지 확인
        if (tileY >= 0 && tileY < mapHeight && tileX >= 0 && tileX < mapWidth) {
            if (map[tileY][tileX] == 'D') {
                // 문의 위치로 방향 판단
                if (tileY <= 2) {
                    direction = "NORTH";
                } else if (tileY >= mapHeight - 3) {
                    direction = "SOUTH";
                } else if (tileX <= 2) {
                    direction = "WEST";
                } else if (tileX >= mapWidth - 3) {
                    direction = "EAST";
                }
            }
        }
        
        // 현재 타일이 문이 아니면 주변 타일도 체크 (플레이어가 타일 경계에 있을 수 있음)
        if (direction == null) {
            // 위쪽 타일
            if (tileY > 0 && tileY - 1 < mapHeight && tileX < mapWidth && map[tileY - 1][tileX] == 'D') {
                direction = "NORTH";
            }
            // 아래쪽 타일
            else if (tileY + 1 < mapHeight && tileX < mapWidth && map[tileY + 1][tileX] == 'D') {
                direction = "SOUTH";
            }
            // 왼쪽 타일
            else if (tileX > 0 && tileY < mapHeight && tileX - 1 < mapWidth && map[tileY][tileX - 1] == 'D') {
                direction = "WEST";
            }
            // 오른쪽 타일
            else if (tileX + 1 < mapWidth && tileY < mapHeight && map[tileY][tileX + 1] == 'D') {
                direction = "EAST";
            }
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
                    
                    // [수정] 방 이동 시 현재 방의 아이템만 표시
                    int nextRoomId = nextRoom.getRoomId();
                    items.clear();
                    if (roomItems.containsKey(nextRoomId)) {
                        items.addAll(roomItems.get(nextRoomId));
                    }
                    
                    // 🔹 새 방의 상자 찾기 (스테이지별 방별 열림 상태 복원)
                    boxes.clear();
                    if (currentRoom != null) {
                        int currentStage = MapLoader.getCurrentStage();
                        int currentRoomId = nextRoom.getRoomId();
                        java.util.Map<Integer, java.util.Set<String>> stageBoxMap = stageBoxes.getOrDefault(currentStage, new java.util.HashMap<>());
                        java.util.Set<String> openedBoxKeys = stageBoxMap.getOrDefault(currentRoomId, new java.util.HashSet<>());
                        
                        char[][] roomMap = currentRoom.getMap();
                        if (roomMap != null) {
                            for (int y = 0; y < roomMap.length; y++) {
                                for (int x = 0; x < roomMap[y].length; x++) {
                                    if (roomMap[y][x] == 'C') {
                                        int boxX = x * Constants.TILE_SIZE;
                                        int boxY = y * Constants.TILE_SIZE;
                                        String boxKey = boxX + "," + boxY;
                                        
                                        Box box = new Box(boxX, boxY);
                                        // 이미 열린 상자는 열린 상태로 복원
                                        if (openedBoxKeys.contains(boxKey)) {
                                            box.open();
                                        }
                                        boxes.add(box);
                                    }
                                }
                            }
                        }
                    }
                    
                    // [서상원님 코드] 새 방으로 이동 시 적 스폰 포인트 찾기 및 스폰
                    findEnemySpawnPoints();
                    spawnEnemiesFromMap();
                    // 🔹 방 이동 시에는 BGM 재생하지 않음 (같은 스테이지 내 이동)
                    soundManager.playSE(18); // [김민정님 코드] 문 열리는 소리
                }
            }
        }
    }
    
    // [서상원님 코드] 맵에서 E 타일(일반몹), L 타일(정예몹), B 타일(보스) 위치 찾기
    private ArrayList<int[]> eliteSpawnPoints = new ArrayList<>();
    private ArrayList<int[]> bossSpawnPoints = new ArrayList<>();
    
    private void findEnemySpawnPoints() {
        enemySpawnPoints.clear();
        eliteSpawnPoints.clear();
        bossSpawnPoints.clear();
        if (currentRoom == null) return;
        
        char[][] map = currentRoom.getMap();
        if (map == null) return;
        
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                if (map[y][x] == 'E') {
                    enemySpawnPoints.add(new int[]{x, y});
                } else if (map[y][x] == 'L') {
                    eliteSpawnPoints.add(new int[]{x, y});
                } else if (map[y][x] == 'B') {
                    bossSpawnPoints.add(new int[]{x, y});
                }
            }
        }
    }
    
    // [서상원님 코드] 스테이지별 일반 적 타입 랜덤 선택
    private EnemyType getRandomEnemyTypeForStage(int stage) {
        EnemyType[] types;
        
        switch (stage) {
            case 1:
                types = new EnemyType[]{EnemyType.SLIME, EnemyType.WOLF, EnemyType.GOBLIN};
                break;
            case 2:
                types = new EnemyType[]{EnemyType.SNAKE, EnemyType.MUDGOLEM, EnemyType.SPORE_FLOWER};
                break;
            case 3:
                types = new EnemyType[]{EnemyType.FROZEN_KNIGHT, EnemyType.YETI, EnemyType.SNOW_MAGE};
                break;
            case 4:
                // ORC 제거
                types = new EnemyType[]{
                    EnemyType.BOMB_SKULL, 
                    EnemyType.HELL_HOUND, 
                    EnemyType.FIRE_IMP, 
                    EnemyType.MAGMA_SLIME_BIG,
                    EnemyType.MAGMA_SLIME_SMALL
                };
                break;
            case 5:
                return null;
            default:
                types = new EnemyType[]{EnemyType.SLIME};
        }
        
        return types[(int)(Math.random() * types.length)];
    }
    
    // [수정] 스테이지별 정예몹 타입 반환
    private EnemyType getEliteEnemyTypeForStage(int stage) {
        switch (stage) {
            case 1:
                return EnemyType.MINOTAUR;
            case 2:
                return EnemyType.GOLEM;
            case 3:
                return EnemyType.ICE_GOLEM;
            case 4:
                return EnemyType.HELL_KNIGHT;
            default:
                return EnemyType.MINOTAUR;
        }
    }
    
    // [서상원님 코드] 맵의 E 타일(일반몹)과 L 타일(정예몹) 위치에서 적 스폰
    private void spawnEnemiesFromMap() {
        // [수정] 이미 클리어된 방이면 적을 스폰하지 않음
        if (currentRoom != null && clearedRooms.contains(currentRoom.getRoomId())) {
            enemies.clear();
            return;
        }
        
        int currentStage = MapLoader.getCurrentStage();
        enemies.clear();
        boss = null; // 보스 초기화
        
        // 🔹 보스 스폰 (B 타일) - 모든 스테이지에서 B 타일이 있으면 보스 스폰
        if (!bossSpawnPoints.isEmpty()) {
            int[] spawnPoint = bossSpawnPoints.get(0); // 첫 번째 B 타일 위치
            int tileX = spawnPoint[0];
            int tileY = spawnPoint[1];
            
            double spawnX = (tileX + 0.5) * Constants.TILE_SIZE;
            double spawnY = (tileY + 0.5) * Constants.TILE_SIZE;
            
            boss = new Boss(spawnX, spawnY, soundManager);
            return; // 보스가 있으면 일반몹/정예몹 스폰 안 함
        }
        
        // 일반몹 스폰 (E 타일)
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
        
        // 정예몹 스폰 (L 타일)
        EnemyType eliteType = getEliteEnemyTypeForStage(currentStage);
        for (int[] spawnPoint : eliteSpawnPoints) {
            int tileX = spawnPoint[0];
            int tileY = spawnPoint[1];
            
            double spawnX = (tileX + 0.5) * Constants.TILE_SIZE;
            double spawnY = (tileY + 0.5) * Constants.TILE_SIZE;
            
            enemies.add(new Enemy(eliteType, spawnX, spawnY));
        }
    }
    
    // [수정] 방에 살아있는 적이 있는지 확인
    private boolean hasAliveEnemies() {
        for (Enemy enemy : enemies) {
            if (enemy.alive) {
                return true;
            }
        }
        if (boss != null && boss.alive) {
            return true;
        }
        return false;
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
    private void checkBoxCollision() {
        int playerScreenX = (int)(player.x - cameraX);
        int playerScreenY = (int)(player.y - cameraY);
        Rectangle playerRect = new Rectangle(playerScreenX, playerScreenY, Constants.TILE_SIZE, Constants.TILE_SIZE);

        for (Box box : boxes) {
            Rectangle boxRect = new Rectangle((int)(box.x - cameraX), (int)(box.y - cameraY),
                                              Constants.TILE_SIZE, Constants.TILE_SIZE);

            if (!box.opened && playerRect.intersects(boxRect)) {
                if (enemies.isEmpty()) {
                    box.open();
                    soundManager.playSE(17);
                    // 🔹 스테이지별 방별 상자 열림 상태 저장
                    if (currentRoom != null) {
                        int currentStage = MapLoader.getCurrentStage();
                        int roomId = currentRoom.getRoomId();
                        String boxKey = box.x + "," + box.y;
                        stageBoxes.putIfAbsent(currentStage, new java.util.HashMap<>());
                        stageBoxes.get(currentStage).putIfAbsent(roomId, new java.util.HashSet<>());
                        stageBoxes.get(currentStage).get(roomId).add(boxKey);
                    }
                    soundManager.playSE(15);

                    double roll = Math.random(); // 0~1 사이 난수

                    if (roll < 0.5) {
                        // 🗡 무기 드롭 (50%)
                        WeaponType[] weaponPool = {
                            WeaponType.PISTOL,
                            WeaponType.SHOTGUN,
                            WeaponType.SNIPER,
                            WeaponType.DAGGER,
                            WeaponType.LONG_SWORD,
                            WeaponType.KNIGHT_SWORD
                        };
                        WeaponType dropWeapon = weaponPool[(int)(Math.random() * weaponPool.length)];

                        // [김선욱님 코드] 무기 등급 랜덤 부여
                        WeaponRarity rarity = WeaponRarity.getRandomRarity();
                        dropWeapon.setRarity(rarity);


                        Item dropped = new Item(box.x, box.y, dropWeapon);
                        items.add(dropped);
                        // ✅ 방별 아이템 저장
                        if (currentRoom != null) {
                            int roomId = currentRoom.getRoomId();
                            roomItems.putIfAbsent(roomId, new ArrayList<>());
                            roomItems.get(roomId).add(dropped);
                        }

                    } else {
                        // ✨ 액티브 아이템 드롭 (50%)
                        ItemType[] activePool = {
                            ItemType.RED_POTION,
                            ItemType.ELIXIR,
                            ItemType.GHOST_CLOAK
                        };
                        ItemType dropItem = activePool[(int)(Math.random() * activePool.length)];
                        Item dropped = new Item(box.x, box.y, dropItem);
                        items.add(dropped);
                        // ✅ 방별 아이템 저장
                        if (currentRoom != null) {
                            int roomId = currentRoom.getRoomId();
                            roomItems.putIfAbsent(roomId, new ArrayList<>());
                            roomItems.get(roomId).add(dropped);
                        }
                    }
                }
            }
        }
    }
    // [로딩 화면] 로딩 완료 후 몹 소환
    private void spawnEnemiesAfterLoading() {
        // [서상원님 코드] 현재 방의 적 스폰 포인트 찾기 및 스폰
        findEnemySpawnPoints();
        spawnEnemiesFromMap();
        boss = null;
        
        // 🔹 상자 초기화 및 새 방의 상자 찾기 (스테이지별 방별 열림 상태 복원)
        boxes.clear();
        if (currentRoom != null) {
            int currentStage = MapLoader.getCurrentStage();
            int currentRoomId = currentRoom.getRoomId();
            java.util.Map<Integer, java.util.Set<String>> stageBoxMap = stageBoxes.getOrDefault(currentStage, new java.util.HashMap<>());
            java.util.Set<String> openedBoxKeys = stageBoxMap.getOrDefault(currentRoomId, new java.util.HashSet<>());
            
            char[][] map = currentRoom.getMap();
            if (map != null) {
                for (int y = 0; y < map.length; y++) {
                    for (int x = 0; x < map[y].length; x++) {
                        if (map[y][x] == 'C') {
                            int boxX = x * Constants.TILE_SIZE;
                            int boxY = y * Constants.TILE_SIZE;
                            String boxKey = boxX + "," + boxY;
                            
                            Box box = new Box(boxX, boxY);
                            // 이미 열린 상자는 열린 상태로 복원
                            if (openedBoxKeys.contains(boxKey)) {
                                box.open();
                            }
                            boxes.add(box);
                        }
                    }
                }
            }
        }
        
        // [수정] 현재 방의 아이템 로드
        if (currentRoom != null) {
            int roomId = currentRoom.getRoomId();
            items.clear();
            if (roomItems.containsKey(roomId)) {
                items.addAll(roomItems.get(roomId));
            }
        }
        
        // 🔹 스테이지 BGM 재생 (로딩 화면 끝난 후)
        playStageMusic();
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
    
    // [김선욱님 코드] 총알/근접 공격 발사 처리
    private void shoot() {
        if (gameState != playState) return; // [김민정님 코드] 플레이 중이 아니면 발사 불가

        // ✅ 항상 Player 인벤토리 기준으로 현재 무기 타입을 먼저 동기화
        if (player == null || player.getCurrentWeapon() == null) return;
        currentWeapon = player.getCurrentWeapon().getType();

        long now = System.currentTimeMillis();
        long delay = (long)(currentWeapon.getAttackSpeed() * 1000 / (1 + player.getAttackSpeedBonus()));
        if (now - lastShootTime < delay) return;
        lastShootTime = now;

        // [김민정님 코드] 무기 발사 사운드
        switch (currentWeapon) {
            // 근접 무기 (검)
            case DAGGER: soundManager.playSE(0); break; // [김민정님 코드] 단검 소리
            case LONG_SWORD: soundManager.playSE(1); break; // [김민정님 코드] 롱소드 소리
            case KNIGHT_SWORD: soundManager.playSE(2); break; // [김민정님 코드] 기사검 소리

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

        // [김선욱님 코드] 근접 무기 공격 처리 (총알 대신 휘두름 이펙트 생성)
        if (currentWeapon == WeaponType.DAGGER ||
            currentWeapon == WeaponType.LONG_SWORD ||
            currentWeapon == WeaponType.KNIGHT_SWORD) {

            double range = currentWeapon.getRange();
            double damage = currentWeapon.getDamage() * player.getAttackMultiplier();

            meleeAttacks.add(new MeleeAttack(player, angle, range, damage)); // ✅ 수정됨
            return;
        }


        // [김선욱님 코드] 원거리 무기 총알 발사 처리
        if (currentWeapon == WeaponType.SHOTGUN) {
            int pellets = 5;
            double spread = Math.toRadians(15);
            double start = angle - spread / 2;
            double step = spread / (pellets - 1);
            for (int i = 0; i < pellets; i++) {
                double a = start + step * i;
                bullets.add(new Bullet(px, py, a, bulletSpeed,
                    currentWeapon.getDamage(),
                    currentWeapon.getRange(),
                    currentWeapon));
            }
        } else {
            bullets.add(new Bullet(px, py, angle, bulletSpeed,
                currentWeapon.getDamage(),
                currentWeapon.getRange(),
                currentWeapon));
        }
        

        // ✅ 탄막이 실제로 발사될 때만 애니메이션 재생
     currentWeapon.playCursorAnimation();
        
    }

    /** [김선욱님 코드] 근접 공격 충돌 감지 */
    private void checkMeleeCollisions() {
        for (MeleeAttack ma : meleeAttacks) {
            if (!ma.isActive()) continue;

            Shape hitbox = ma.getHitbox();
            if (hitbox == null) continue;  // ✅ NullPointer 방지 (중요!)

            // [김선욱님 코드] 일반 몬스터와 충돌 체크
            for (Enemy e : enemies) {
                if (!e.alive) continue;
                if (ma.hasHit(e)) continue; // ✅ 이미 맞은 적이면 스킵

                double drawY_world = e.y - (e.hitHeight - 48);
                double spriteCenterX = e.x + (e.drawWidth / 2.0);
                double spriteCenterY = drawY_world + (e.drawHeight / 2.0);

                Rectangle enemyRect = new Rectangle(
                    (int)(spriteCenterX - e.hitWidth / 2.0),
                    (int)(spriteCenterY - e.hitHeight / 2.0),
                    (int)e.hitWidth,
                    (int)e.hitHeight
                );

                if (hitbox.intersects(enemyRect)) {
                    if (ma.hasHit(e)) continue;
                    double dmg = ma.getDamage();
                    e.takeDamage((int)dmg);
                    ma.markHit(e);

                    effects.add(new HitSpark(spriteCenterX, spriteCenterY)); // ✅ 오류 해결됨!

                    Color dmgColor = dmg >= 50 ? Color.RED : Color.ORANGE;
                    damageTexts.add(new DamageText(spriteCenterX, spriteCenterY - 10,
                            String.valueOf((int)dmg), dmgColor));
                }

            }

            // [김선욱님 코드] 보스 충돌 체크
            if (boss != null && boss.alive) {
                if (ma.hasHit(boss)) continue; // ✅ 보스 중복 타격 방지

                double drawY_world = boss.y - (boss.hitHeight - 48);
                double spriteCenterX = boss.x + (boss.drawWidth / 2.0);
                double spriteCenterY = drawY_world + (boss.drawHeight / 2.0);

                Rectangle bossRect = new Rectangle(
                    (int)(spriteCenterX - boss.hitWidth / 2.0),
                    (int)(spriteCenterY - boss.hitHeight / 2.0),
                    (int)boss.hitWidth,
                    (int)boss.hitHeight
                );

                if (hitbox.intersects(bossRect)) {
                    double dmg = ma.getDamage();
                    boss.takeDamage((int)dmg);
                    ma.markHit(boss); // ✅ 보스도 1회만 타격

                    Color dmgColor = dmg >= 50 ? Color.RED : Color.ORANGE;
                    damageTexts.add(new DamageText(spriteCenterX, spriteCenterY - 10,
                            String.valueOf((int)dmg), dmgColor));

                }
            }
        }

        // [김선욱님 코드] 수명 끝난 근접 공격 제거
        meleeAttacks.removeIf(ma -> !ma.isActive());
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
        // 🔹 아이템 습득 범위 축소 (기존 TILE_SIZE의 70%로 축소)
        int pickupRange = (int)(Constants.TILE_SIZE * 0.7);
        Rectangle playerRect = new Rectangle(
                (int) playerX - pickupRange / 2,
                (int) playerY - pickupRange / 2,
                pickupRange, pickupRange);

        // 인덱스 기반 반복문 사용 (순회 중 add/remove 허용)
        for (int i = 0; i < items.size(); ) {
            Item item = items.get(i);

            if (item.isPicked() || !playerRect.intersects(item.getBounds())) {
                i++;
                continue;
            }

            // 🔹 상자/드롭 무기 처리 (근거리 ↔ 근거리, 원거리 ↔ 원거리만 교체)
            if (item.isWeaponPickup()) {
                boolean success = handleWeaponItemPickup(item);
                if (success) {
                    // 방별 아이템 목록에서도 제거
                    if (currentRoom != null) {
                        int roomId = currentRoom.getRoomId();
                        if (roomItems.containsKey(roomId)) {
                            roomItems.get(roomId).remove(item);
                        }
                    }
                    // 아이템 리스트에서 제거
                    items.remove(i);
                    // 무기 교체 성공 시 사운드 재생
                    soundManager.playSE(15);
                    continue; // i 증가하지 않음 (이미 다음 요소로 이동됨)
                } else {
                    // 교체 조건이 아니면 다음 프레임에 다시 시도
                    i++;
                    continue;
                }
            }

            // 🔹 액티브 아이템(포션/유령망토)만 애니메이션
            ItemType t = item.getType();
            boolean isActive =
                    t == ItemType.RED_POTION ||
                    t == ItemType.ELIXIR ||
                    t == ItemType.GHOST_CLOAK;

            if (isActive && !player.obtainingItem) {
                player.playObtainEffect(item.getPickupImage());
            }

            item.pickUp();
            acquiredItems.add(item.getType());
            applyItemEffect(item.getType());

            // [수정] 방별 아이템 목록에서도 제거
            if (currentRoom != null) {
                int roomId = currentRoom.getRoomId();
                if (roomItems.containsKey(roomId)) {
                    roomItems.get(roomId).remove(item);
                }
            }

            // [김민정님 코드] 아이템 획득 사운드
            soundManager.playSE(15);

            // 아이템 리스트에서 제거
            items.remove(i);
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

                player.currentKeyCount++;
                soundManager.playSE(12);
                return true;
            }
            return false;
        });
    }

     // [김선욱님 코드] 아이템 효과 적용
     private void applyItemEffect(ItemType type) {
        if (type == null) return;

        // 🔹 버프 아이템 (공격력/속도 등)
        if (type.getAttackBuff() != 0) {
            player.addAttackBonus(type.getAttackBuff());
        }
        if (type.getSpeedBuff() != 0) {
            player.addSpeedBonus(type.getSpeedBuff());
        }
        if (type.getAttackSpeedBuff() != 0) {
            player.addAttackSpeedBonus(type.getAttackSpeedBuff());
        }

        // 🔹 체력 관련/액티브 아이템 → 인벤토리 수량만 증가
        if (type == ItemType.RED_POTION) {
            player.redPotionCount++;
        }
        else if (type == ItemType.ELIXIR) {
            player.elixirCount++;
        }
        else if (type.getHpBuff() != 0) {
            // ❗ 일반적인 HP 버프 아이템만 최대체력 증가
            player.addMaxHP(type.getHpBuff());
        }

        // 🔹 특수 효과 아이템
        if (type == ItemType.VAMPIRE_TOOTH) {
            vampireEffectActive = true;
            damageTexts.add(new DamageText(player.x, player.y - 20, "🩸 흡혈 효과 발동!", Color.PINK));
        }

        if (type == ItemType.GHOST_CLOAK) {
            // 🔹 유령 망토는 인벤토리 수량만 증가 (E키로 사용)
            player.ghostCloakCount++;
        }

        // 🔹 공통 이펙트 표시 (중복 방지)
        if (type != ItemType.RED_POTION && type != ItemType.ELIXIR) {
            damageTexts.add(new DamageText(player.x, player.y - 20, "+" + type.getName(), Color.CYAN));
        }
    }

    // [추가] 상자에서 떨어진 무기(WeaponType) 아이템 처리
    // 반환값: true = 교체 성공, false = 교체 실패 (조건 불만족)
    private boolean handleWeaponItemPickup(Item item) {
        WeaponType wt = item.getWeaponType();
        if (wt == null) return false;

        // 🔹 무기 교체 쿨다운 체크
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWeaponSwapTime < WEAPON_SWAP_COOLDOWN) {
            // 쿨다운 중이면 교체 불가
            return false;
        }

        // 현재 들고 있는 무기 가져오기
        Weapon currentWeapon = player.getCurrentWeapon();
        if (currentWeapon == null) {
            // 무기가 없으면 교체 불가
            return false;
        }

        // 현재 무기의 타입 확인 (근거리/원거리)
        boolean currentIsRanged = currentWeapon.getType().isRanged();
        boolean pickupIsRanged = wt.isRanged();

        // 🔹 현재 들고 있는 무기와 상자 무기가 같은 타입(근거리↔근거리, 원거리↔원거리)일 때만 교체
        if (currentIsRanged != pickupIsRanged) {
            // 타입이 다르면 교체 불가 (플레이어가 직접 Q키로 바꾸지 않는 이상)
            return false;
        }

        // 0: 근접, 1: 원거리 (슬롯 인덱스)
        int slotIndex = pickupIsRanged ? 1 : 0;

        // 인벤토리 크기 보장
        while (player.inventory.size() <= slotIndex) {
            player.inventory.add(null);
        }

        Weapon current = player.inventory.get(slotIndex);

        // 기존 무기가 있었다면 아이템으로 바닥에 드롭
        if (current != null) {
            Item dropped = new Item(player.x, player.y, current.getType());
            items.add(dropped);
        }

        // 새 무기 장착
        Weapon newWeapon = new Weapon(wt);
        player.inventory.set(slotIndex, newWeapon);

        // 현재 무기도 갱신 (currentWeaponIndex가 바뀌지 않았으므로 그대로 유지)
        syncCurrentWeaponFromPlayer();

        // 애니메이션
        if (!player.obtainingItem) {
            player.playObtainEffect(item.getPickupImage());
        }

        // 🔹 무기 교체 쿨다운 시작
        lastWeaponSwapTime = System.currentTimeMillis();

        return true; // 성공
    }
    
    // [적 공격 체크] 일반 적의 근접 공격 및 투사체 충돌 체크
    private void checkEnemyAttacks() {
        double playerX = player.x;
        double playerY = player.y;
        
        // 플레이어 히트박스 생성
        Rectangle playerRect = new Rectangle(
            (int)playerX,
            (int)playerY,
            Constants.TILE_SIZE,
            Constants.TILE_SIZE
        );
        
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
            
            // 근접 공격 체크
            if (enemy.canAttackPlayer((int)playerX, (int)playerY)) {
                int damage = enemy.getAttackDamage();
                player.receiveDamage(damage);
                damageTexts.add(new DamageText(playerX, playerY - 10,
                        String.valueOf(damage), Color.RED));
            }
            
            // 🔹 원거리 투사체 충돌 체크
            int projectileDamage = enemy.checkProjectileCollision(playerRect);
            if (projectileDamage > 0) {
                player.receiveDamage(projectileDamage);
                damageTexts.add(new DamageText(playerX, playerY - 10,
                        String.valueOf(projectileDamage), Color.RED));
            }
        }
        
        // 🔹 보스 공격 체크
        if (boss != null && boss.alive) {
            if (boss.canAttackPlayer((int)playerX, (int)playerY)) {
                int damage = boss.getAttackDamage();
                player.receiveDamage(damage);
                damageTexts.add(new DamageText(playerX, playerY - 10,
                        String.valueOf(damage), Color.RED));
            }
            
            // 🔹 보스 투사체 충돌 체크
            for (Boss.BossProjectile projectile : boss.getProjectiles()) {
                if (!projectile.isActive()) continue;
                
                Rectangle projectileRect = projectile.getHitBox();
                if (playerRect.intersects(projectileRect)) {
                    int damage = projectile.getDamage();
                    player.receiveDamage(damage);
                    projectile.deactivate();
                    damageTexts.add(new DamageText(playerX, playerY - 10,
                            String.valueOf(damage), Color.RED));
                }
            }
        }
    }

    // ---------------------------------------------------------
    // [민정 추가] 다음 스테이지 이동 시스템
    // ---------------------------------------------------------

    // 1. 현재 좌표의 타일 종류를 가져오는 메서드
    public char getTileChar(int worldX, int worldY) {
        if (currentRoom == null) return ' '; 

        int tileX = worldX / Constants.TILE_SIZE;
        int tileY = worldY / Constants.TILE_SIZE;

        char[][] map = currentRoom.getMap();
        
        if (tileY >= 0 && tileY < map.length && tileX >= 0 && tileX < map[0].length) {
            return map[tileY][tileX];
        }
        return ' ';
    }

    // 2. 맵 교체 및 초기화 메서드
    public void nextStage() {
        int currentStage = MapLoader.getCurrentStage();
        int nextStage = currentStage + 1;

        // 5탄 이후 엔딩 처리
        if (nextStage > map.StageInfo.MAX_STAGE) {
            gameState = titleState; 
            soundManager.stop();
            soundManager.playMusic(29); 
            return;
        }

        // 로딩 화면 시작
        gameState = loadingState;
        loadingStartTime = System.currentTimeMillis();

        // 맵 데이터 불러오기
        MapLoader.loadAllRooms(nextStage);
        currentRoom = MapLoader.getRoom(0); 
        
        // 🔹 미니맵 초기화 (새 스테이지에 맞게 재생성)
        minimap = new map.Minimap();

        // ✅ 새로운 스테이지에서는 방 클리어/아이템 정보 초기화
        clearedRooms.clear();
        roomItems.clear();

        // 플레이어 위치 초기화 (setupGame과 동일한 위치)
        player.x = Constants.TILE_SIZE * 10;
        player.y = Constants.TILE_SIZE * 6;
        
        // 중요: 다음 판으로 가면 열쇠와 적들을 초기화
        player.currentKeyCount = 0; 
        keys.clear();
        bullets.clear();
        items.clear();
        damageTexts.clear();
        enemies.clear();
        boxes.clear(); // 상자도 초기화
        
        // 🔹 새 스테이지의 첫 방 상자 스폰 (스테이지별 방별 열림 상태 복원)
        if (currentRoom != null) {
            int currentRoomId = currentRoom.getRoomId();
            java.util.Map<Integer, java.util.Set<String>> stageBoxMap = stageBoxes.getOrDefault(nextStage, new java.util.HashMap<>());
            java.util.Set<String> openedBoxKeys = stageBoxMap.getOrDefault(currentRoomId, new java.util.HashSet<>());
            
            char[][] map = currentRoom.getMap();
            if (map != null) {
                for (int y = 0; y < map.length; y++) {
                    for (int x = 0; x < map[y].length; x++) {
                        if (map[y][x] == 'C') {
                            int boxX = x * Constants.TILE_SIZE;
                            int boxY = y * Constants.TILE_SIZE;
                            String boxKey = boxX + "," + boxY;
                            
                            Box box = new Box(boxX, boxY);
                            // 이미 열린 상자는 열린 상태로 복원
                            if (openedBoxKeys.contains(boxKey)) {
                                box.open();
                            }
                            boxes.add(box);
                        }
                    }
                }
            }
        }
        
        // 이전 스테이지 BGM 정지 (로딩 화면 끝난 후 playStageMusic()에서 재생)
        soundManager.stop();
        
    }

    private void updateCursorToWeapon() {
        try {
            // ✅ Player 기준으로 현재 무기 타입을 다시 동기화
            if (player == null || player.getCurrentWeapon() == null) return;
            currentWeapon = player.getCurrentWeapon().getType();

            BufferedImage img = currentWeapon.getWeaponImage();
            if (img == null) {
                setCursor(Cursor.getDefaultCursor());
                return;
            }

            // ✅ OS가 허용하는 커서 크기 조회
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension best = tk.getBestCursorSize(img.getWidth(), img.getHeight());
            // 일부 환경은 0,0을 돌려줌(커스텀 커서 미지원)
            if (best.width == 0 || best.height == 0) {
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                return;
            }

            // ✅ 원하는 확대 배율(2~3배 권장) → 하지만 OS 허용치로 clamp
            int desiredScale = 2; // 필요시 3으로 올려도 됩니다
            int reqW = Math.min(img.getWidth() * desiredScale, best.width);
            int reqH = Math.min(img.getHeight() * desiredScale, best.height);
            reqW = Math.max(16, reqW);
            reqH = Math.max(16, reqH);

            // 부드러운 스케일링
            BufferedImage scaled = new BufferedImage(reqW, reqH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(img, 0, 0, reqW, reqH, null);
            g2.dispose();

            // ✅ 핫스팟(무기 손잡이 쪽 느낌으로 아래쪽)
            Point hotspot = new Point(reqW / 2, (int)(reqH * 0.75));
            // 핫스팟은 반드시 이미지 영역 안이어야 함
            hotspot.x = Math.max(0, Math.min(hotspot.x, reqW - 1));
            hotspot.y = Math.max(0, Math.min(hotspot.y, reqH - 1));

            Cursor custom = tk.createCustomCursor(scaled, hotspot, currentWeapon.getName());

            // ✅ 패널 + 상위 윈도우 둘 다에 적용 (윈도우가 떠있을 때)
            SwingUtilities.invokeLater(() -> {
                setCursor(custom);
                Window w = SwingUtilities.getWindowAncestor(this);
                if (w != null) w.setCursor(custom);
            });

        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> setCursor(Cursor.getDefaultCursor()));
        }
        
        setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
        	    new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
        	    new Point(0, 0),
        	    "invisibleCursor"
        	));
    }

    // [김선욱님 코드] 무기 변경
    //  - Player 인벤토리에서 실제 보유 중인 무기만 순환
    private void changeWeapon(boolean next) {
        if (player == null) return;

        // 🔹 무기 교체 쿨다운 체크
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWeaponSwapTime < WEAPON_SWAP_COOLDOWN) {
            // 쿨다운 중이면 교체 불가
            return;
        }

        // 1) Player 인벤토리에서 현재 무기 인덱스 변경
        player.swapWeapon();

        // 2) Player 기준으로 currentWeapon을 다시 동기화 + 커서 갱신
        updateCursorToWeapon();
        
        // 🔹 무기 교체 쿨다운 시작
        lastWeaponSwapTime = System.currentTimeMillis();
    }

    public void syncCurrentWeaponFromPlayer() {
        if (player == null) return;
        Weapon w = player.getCurrentWeapon();
        if (w != null) {
            currentWeapon = w.getType();
        }
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
        
        // [김선욱님 코드] 근접 공격(검기) 그리기
        for (MeleeAttack ma : meleeAttacks) {
            ma.draw(g2, cameraX, cameraY); // ✅ 카메라 보정 전달
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

        // 💥 [김선욱님 코드] 히트 스파크 이펙트 그리기
        for (HitSpark s : effects) {
            s.draw(g2, cameraX, cameraY);
        }
        
        for (Box box : boxes) {
            box.draw(g2, cameraX, cameraY);
        }

        
        if (currentWeapon != null) {
            currentWeapon.drawCursor(g2, mouseX, mouseY, false);

            // 🔹 애니메이션이 끝났는지 감시
            if (!currentWeapon.isAnimating() && isWeaponAnimating) {
                isWeaponAnimating = false;
            }
        }
        
        // [김민정님 코드] HUD 그리기 (기존 drawPlayerHUD 대신 ui.draw 사용)
        ui.draw(g2);
        if (minimap != null && currentRoom != null) {
            minimap.render(g2, currentRoom.getRoomId());
        }
        g2.dispose();
    }
    
    // [서상원님 코드] 키보드 입력 처리
    @Override
    public void keyTyped(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_G) keyG = true;
        if (code == KeyEvent.VK_F) keyF = true;
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
            if (code == KeyEvent.VK_G) keyG = true;
            if (code == KeyEvent.VK_F) keyF = true;
            
            if (code == KeyEvent.VK_1) {
                if (player != null) {
                    player.selectedItemIndex = 0;
                }
            }
            if (code == KeyEvent.VK_2) {
                if (player != null) {
                    player.selectedItemIndex = 1;
                }
            }
            if (code == KeyEvent.VK_3) {
                if (player != null) {
                    player.selectedItemIndex = 2;
                }
            }

            if (code == KeyEvent.VK_E) {
                // 선택된 아이템 슬롯 사용 (빨간 물약 / 엘릭서 / 유령 망토)
                if (player != null) {
                    if (player.selectedItemIndex == 0 && player.redPotionCount > 0) {
                        player.heal(30);
                        player.redPotionCount--;
                        damageTexts.add(new DamageText(player.x, player.y - 20, "❤️ HP +30", Color.PINK));
                        soundManager.playSE(16);
                    } else if (player.selectedItemIndex == 1 && player.elixirCount > 0) {
                        player.heal(player.getMaxHP());
                        player.elixirCount--;
                        damageTexts.add(new DamageText(player.x, player.y - 20, "💖 체력 완전 회복!", Color.MAGENTA));
                        soundManager.playSE(16);
                    } else if (player.selectedItemIndex == 2 && player.ghostCloakCount > 0) {
                        // 🔹 유령 망토 사용 (5초간 무적)
                        player.activateGhostCloak();
                        player.ghostCloakCount--;
                        damageTexts.add(new DamageText(player.x, player.y - 20, "👻 무적 발동!", Color.CYAN));
                        soundManager.playSE(16);
                    }
                }
            }
            
            if (code == KeyEvent.VK_Q) {
                changeWeapon(true);
            }
            
            if (code == KeyEvent.VK_P) gameState = gameOverState;
        }
        else if (gameState == gameOverState) {
            if (code == KeyEvent.VK_R) {
                resetGameOnDeath();
                gameState = playState;
            }
        }
        else if (gameState == gameClearState) {
            if (code == KeyEvent.VK_R) {
                // 타이틀 화면으로 이동 및 사운드 초기화
                soundManager.stop(); // 모든 사운드 정지
                gameState = titleState; // 타이틀 화면으로
                soundManager.playMusic(29); // 타이틀 BGM 재생
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
        if (code == KeyEvent.VK_G) keyG = false;
        if (code == KeyEvent.VK_F) keyF = false;
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
        if (SwingUtilities.isLeftMouseButton(e)) {
            shoot();
            if (currentWeapon != null) {
                currentWeapon.playCursorAnimation();
                isWeaponAnimating = true; // 🔹 애니메이션 시작
            }
        }
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

        // 🔹 같은 음악이 이미 재생 중이면 재생하지 않음 (중복 방지)
        if (currentMusicIndex == musicIndex) {
            return;
        }

        // 🔹 이전 배경음악 강제 정지
        soundManager.stop();
        currentMusicIndex = -1; // 재생 중인 음악 초기화
        
        // 새 음악 재생
        soundManager.playMusic(musicIndex);
        currentMusicIndex = musicIndex; // 현재 재생 중인 음악 인덱스 저장
    }
}
