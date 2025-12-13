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

    // [김선욱님 코드] 아이템 생성자
    public Item(double x, double y, ItemType type) {
        this.x = x;
        this.y = y;
        this.type = type;

        loadSpriteSheet();
        setupSpriteRegion(type);
    }

    private static void loadSpriteSheet() {
        if (spriteSheet != null) return;
        try {
            spriteSheet = ImageIO.read(new File("res/item/items.png"));
        } catch (IOException e) {
            System.out.println("❌ items.png 로드 실패");
        }
    }

    // [김선욱님 코드] 타입별 이미지 좌표 및 크기 지정
    private void setupSpriteRegion(ItemType type) {
        switch (type) {
            case POWER_FRUIT:
                spriteX = 34;
                spriteY = 1278;
                spriteW = 32;
                spriteH = 32;
                break;
            case LIFE_SEED:
                spriteX = 612;
                spriteY = 1285;
                spriteW = 32;
                spriteH = 32;
                break;
            case WIND_CANDY:
                spriteX = 774;
                spriteY = 1286;
                spriteW = 32;
                spriteH = 32;
                break;
            case DEMON_HORN:
                spriteX = 515;
                spriteY = 1185;
                spriteW = 32;
                spriteH = 32;
                break;
            case HERMES_BOOTS:
                spriteX = 832;
                spriteY = 1156;
                spriteW = 32;
                spriteH = 32;
                break;
            case RAPID_GLOVES:
                spriteX = 1025;
                spriteY = 1153;
                spriteW = 32;
                spriteH = 32;
                break;
            case DRAGON_SCALE:
                spriteX = 866;
                spriteY = 1313;
                spriteW = 32;
                spriteH = 32;
                break;
            case RED_POTION:
                spriteX = 836;
                spriteY = 1347;
                spriteW = 32;
                spriteH = 32;
                break;
            case ELIXIR:
                spriteX = 387;
                spriteY = 1315;
                spriteW = 32;
                spriteH = 32;
                break;
            default:
                spriteX = 0;
                spriteY = 0;
                spriteW = DEFAULT_SIZE;
                spriteH = DEFAULT_SIZE;
                break;
        }
    }

    // [김선욱님 코드] 아이템 그리기
    public void draw(Graphics2D g2) {
        if (picked || spriteSheet == null) return;

        BufferedImage img = spriteSheet.getSubimage(spriteX, spriteY, spriteW, spriteH);
        g2.drawImage(img, (int)x, (int)y, spriteW, spriteH, null);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        g2.drawString(type.getName(), (int)x - 10, (int)y - 5);
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, spriteW, spriteH);
    }

    public ItemType getType() { return type; }
    public boolean isPicked() { return picked; }
    public void pickUp() { picked = true; }
}
