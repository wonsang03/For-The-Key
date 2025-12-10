package enemy;

public enum EnemyType {
    
    // 형식: 이름, 최대HP, 공격력, 속도, 사거리(range)
    SLIME("Slime", 30, 5, 2.0, 100),
    WOLF("Wolf", 40, 8, 5.0, 80), 
    GOBLIN("Goblin", 35, 7, 2.5, 100),
    SNAKE("Snake", 150, 12, 2.0, 90),
    MUDGOLEM("Mud Golem", 200, 10, 1.0, 100),
    SPORE_FLOWER("Spore Flower", 100, 15, 0.0, 350), // 원거리
    BOMB_SKULL("Bomb Skull", 100, 100, 6.0, 70),
    HELL_HOUND("Hell Hound", 400, 25, 5.5, 90),
    FIRE_IMP("Fire Imp", 500, 50, 4.0, 300), // 원거리
    
    MAGMA_SLIME_BIG("Magma Slime", 500, 30, 3.0, 80),   
    MAGMA_SLIME_SMALL("Magma Slime", 250, 20, 3.0, 64), 
    
    ORC("Orc", 500, 20, 1.5, 100),
    MINOTAUR("Minotaur", 500, 20, 1.5, 100),
    GOLEM("Golem", 1000, 45, 1.5, 100),
    FROZEN_KNIGHT("Frozen Knight", 375, 16, 3.0, 90),
    YETI("Yeti", 500, 20, 2.0, 120),
    SNOW_MAGE("Snow Mage", 300, 25, 2.5, 400), // 원거리
    ICE_GOLEM("Ice Golem", 1500, 50, 3.5, 100),
    HELL_KNIGHT("Hell Knight", 2000, 80, 3.0, 90);

    // 필드 변수
    private String name;
    private int maxHp;
    private int attack;
    private double speed;
    private int range; 

    // 생성자 (range 필드 추가)
    EnemyType(String name, int maxHp, int attack, double speed, int range) {
        this.name = name;
        this.maxHp = maxHp;
        this.attack = attack;
        this.speed = speed;
        this.range = range; 
    }

    // Getter 메서드
    public String getName() { return name; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public double getSpeed() { return speed; }
    public int getRange() { return range; } 
}