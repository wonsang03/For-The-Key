package item;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Item {
    private double x, y;
    private ItemType type;
    private boolean picked = false;

    private static BufferedImage spriteSheet;
    private int spriteX, spriteY, spriteW, spriteH;
    private static final int DEFAULT_SIZE = 32;

    // [김선욱님 코드] 🔹 무기 드롭용 필드 추가
    private boolean weaponPickup = false;
    private WeaponType weaponType = null;
    private BufferedImage weaponImage = null;

    // [김선욱님 코드] 🔹 현재 장착 무기 교체 여부
    private boolean replaceCurrentWeapon = false;

    // [김선욱님 코드] 아이템 생성자
    public Item(double x, double y, ItemType type) {
        this.x = x;
        this.y = y;
        this.type = type;

        loadSpriteSheet();
        
        setupSpriteRegion(type);
    }

    // [김선욱님 코드] 🔹 무기 전용 생성자 (상자 드롭용)
    public Item(double x, double y, WeaponType weaponType) {
        this.x = x;
        this.y = y;
        this.weaponPickup = true;
        this.weaponType = weaponType;

        // Weapon 이미지 로드
        if (weaponType != null) {
            this.weaponImage = weaponType.getWeaponImage();
        }
    }

    private static void loadSpriteSheet() {
        if (spriteSheet != null) return;
        try {
            spriteSheet = ImageIO.read(new File("res/item/items.png"));
        } catch (IOException e) {
            // 이미지 로드 실패 시 조용히 처리
        }
    }

    // [김선욱님 코드] 타입별 이미지 좌표 및 크기 지정
    private void setupSpriteRegion(ItemType type) {
        switch (type) {
            case POWER_FRUIT -> {
                spriteX = 34;
                spriteY = 1278;
                spriteW = 32;
                spriteH = 32;
            }
            case LIFE_SEED -> {
                spriteX = 614;
                spriteY = 1285;
                spriteW = 25;
                spriteH = 25;
            }
            case WIND_CANDY -> {
                spriteX = 743;
                spriteY = 1286;
                spriteW = 32;
                spriteH = 32;
            }
            case DEMON_HORN -> {
                spriteX = 515;
                spriteY = 1185;
                spriteW = 32;
                spriteH = 32;
            }
            case HERMES_BOOTS -> {
                spriteX = 832;
                spriteY = 1156;
                spriteW = 32;
                spriteH = 32;
            }
            case RAPID_GLOVES -> {
                spriteX = 1025;
                spriteY = 1153;
                spriteW = 32;
                spriteH = 32;
            }
            case DRAGON_SCALE -> {
                spriteX = 866;
                spriteY = 1313;
                spriteW = 32;
                spriteH = 32;
            }
            case RED_POTION -> {
                spriteX = 836;
                spriteY = 1347;
                spriteW = 32;
                spriteH = 32;
            }
            case ELIXIR -> {
                spriteX = 387;
                spriteY = 1315;
                spriteW = 32;
                spriteH = 32;
            }
            case VAMPIRE_TOOTH -> {
                spriteX = 834;
                spriteY = 1604;
                spriteW = 32;
                spriteH = 25;
            }
            case GHOST_CLOAK -> {
                spriteX = 321;
                spriteY = 1156;
                spriteW = 32;
                spriteH = 32;
            }
            default -> {
                spriteX = 0;
                spriteY = 0;
                spriteW = DEFAULT_SIZE;
                spriteH = DEFAULT_SIZE;
            }
        }
    }


    // [김선욱님 코드] 아이템 그리기
    public void draw(Graphics2D g2) {
        if (picked) return;

        // [김선욱님 코드] 🔹 무기 픽업이면 무기 이미지로 표시
        if (weaponPickup && weaponImage != null && weaponType != null) {
            int size = 42;
            int drawX = (int)(x - size / 2);
            int drawY = (int)(y - size / 2);

            // [김선욱님 코드] 🔹 등급별 Glow 색상 설정
            Color glowColor = switch (weaponType.getRarity()) {
                case RUSTY -> new Color(128, 128, 128, 120);
                case NORMAL -> new Color(255, 255, 255, 100);
                case IRON -> new Color(100, 200, 255, 150);
                case SHARP -> new Color(255, 128, 0, 160);
                case MASTER -> new Color(255, 215, 0, 180);
                default -> new Color(255, 255, 255, 80);
            };

            // [김선욱님 코드] 🔹 Glow(빛) 효과 추가
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(glowColor);
            g2.fillOval(drawX - 6, drawY - 6, size + 12, size + 12);

            // [김선욱님 코드] 🔹 무기 이미지 그리기
            g2.drawImage(weaponImage, drawX, drawY, size, size, null);

            // [김선욱님 코드] 🔹 등급 이름 표시 (색상 구분)
            String name = (weaponType.getDisplayName() != null) ? weaponType.getDisplayName() : weaponType.getName();

            g2.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
            if (weaponType.getRarity() != null) {
                switch (weaponType.getRarity()) {
                    case RUSTY -> g2.setColor(Color.GRAY);
                    case NORMAL -> g2.setColor(Color.WHITE);
                    case IRON -> g2.setColor(new Color(150, 200, 255));
                    case SHARP -> g2.setColor(Color.ORANGE);
                    case MASTER -> g2.setColor(Color.YELLOW);
                    default -> g2.setColor(Color.CYAN);
                }
            } else {
                g2.setColor(Color.CYAN);
            }

            g2.drawString(name, (int)x - 20, (int)y - 10);
            return;
        }

        // [원본 유지] 일반 아이템 렌더링
        if (spriteSheet == null) return;
        BufferedImage img = spriteSheet.getSubimage(spriteX, spriteY, spriteW, spriteH);
        g2.drawImage(img, (int)x, (int)y, spriteW, spriteH, null);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        g2.drawString(type.getName(), (int)x - 10, (int)y - 5);
    }
    public Rectangle getBounds() {
        if (weaponPickup) {
            return new Rectangle((int)x - 21, (int)y - 21, 42, 42);
        }
        return new Rectangle((int)x, (int)y, spriteW, spriteH);
    }

    public ItemType getType() { return type; }
    public boolean isPicked() { return picked; }
    public void pickUp() { picked = true; }

    // [김선욱님 코드] 무기 드롭용 오버로드 추가
    public void pickUp(boolean value) { picked = value; }

    // [김선욱님 코드] 🔹 무기 전용 getter
    public boolean isWeaponPickup() { return weaponPickup; }
    public WeaponType getWeaponType() { return weaponType; }

    // [김선욱님 코드] 🔹 무기 교체 여부 관리 (GamePanel에서 사용)
    public boolean isReplaceCurrentWeapon() { return replaceCurrentWeapon; }
    public void setReplaceCurrentWeapon(boolean value) { this.replaceCurrentWeapon = value; }

        // -------------------------------------------------------------
    // [민정 추가] UI에서 아이템 이미지를 가져가기 위한 정적 메서드
    // -------------------------------------------------------------
    public static BufferedImage getIconImage(ItemType type) {
        loadSpriteSheet(); // 이미지가 로드 안 됐으면 로드

        int sx = 0, sy = 0;
        int sw = 32, sh = 32;

        // 필요한 아이템(물약)의 좌표만 입력
        switch (type) {
            case RED_POTION:
                sx = 836; sy = 1347; break;
            case ELIXIR:
                sx = 387; sy = 1315; break;
            case GHOST_CLOAK:
                sx = 321; sy = 1156; break;
            // 필요하다면 다른 아이템 좌표도 여기에 추가 가능
            default:
                return null;
        }

        // 이미지 잘라서 반환
        if (spriteSheet != null) {
            return spriteSheet.getSubimage(sx, sy, sw, sh);
        }
        return null;
    }
    
    // [민정 추가] 현재 아이템 객체의 이미지를 잘라서 반환하는 함수
    public BufferedImage getItemImage() {
        if (spriteSheet == null) {
            loadSpriteSheet();
        }
        return spriteSheet.getSubimage(spriteX, spriteY, spriteW, spriteH);
    }

    // [추가] 획득 애니메이션에 사용할 이미지 (무기면 무기 이미지, 아니면 아이템 시트)
    public BufferedImage getPickupImage() {
        if (weaponPickup && weaponImage != null) {
            return weaponImage;
        }
        return getItemImage();
    }
    
}
