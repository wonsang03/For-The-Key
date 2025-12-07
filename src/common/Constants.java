package common; // 모든 파일이 이 패키지 이름을 씁니다.

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
    
    // 게임 속도
    public static final int FPS              = 60;
}