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
    
    // 이미지를 담을 2차원 배열 [열(방향)][행(동작)]
    BufferedImage[][] animations;
    int totalFrames = 4;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        
        setDefaultValues();
        getPlayerImage();   // 이미지 로딩 및 자르기
    }

    public void setDefaultValues() {
    	// 시작 위치 및 속도 설정
    	x = Constants.TILE_SIZE * 5; // 임시 시작 위치
        y = Constants.TILE_SIZE * 5;
        
        speed = 4; // 이동 속도
        direction = "down"; // 처음엔 아래를 봄
        spriteNum = 0; // 0번 프레임부터 시작
    }
    
    public void getPlayerImage() {
        try {
        	// 스프라이트 시트 불러오기 (파일명: player.png)
            BufferedImage spriteSheet = ImageIO.read(getClass().getResourceAsStream("/res/player/player.png"));
        	
            // 배열 크기: [3열(방향)][4행(동작)]
            animations = new BufferedImage[3][totalFrames];

            int width = spriteSheet.getWidth() / 3;             // 전체 너비 / 3칸
            int height = spriteSheet.getHeight() / totalFrames; // 전체 높이 / 4칸

            // 열(Col)을 기준으로 먼저 돕니다.
            for (int col = 0; col < 3; col++) {
                for (int row = 0; row < totalFrames; row++) {
                    // animations[방향칸][동작줄] 에 저장
                    animations[col][row] = spriteSheet.getSubimage(col * width, row * height, width, height);
                }
            }
            
        } catch (IOException e) {
        	e.printStackTrace();
            System.out.println("이미지 로드 실패! /res/player/player.png 파일을 확인하세요.");
        }
    }

    @Override
    public void update() {
        // 움직임 상태 확인용 변수
        boolean isMoving = false;

        // 키 입력에 따른 이동 및 방향 설정
        if (keyH.upPressed) {
            direction = "up";
            y -= speed;
            isMoving = true;
        } else if (keyH.downPressed) {
            direction = "down";
            y += speed;
            isMoving = true;
        } else if (keyH.leftPressed) {
            direction = "left";
            x -= speed;
            isMoving = true;
        } else if (keyH.rightPressed) {
            direction = "right";
            x += speed;
            isMoving = true;
        }

        // 애니메이션 로직 (세로 4프레임을 순환)
        if (isMoving) {
            spriteCounter++;
            if (spriteCounter > 8) { // 속도 조절 (숫자가 크면 느려짐)
                spriteNum++; // 다음 동작 프레임으로
                if (spriteNum >= totalFrames) { // 끝까지 가면 다시 0번으로
                    spriteNum = 0;
                }
                spriteCounter = 0;
            }
        } else {
            spriteNum = 0; // 멈추면 가장 첫 번째 동작(보통 서 있는 자세) 보여줌
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        // 방향에 따라 몇 번째 '세로줄(열, Column)'을 쓸지 결정
        int colDir = 0; 
        
        switch (direction) {
        case "down":  colDir = 0; break; // 1번째 세로줄: 정면
        case "up":    colDir = 1; break; // 2번째 세로줄: 뒷면
        case "right": colDir = 2; break; // 3번째 세로줄: 옆면(오른쪽)
        // *중요* '왼쪽' 이미지가 없으므로 '오른쪽'을 빌려 씁니다.
        case "left":  colDir = 2; break; 
        }

        // 2. 현재 애니메이션 순서에 따라 몇 번째 '가로줄(행, Row)'을 쓸지 결정
        int rowFrame = spriteNum; // 0 ~ 3 사이의 숫자

        // 안전 장치 후 이미지 선택
        // animations[방향][동작]
        if (animations != null) {
            image = animations[colDir][rowFrame];
        }

        g2.drawImage(image, x, y, Constants.TILE_SIZE, Constants.TILE_SIZE, null);
    }
}