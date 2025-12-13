package map;

// [서충만님 코드] 스테이지 정보를 관리하는 클래스
public class StageInfo {
    
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
