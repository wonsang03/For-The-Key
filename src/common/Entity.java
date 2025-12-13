package common;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Entity {

    public int x, y;
    public int speed;
    
    // [민정님 추가] 이름을 저장할 변수
    public String name;

    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public String direction;

    public int spriteCounter = 0;
    public int spriteNum = 1;

    public Rectangle solidArea;
    public boolean collisionOn = false;
    
    public void update() {
    }

    public void draw(Graphics2D g2) {
    }
    
    // [민정님 추가] 이름을 가져오는 기능
    public String getName() {
        return name;
    }
}
