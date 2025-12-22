package item;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import common.Constants;

public class Box {
    public int x, y;
    public boolean opened = false;
    private static BufferedImage closedImg, openedImg;

    // Static 초기화로 한 번만 로드
    static {
        try {
            java.io.InputStream is1 = Box.class.getResourceAsStream("/item/item/box_closed.png");
            java.io.InputStream is2 = Box.class.getResourceAsStream("/item/item/box_open.png");
            if (is1 != null) closedImg = ImageIO.read(is1);
            if (is2 != null) openedImg = ImageIO.read(is2);
        } catch (Exception e) {
            closedImg = null;
            openedImg = null;
        }
    }

    public Box(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D g2, double cameraX, double cameraY) {
        BufferedImage img = opened ? openedImg : closedImg;
        if (img != null) {
            g2.drawImage(img, (int)(x - cameraX), (int)(y - cameraY),
                    Constants.TILE_SIZE, Constants.TILE_SIZE, null);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
    }

    public void open() {
        if (!opened) {
            opened = true;
        }
    }
}
