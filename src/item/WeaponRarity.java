package item;

// [김선욱님 코드] 무기 등급 시스템
public enum WeaponRarity {
    RUSTY("Rusty", "녹슨", 0.8, 0.9),
    NORMAL("Normal", "일반", 1.0, 1.0),
    IRON("Iron", "강철의", 1.2, 1.0),
    SHARP("Sharp", "예리한", 1.6, 1.1),
    MASTER("Master", "명장의", 2.0, 1.2);

    private final String prefixEn;  // 영어 접두사
    private final String prefixKo;  // 한국어 접두사
    private final double attackMultiplier;
    private final double speedMultiplier;

    WeaponRarity(String prefixEn, String prefixKo, double attackMultiplier, double speedMultiplier) {
        this.prefixEn = prefixEn;
        this.prefixKo = prefixKo;
        this.attackMultiplier = attackMultiplier;
        this.speedMultiplier = speedMultiplier;
    }

    public String getPrefixEn() { return prefixEn; }
    public String getPrefixKo() { return prefixKo; }
    public double getAttackMultiplier() { return attackMultiplier; }
    public double getSpeedMultiplier() { return speedMultiplier; }

    /** 🔹 무작위 등급 선택 (드롭 시 랜덤) */
    public static WeaponRarity getRandomRarity() {
        double r = Math.random();
        if (r < 0.35) return RUSTY;
        else if (r < 0.65) return NORMAL;
        else if (r < 0.85) return IRON;
        else if (r < 0.97) return SHARP;
        else return MASTER;
    }
}
