package enemy;

public enum EnemyType {
    
    // 형식: 이름, 최대HP, 공격력, 속도, 사거리(range)
    SLIME("Slime", 60, 5, 3.0, 100),
    WOLF("Wolf", 80, 8, 5.0, 80), 
    GOBLIN("Goblin", 70, 7, 3.5, 100),
    SNAKE("Snake", 250, 12, 3.0, 90),
    MUDGOLEM("Mud Golem", 450, 15, 2.5, 100),
    SPORE_FLOWER("Spore Flower", 200, 15, 0.0, 350), // 원거리
    BOMB_SKULL("Bomb Skull", 500, 100, 7.0, 100), // 공격 사거리 낮춤 (자폭 범위는 별도)
    HELL_HOUND("Hell Hound", 800, 25, 6.5, 90),
    FIRE_IMP("Fire Imp", 1000, 50, 5.5, 300), // 원거리
    
    MAGMA_SLIME_BIG("Magma Slime", 1200, 50, 3.0, 80),   
    MAGMA_SLIME_SMALL("Magma Slime", 750, 30, 4.5, 64), 
    
    MINOTAUR("Minotaur", 700, 40, 2.5, 100),
    GOLEM("Golem", 1500, 50, 3.5, 100),
    FROZEN_KNIGHT("Frozen Knight", 600, 40, 4.0, 90),
    YETI("Yeti", 800, 45, 3.0, 120),
    SNOW_MAGE("Snow Mage", 500, 40, 3.5, 400), // 원거리
    ICE_GOLEM("Ice Golem", 2000, 60, 4.5, 100),
    HELL_KNIGHT("Hell Knight", 2500, 100, 4.0, 90);

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