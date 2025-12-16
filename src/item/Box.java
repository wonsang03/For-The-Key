package item;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import common.Constants;

public class Box {
    public int x, y;
    public boolean opened = false;
    private BufferedImage closedImg, openedImg;

    public Box(int x, int y) {
        this.x = x;
        this.y = y;
        loadImages();
    }

    private void loadImages() {
        try {
            closedImg = ImageIO.read(new File("res/item/box_closed.png"));
            openedImg = ImageIO.read(new File("res/item/box_open.png"));
        } catch (Exception e) {
        }
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
