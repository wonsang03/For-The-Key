package player;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import common.Constants;
import common.Entity;
import main.GamePanel;

public class Player extends Entity {

    GamePanel gp;
    KeyHandler keyH;
    
    // 이미지 변수
    BufferedImage baseImage;
    
    // WeaponType 대신 일단 '글자(String)'로 무기를 관리합니다.
    // 나중에 담당자가 WeaponType을 완성하면 그때 바꾸면 됩니다.
    public String currentWeapon;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        
        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        // 시작 위치 (100, 100)
        x = 100;
        y = 100;
        speed = 4; // 이동 속도
        direction = "down";
        
        // 시작 무기 설정: "Pistol" 또는 "Dagger"라고 적으세요.
        currentWeapon = "Pistol";
    }
    
    public void getPlayerImage() {
        try {
            // 이미지 불러오기 (res/player/player.png)
            baseImage = ImageIO.read(getClass().getResourceAsStream("/res/player/player.png"));
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("오류: 이미지를 읽지 못했습니다. /res/player/player.png 파일이 있는지 확인하세요!");
        }
    }

    public void update() {
        // 키보드 입력에 따른 이동 (상하좌우)
        if (keyH.upPressed) {
            y -= speed;
            direction = "up";
        }
        else if (keyH.downPressed) {
            y += speed;
            direction = "down";
        }
        else if (keyH.leftPressed) {
            x -= speed;
            direction = "left";
        }
        else if (keyH.rightPressed) {
            x += speed;
            direction = "right";
        }
    }

    public void draw(Graphics2D g2) {
        // 이미지가 잘 로드되었는지 확인 후 그리기
        if (baseImage != null) {
            g2.drawImage(baseImage, x, y, Constants.TILE_SIZE, Constants.TILE_SIZE, null);
        } else {
            // 이미지가 없으면 빨간 네모라도 그려서 오류를 알림
            g2.setColor(java.awt.Color.RED);
            g2.fillRect(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
        }
    }
}