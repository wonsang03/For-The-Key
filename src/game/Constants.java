// game/Constants.java (수정)

package game;

public class Constants {
    // === 윈도우 설정 ===
    public static final String GAME_TITLE    = "For the Key";
    
    // 타일 크기 (64px)
    public static final int TILE_SIZE        = 64;
    
    // 화면 크기 (가로 20칸 x 세로 12칸)
    public static final int MAX_SCREEN_COL   = 20;
    public static final int MAX_SCREEN_ROW   = 12;
    
    // 실제 픽셀 크기 (1280 x 768)
    public static final int WINDOW_WIDTH     = TILE_SIZE * MAX_SCREEN_COL;
    public static final int WINDOW_HEIGHT    = TILE_SIZE * MAX_SCREEN_ROW;
    
    // === 세계 지도 설정 (새로 추가) ===
    public static final int MAX_WORLD_COL    = 50; 
    public static final int MAX_WORLD_ROW    = 50; 
    
    public static final int WORLD_WIDTH      = TILE_SIZE * MAX_WORLD_COL; 
    public static final int WORLD_HEIGHT     = TILE_SIZE * MAX_WORLD_ROW;
}