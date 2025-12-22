package item;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public enum WeaponType {
    PISTOL("Pistol", 10, 0.4, 600, true,"res/item/pistol1.png"),
    SHOTGUN("Shotgun", 5, 1.2, 350,true, "res/item/shotgun1.png"),
    SNIPER("Sniper", 60, 2.0, 1200,true, "res/item/sniper1.png"),
    DAGGER("Dagger", 5, 0.15, 80,false, "res/item/dagger1.png"),
    LONG_SWORD("Long Sword", 15, 0.5, 150, false, null),
    KNIGHT_SWORD("Knight Sword", 45, 1.5, 210, false, null);

    private final String name;
    private final double damage, attackSpeed, range;
    private final boolean isRanged;
    private final String imagePath;
    private BufferedImage image;
    private static BufferedImage itemSheet;
    

    // 🎞️ 애니메이션 관련 필드
    private List<BufferedImage> cursorFrames = new ArrayList<>();
    private int currentFrame = 0;
    private boolean playing = false;
    private long lastFrameTime = 0;
    private int frameDelay = 60;

    // 🏅 [김선욱님 코드] 무기 등급 시스템
    private WeaponRarity rarity = WeaponRarity.NORMAL; // 기본 등급 (일반)

    WeaponType(String name, double damage, double attackSpeed, double range, boolean isRanged, String imagePath) {
        this.name = name;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.range = range;
        this.isRanged = isRanged;
        this.imagePath = imagePath;
    }

    // ✅ Enum 초기화 시 커서 프레임 및 무기 이미지 로드
    static {
        // 먼저 공용 itemSheet를 미리 로드
        try {
            java.io.InputStream is = WeaponType.class.getResourceAsStream("/res/item/items.png");
            if (is != null) itemSheet = ImageIO.read(is);
        } catch (Exception e) {
            itemSheet = null;
        }

        // 그 다음 각 무기별 커서 프레임과 이미지 로드
        for (WeaponType wt : values()) {
            wt.loadCursorFrames();
            wt.loadWeaponImageInternal(); // 모든 무기 이미지를 미리 로드하여 런타임 프리징 방지
        }
    }

    public boolean isRanged() { return isRanged; }

    // ======================================================================
    // 기존 기능 그대로
    // ======================================================================
    public String getName() { return name; }
    public double getDamage() { return damage; }
    public double getAttackSpeed() { return attackSpeed; }
    public double getRange() { return range; }

    // ======================================================================
    // 🏅 [김선욱님 코드] 등급 관련 기능
    // ======================================================================
    public WeaponRarity getRarity() { return rarity; }

    public void setRarity(WeaponRarity rarity) {
        this.rarity = rarity;
    }

    /** 🔹 등급 적용된 실제 공격력 */
    public double getEffectiveDamage() {
        return damage * rarity.getAttackMultiplier();
    }

    /** 🔹 등급 적용된 공격속도 */
    public double getEffectiveAttackSpeed() {
        return attackSpeed * rarity.getSpeedMultiplier();
    }

    /** 🔹 등급 접두사 포함 이름 (예: "명장의 롱소드") */
    public String getDisplayName() {
        return rarity.getPrefixKo() + " " + name;
    }

    // ======================================================================
    // 이미지 및 커서 로드
    // ======================================================================

    // Static 초기화 전용 - 실제 이미지 로드 수행
    private void loadWeaponImageInternal() {
        if (image != null) return;

        try {
            // 개별 파일이 있는 무기들 (PISTOL, SHOTGUN, SNIPER, DAGGER)
            if (imagePath != null) {
                // imagePath is stored as a project-relative resource path like "res/item/pistol1.png".
                // Resources are packaged inside the JAR under "/res/...", so do NOT strip "res/" here.
                java.io.InputStream is = getClass().getResourceAsStream("/" + imagePath);
                if (is != null) {
                    image = ImageIO.read(is);
                }
                return;
            }

            // items.png 스프라이트 시트를 사용하는 무기들 (LONG_SWORD, KNIGHT_SWORD)
            if (itemSheet == null) {
                return;
            }

            int spriteX = 0, spriteY = 0, spriteW = 29, spriteH = 29;
            switch (this) {
                case LONG_SWORD -> { spriteX = 702; spriteY = 1507; }
                case KNIGHT_SWORD -> { spriteX = 1025; spriteY = 1474; }
                default -> { return; }
            }
            image = itemSheet.getSubimage(spriteX, spriteY, spriteW, spriteH);
        } catch (Exception e) {
            image = null;
        }
    }

    // Public getter - 단순히 캐시된 이미지만 반환 (런타임 I/O 없음)
    public BufferedImage getWeaponImage() {
        return image;
    }

    public boolean isMelee() {
        return this == DAGGER || this == LONG_SWORD || this == KNIGHT_SWORD;
    }

    public static WeaponType next(WeaponType current) {
        int idx = (current.ordinal() + 1) % values().length;
        return values()[idx];
    }

    public static WeaponType previous(WeaponType current) {
        int idx = (current.ordinal() - 1 + values().length) % values().length;
        return values()[idx];
    }

    // ======================================================================
    // 🎞️ 커서 애니메이션
    // ======================================================================
    private void loadCursorFrames() {
        String baseName = name.toLowerCase().replace(" ", "");
        int maxFrames = switch (this) {
            case PISTOL -> 6;
            case SHOTGUN -> 3;
            case SNIPER -> 4;
            case DAGGER -> 2;
            default -> 0;
        };
        if (maxFrames == 0) return;

        for (int i = 1; i <= maxFrames; i++) {
            String path = String.format("/res/item/%s%d.png", baseName, i);
            try {
                java.io.InputStream is = getClass().getResourceAsStream(path);
                if (is != null) cursorFrames.add(ImageIO.read(is));
            } catch (IOException e) {
                // 커서 프레임 로드 실패 시 조용히 처리
            }
        }
    }

    public void playCursorAnimation() {
        if (cursorFrames.isEmpty()) return;
        playing = true;
        currentFrame = 0;
        lastFrameTime = System.currentTimeMillis();
    }

    public void drawCursor(Graphics2D g2, int mouseX, int mouseY, Object mousePressed) {
        // ⚔️ 검은 애니메이션 프레임이 없으므로 단일 이미지로 표시
        if (this == LONG_SWORD || this == KNIGHT_SWORD) {
            BufferedImage swordImg = getWeaponImage();
            if (swordImg != null) {
                int size = 90;
                g2.drawImage(swordImg, mouseX - size / 2, mouseY - size / 2, size, size, null);
            }
            return;
        }

        // 🔫 그 외 무기들은 애니메이션 표시
        if (cursorFrames.isEmpty()) return;

        if (playing) {
            long now = System.currentTimeMillis();
            if (now - lastFrameTime >= frameDelay) {
                currentFrame++;
                lastFrameTime = now;
                if (currentFrame >= cursorFrames.size()) {
                    currentFrame = 0;
                    playing = false;
                }
            }
        }

        BufferedImage frame = cursorFrames.get(currentFrame);
        int size = 90;
        g2.drawImage(frame, mouseX - size / 2, mouseY - size / 2, size, size, null);
    }

    public boolean isAnimating() {
        return playing;
    }
}
