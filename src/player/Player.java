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
    
    // [추가] 스탯 시스템: 아이템 효과 적용을 위한 필드 추가
    private int maxHP = 100;
    private int hp = 100;
    private double attackMultiplier = 1.0;  // 공격력 배수
    private double attackSpeedBonus = 0.0;  // 공격속도 보너스
    private double baseSpeed = 4.0;        // 기본 이동속도 (아이템으로 변경 가능하도록)

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
        
        // [변경] speed = 4 → speed = (int)baseSpeed: 아이템으로 속도 변경 가능하도록 수정
        speed = (int)baseSpeed; // 이동 속도
        direction = "down"; // 처음엔 아래를 봄
        spriteNum = 0; // 0번 프레임부터 시작
    }
    
    public void getPlayerImage() {
        try {
        	// [변경] 이미지 로딩 방식 변경: 프로젝트 루트 기준 경로 사용
            java.io.File file = Constants.getResourceFile("res/player.png");
            BufferedImage spriteSheet = ImageIO.read(file);
        	
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
        int moveX = 0;
        int moveY = 0;

        // [변경] 대각선 이동 추가: if-else if → 독립적인 if문으로 변경하여 동시 입력 가능
        // 키 입력에 따른 이동 (대각선 이동 가능)
        if (keyH.upPressed) {
            moveY -= speed;
            isMoving = true;
        }
        if (keyH.downPressed) {
            moveY += speed;
            isMoving = true;
        }
        if (keyH.leftPressed) {
            moveX -= speed;
            isMoving = true;
        }
        if (keyH.rightPressed) {
            moveX += speed;
            isMoving = true;
        }

        // [추가] 대각선 이동 시 속도 정규화: 대각선 이동이 더 빠르지 않도록 √2로 나눔
        if (moveX != 0 && moveY != 0) {
            // 대각선 이동 시 속도를 √2로 나눔 (약 0.707)
            double diagonalSpeed = speed / Math.sqrt(2.0);
            moveX = (int)(moveX * (diagonalSpeed / speed));
            moveY = (int)(moveY * (diagonalSpeed / speed));
        }

        // 실제 이동 적용
        x += moveX;
        y += moveY;

        // [변경] 방향 설정 로직 변경: 수직 방향 우선, 수평 방향 차순으로 설정
        if (isMoving) {
            // 수직 방향 우선
            if (moveY < 0) {
                direction = "up";
            } else if (moveY > 0) {
                direction = "down";
            } 
            // 수평 방향
            else if (moveX < 0) {
                direction = "left";
            } else if (moveX > 0) {
                direction = "right";
            }
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
        // [추가] 왼쪽 방향 이미지 반전을 위한 플래그
        boolean flipHorizontal = false; // 왼쪽일 때 이미지 반전 여부
        
        switch (direction) {
        case "down":  colDir = 0; break; // 1번째 세로줄: 정면
        case "up":    colDir = 1; break; // 2번째 세로줄: 뒷면
        case "right": colDir = 2; break; // 3번째 세로줄: 옆면(오른쪽)
        case "left":  
            colDir = 2; // 오른쪽 이미지를 사용
            flipHorizontal = true; // 왼쪽이므로 반전 필요
            break; 
        }

        // 현재 애니메이션 순서에 따라 몇 번째 '가로줄(행, Row)'을 쓸지 결정
        int rowFrame = spriteNum; // 0 ~ 3 사이의 숫자

        // 안전 장치 후 이미지 선택
        // animations[방향][동작]
        if (animations != null) {
            image = animations[colDir][rowFrame];
        }

        // [추가] 왼쪽 방향일 때 이미지를 수평 반전시켜서 그리기: drawImage의 음수 width 사용
        if (flipHorizontal && image != null) {
            // 이미지를 반전시켜서 그리기: x + width, y, -width, height
            g2.drawImage(image, x + Constants.TILE_SIZE, y, -Constants.TILE_SIZE, Constants.TILE_SIZE, null);
        } else {
            g2.drawImage(image, x, y, Constants.TILE_SIZE, Constants.TILE_SIZE, null);
        }
    }
    
    // [추가] 스탯 관련 메서드: 아이템 효과 적용을 위한 메서드들
    public void heal(int value) { 
        hp = Math.min(maxHP, hp + value); 
    }
    
    public void addAttackBonus(double value) { 
        attackMultiplier += value; 
    }
    
    public void addSpeedBonus(double value) { 
        baseSpeed += value;
        speed = (int)baseSpeed;
    }
    
    public void addAttackSpeedBonus(double value) { 
        attackSpeedBonus += value; 
    }
    
    public void addMaxHP(double value) { 
        maxHP += (int)value; 
        hp += (int)value; 
    }
    
    // [추가] Getter 메서드: GamePanel에서 플레이어 스탯을 가져오기 위한 메서드들
    public int getHP() { return hp; }
    public int getMaxHP() { return maxHP; }
    public double getAttackMultiplier() { return attackMultiplier; }
    public double getAttackSpeedBonus() { return attackSpeedBonus; }
    public double getMoveSpeed() { return baseSpeed; }
}