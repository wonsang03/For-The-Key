package map;

// [서충만님 코드] 스테이지 정보를 관리하는 클래스
public class StageInfo {
	
	// -----------------------------------------------------------
    // [민정 추가] 스테이지 클리어 규칙 (열쇠 개수)
    // -----------------------------------------------------------
    
    // 최대 스테이지 개수
    public static final int MAX_STAGE = 5;

    // 스테이지별 필요 열쇠 개수 (인덱스 0은 비움, 1부터 시작)
    // 1탄=1개, 2탄=2개, 3탄=3개, 4탄=4개
    private static final int[] REQUIRED_KEYS = { 0, 1, 2, 3, 4, 0 };

    // 해당 스테이지를 깨기 위해 필요한 열쇠 개수를 알려주는 메서드
    public static int getRequiredKeyCount(int stage) {
        if (stage > 0 && stage < REQUIRED_KEYS.length) {
            return REQUIRED_KEYS[stage];
        }
        return 0; // 예외 상황 (0개)
    }
    
    public static String getCurrentStageName() {
        int stage = MapLoader.getCurrentStage();
        return getStageName(stage);
    }
    
    // [서충만님 코드] 스테이지 번호에 해당하는 이름을 반환
    public static String getStageName(int stageNum) {
        switch (stageNum) {
            case 1:
                return "미아의 숲";
            case 2:
                return "늪지대";
            case 3:
                return "얼음 동굴";
            case 4:
                return "지옥의 전당";
            case 5:
                return "알현실";
            default:
                return "알 수 없는 스테이지";
        }
    }
}
