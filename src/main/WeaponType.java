package main;

public enum WeaponType {
    PISTOL("Pistol", 10, 0.4, 600),
    SHOTGUN("Shotgun", 5, 1.2, 350),
    SNIPER("Sniper", 60, 2.0, 1200),
    DAGGER("Dagger", 5, 0.15, 80),
    LONG_SWORD("Long Sword", 15, 0.5, 150),
    KNIGHT_SWORD("Knight Sword", 45, 1.5, 210);

    private final String name;
    private final double damage, attackSpeed, range;

    WeaponType(String name, double damage, double attackSpeed, double range) {
        this.name = name; this.damage = damage; this.attackSpeed = attackSpeed; this.range = range;
    }

    public String getName() { return name; }
    public double getDamage() { return damage; }
    public double getAttackSpeed() { return attackSpeed; }
    public double getRange() { return range; }

    public static WeaponType next(WeaponType current) {
        int idx = (current.ordinal() + 1) % values().length;
        return values()[idx];
    }
    public static WeaponType previous(WeaponType current) {
        int idx = (current.ordinal() - 1 + values().length) % values().length;
        return values()[idx];
    }
}
