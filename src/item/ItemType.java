package item;

import java.awt.Color;
import java.util.Random; // [민정 추가] 랜덤 사용을 위해 추가

public enum ItemType {
    POWER_FRUIT("Power Fruit", "공격력 +10%", 0.1, 0, 0, 0, Rarity.NORMAL),
    LIFE_SEED("Life Seed", "최대 체력 +5", 0, 5, 0, 0, Rarity.NORMAL),
    WIND_CANDY("Wind Candy", "이동속도 +10%", 0, 0, 0.1, 0, Rarity.NORMAL),
    DEMON_HORN("Demon Horn", "공격력 +175%", 1.75, 0, 0, 0, Rarity.RARE),
    HERMES_BOOTS("Hermes Boots", "이동속도 +2.5", 0, 0, 2.5, 0, Rarity.RARE),
    RAPID_GLOVES("Rapid Gloves", "공격속도 +50%", 0, 0, 0, 0.5, Rarity.RARE),
    DRAGON_SCALE("Dragon Scale", "최대 체력 +100", 0, 100, 0, 0, Rarity.LEGENDARY),
    RED_POTION("Red Potion", "체력 +30 회복", 0, 30, 0, 0, Rarity.NORMAL),
    ELIXIR("Elixir", "체력 완전 회복", 0, 9999, 0, 0, Rarity.LEGENDARY),
	
	// [민정 추가] 유령 망토 (이속 버프 제거, 5초 무적 설명)
    // 수치는 모두 0으로 두고, 사용 시 로직(Player.java)에서 처리합니다.
    GHOST_CLOAK("Ghost Cloak", "5초 무적", 0, 0, 0, 0, Rarity.RARE);

    private final String name, description;
    private final double attackBuff, hpBuff, speedBuff, attackSpeedBuff;
    private final Rarity rarity;

    ItemType(String name, String desc, double atk, double hp, double spd, double aspd, Rarity rarity) {
        this.name = name;
        this.description = desc;
        this.attackBuff = atk;
        this.hpBuff = hp;
        this.speedBuff = spd;
        this.attackSpeedBuff = aspd;
        this.rarity = rarity;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getAttackBuff() { return attackBuff; }
    public double getHpBuff() { return hpBuff; }
    public double getSpeedBuff() { return speedBuff; }
    public double getAttackSpeedBuff() { return attackSpeedBuff; }
    public Rarity getRarity() { return rarity; }

    public enum Rarity {
        NORMAL(new Color(180, 180, 180)),
        RARE(new Color(80, 160, 255)),
        LEGENDARY(new Color(255, 200, 50));

        private final Color color;
        Rarity(Color color) { this.color = color; }
        public Color getColor() { return color; }
    }

	public static ItemType getRandom() {
//		// TODO Auto-generated method stub
//		return null;                              // [민정 수정] 기존 내용 주석 처리
		return values()[new Random().nextInt(values().length)]; // [민정 추가]
	}
}
