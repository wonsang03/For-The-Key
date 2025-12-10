package common; // 모든 파일이 이 패키지 이름을 씁니다.

import java.io.File;

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
    
    // === 세계 지도 설정 ===
    public static final int MAX_WORLD_COL    = 50; 
    public static final int MAX_WORLD_ROW    = 50; 
    
    public static final int WORLD_WIDTH      = TILE_SIZE * MAX_WORLD_COL; 
    public static final int WORLD_HEIGHT     = TILE_SIZE * MAX_WORLD_ROW;
    
    // 게임 속도
    public static final int FPS              = 60;
    
    // === 리소스 경로 유틸리티 ===
    private static String projectRoot = null;
    
    /**
     * 프로젝트 루트 디렉토리를 찾아서 반환합니다.
     * For-The-Key 폴더를 찾아서 그 경로를 반환합니다.
     */
    public static String getProjectRoot() {
        if (projectRoot != null) {
            return projectRoot;
        }
        
        // 방법 1: 현재 작업 디렉토리에서 For-The-Key 찾기
        File currentWorkingDir = new File(System.getProperty("user.dir"));
        File forTheKeyDir = new File(currentWorkingDir, "For-The-Key");
        if (forTheKeyDir.exists() && forTheKeyDir.isDirectory()) {
            // res 폴더가 있는지 확인
            File resDir = new File(forTheKeyDir, "res");
            if (resDir.exists() && resDir.isDirectory()) {
                projectRoot = forTheKeyDir.getAbsolutePath();
                return projectRoot;
            }
        }
        
        // 방법 2: 현재 작업 디렉토리가 이미 For-The-Key인 경우
        File resDir = new File(currentWorkingDir, "res");
        if (resDir.exists() && resDir.isDirectory()) {
            projectRoot = currentWorkingDir.getAbsolutePath();
            return projectRoot;
        }
        
        // 방법 3: 클래스 파일 위치에서 찾기
        try {
            String classPath = Constants.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            classPath = java.net.URLDecoder.decode(classPath, "UTF-8");
            File classDir = new File(classPath);
            
            // bin 또는 out 폴더에서 시작하는 경우
            if (classDir.getName().equals("bin") || classDir.getName().equals("out") || 
                classDir.getName().equals("classes")) {
                classDir = classDir.getParentFile();
            }
            
            // For-The-Key 폴더를 찾을 때까지 상위 디렉토리로 이동
            File searchDir = classDir;
            for (int i = 0; i < 5 && searchDir != null; i++) {
                File testDir = new File(searchDir, "For-The-Key");
                if (testDir.exists() && testDir.isDirectory()) {
                    File testRes = new File(testDir, "res");
                    if (testRes.exists() && testRes.isDirectory()) {
                        projectRoot = testDir.getAbsolutePath();
                        return projectRoot;
                    }
                }
                searchDir = searchDir.getParentFile();
            }
        } catch (Exception e) {
            // 예외 발생 시 무시하고 다음 방법 시도
        }
        
        // 방법 4: 상위 디렉토리에서 찾기
        File parentDir = currentWorkingDir.getParentFile();
        if (parentDir != null) {
            File testDir = new File(parentDir, "For-The-Key");
            if (testDir.exists() && testDir.isDirectory()) {
                File testRes = new File(testDir, "res");
                if (testRes.exists() && testRes.isDirectory()) {
                    projectRoot = testDir.getAbsolutePath();
                    return projectRoot;
                }
            }
        }
        
        // 모든 방법 실패 시 현재 작업 디렉토리 사용
        projectRoot = currentWorkingDir.getAbsolutePath();
        System.err.println("경고: For-The-Key 폴더를 찾지 못했습니다. 현재 디렉토리 사용: " + projectRoot);
        return projectRoot;
    }
    
    /**
     * 리소스 파일의 전체 경로를 반환합니다.
     * @param relativePath res/ 또는 src/ 로 시작하는 상대 경로
     * @return 전체 파일 경로
     */
    public static File getResourceFile(String relativePath) {
        String root = getProjectRoot();
        return new File(root, relativePath);
    }
}
