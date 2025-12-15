package item;

import java.awt.image.BufferedImage;

public enum WeaponType {
	// [민정 추가] 맨 뒤에 true(원거리) 또는 false(근거리) 추가
    PISTOL("Pistol", 10, 0.4, 600, true),       // 원거리
    SHOTGUN("Shotgun", 5, 1.2, 350, true),      // 원거리
    SNIPER("Sniper", 60, 2.0, 1200, true),      // 원거리  
    DAGGER("Dagger", 5, 0.15, 80, false),       // 근거리
    LONG_SWORD("Long Sword", 15, 0.5, 150, false), // 근거리
    KNIGHT_SWORD("Knight Sword", 45, 1.5, 210, false); // 근거리

    private final String name;
    private final double damage, attackSpeed, range;
    
     // [민정 추가] 원거리 여부 저장 변수
    private final boolean isRanged;

    // [민정 수정] 매개 변수 boolean isRanged 추가
    WeaponType(String name, double damage, double attackSpeed, double range, boolean isRanged) {
        this.name = name;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.range = range;
        this.isRanged = isRanged; // [민정 추가] 값 저장
    }

    public String getName() { return name; }
    public double getDamage() { return damage; }
    public double getAttackSpeed() { return attackSpeed; }
    public double getRange() { return range; }
    
    
    
    // [민정 추가] 밖에서 원거리인지 물어보는 메서드
    public boolean isRanged() { return isRanged; }

    public static WeaponType next(WeaponType current) {
        int idx = (current.ordinal() + 1) % values().length;
        return values()[idx];
    }

    public static WeaponType previous(WeaponType current) {
        int idx = (current.ordinal() - 1 + values().length) % values().length;
        return values()[idx];
    }

	public BufferedImage getWeaponImage() {
		// TODO Auto-generated method stub
		return null;
	}
}