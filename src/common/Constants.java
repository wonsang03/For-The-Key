package common;

public class Constants {

    public static final String GAME_TITLE = "For the Key";

    public static final int TILE_SIZE = 64;

    public static final int MAX_SCREEN_COL = 20;
    public static final int MAX_SCREEN_ROW = 12;

    public static final int WINDOW_WIDTH = TILE_SIZE * MAX_SCREEN_COL;
    public static final int WINDOW_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW;

    public static final int MAX_WORLD_COL = 50; 
    public static final int MAX_WORLD_ROW = 50; 
    
    public static final int WORLD_WIDTH = TILE_SIZE * MAX_WORLD_COL; 
    public static final int WORLD_HEIGHT = TILE_SIZE * MAX_WORLD_ROW;

    public static final int FPS = 60;
    
    // 게임 상태
    public static final int TITLE_STATE = 0;
    public static final int PLAY_STATE = 1;
    public static final int GAME_OVER_STATE = 2;
    public static final int LOADING_STATE = 3;
    public static final int GAME_CLEAR_STATE = 4;
    
    // 로딩 화면 타이밍
    public static final long STAGE_NAME_DURATION = 1500;
    public static final long FADE_IN_DURATION = 1000;
    public static final long TOTAL_LOADING_DURATION = STAGE_NAME_DURATION + FADE_IN_DURATION;
    
    // 무기 교체 쿨다운
    public static final long WEAPON_SWAP_COOLDOWN = 4500; // 4.5초
    
    // 카메라
    public static final double CAMERA_LERP = 0.05;
    
    // 아이템 획득 범위
    public static final double ITEM_PICKUP_RANGE_RATIO = 0.7; // TILE_SIZE의 70%
    
    // 적 스폰 타일
    public static final char ENEMY_SPAWN_TILE = 'E';
    public static final char ELITE_SPAWN_TILE = 'L';
    public static final char BOSS_SPAWN_TILE = 'B';
    
    // 아이템 드롭 확률
    public static final double COMMON_ENEMY_DROP_RATE = 0.8; // 80%
    public static final double BOX_WEAPON_DROP_RATE = 0.5; // 50%
    
    // 플레이어 초기 위치
    public static final int PLAYER_START_X = 10;
    public static final int PLAYER_START_Y = 6;
    
    // 유령 망토 지속 시간 (프레임)
    public static final int GHOST_CLOAK_DURATION = 300; // 5초 (60 FPS)
    
    // 무적 시간 (프레임)
    public static final int INVINCIBLE_DURATION = 20; // 약 0.33초
    
    // 발걸음 소리 간격 (프레임)
    public static final int FOOTSTEP_INTERVAL = 20;
    
    // 아이템 획득 애니메이션 시간 (프레임)
    public static final int ITEM_OBTAIN_ANIMATION_DURATION = 180;
}
