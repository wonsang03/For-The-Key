package player;

import java.awt.AlphaComposite;
import java.awt.Color;
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
    
    BufferedImage[][] animations;
    int totalFrames = 4;
    
    // [김선욱님 코드] 스탯 시스템: 아이템 효과 적용을 위한 필드 추가
    private int maxHP = 100;
    private int hp = 100;
    private double attackMultiplier = 1.0;
    private double attackSpeedBonus = 0.0;
    private double baseSpeed = 4.0;
    
    // [김민정님 코드] 발걸음 소리 타이머 변수
    int footstepCounter = 0; 
    
    // [김민정님 코드] 무적 시간(피격 효과) 관련 변수
    public boolean invincible = false; // true면 무적 상태 (데미지 안 입음)
    public int invincibleCounter = 0;  // 무적 시간 타이머

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        
        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        x = Constants.TILE_SIZE * 5;
        y = Constants.TILE_SIZE * 5;
        speed = (int)baseSpeed;
        direction = "down";
        spriteNum = 0;
    }
    
    public void getPlayerImage() {
        try {
            java.io.File file = new java.io.File("res/player.png");
            
            if (!file.exists()) {
                System.err.println("경고: 플레이어 이미지 파일을 찾을 수 없습니다: " + file.getAbsolutePath());
                animations = new BufferedImage[3][totalFrames];
                return;
            }
            
            BufferedImage spriteSheet = ImageIO.read(file);
            
            if (spriteSheet == null) {
                System.err.println("경고: 플레이어 이미지를 읽을 수 없습니다.");
                animations = new BufferedImage[3][totalFrames];
                return;
            }
            
            animations = new BufferedImage[3][totalFrames];

            int width = spriteSheet.getWidth() / 3;
            int height = spriteSheet.getHeight() / totalFrames;

            for (int col = 0; col < 3; col++) {
                for (int row = 0; row < totalFrames; row++) {
                    animations[col][row] = spriteSheet.getSubimage(col * width, row * height, width, height);
                }
            }
            
            System.out.println("플레이어 이미지 로드 성공: " + file.getAbsolutePath());
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("이미지 로드 실패! res/player.png 파일을 확인하세요.");
            animations = new BufferedImage[3][totalFrames];
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("플레이어 이미지 로드 중 오류 발생: " + e.getMessage());
            animations = new BufferedImage[3][totalFrames];
        }
    }

    @Override
    public void update() {
        boolean isMoving = false;
        int moveX = 0;
        int moveY = 0;

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

        // 대각선 이동 시 속도 정규화
        if (moveX != 0 && moveY != 0) {
            double diagonalSpeed = speed / Math.sqrt(2.0);
            moveX = (int)(moveX * (diagonalSpeed / speed));
            moveY = (int)(moveY * (diagonalSpeed / speed));
        }

        x += moveX;
        y += moveY;

        if (isMoving) {
            if (moveY < 0) direction = "up";
            else if (moveY > 0) direction = "down";
            else if (moveX < 0) direction = "left";
            else if (moveX > 0) direction = "right";
            
            // [김민정님 코드] 발걸음 소리 재생 로직
            footstepCounter++; 
            if (footstepCounter > 20) { // 약 0.3초마다 재생
                gp.soundManager.playSE(19);    // 19번: player_move.wav
                footstepCounter = 0;    
            }
        } else {
            footstepCounter = 20; 
        }

        // [김민정님 코드] 애니메이션 로직
        if (isMoving) {
            spriteCounter++;
            if (spriteCounter > 8) { 
                spriteNum++; 
                if (spriteNum >= totalFrames) spriteNum = 0;
                spriteCounter = 0;
            }
        } else {
            spriteNum = 0; 
        }

        // [김민정님 코드] 무적 시간 관리 (약 1초 동안 무적 유지 후 해제)
        if (invincible == true) {
            invincibleCounter++;
            if (invincibleCounter > 20) { // 60프레임 = 약 1초
                invincible = false;
                invincibleCounter = 0;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        // 1. 방향에 따른 이미지 선택
        int colDir = 0; 
        boolean flipHorizontal = false; 
        
        switch (direction) {
        case "down":  colDir = 0; break; 
        case "up":    colDir = 1; break; 
        case "right": colDir = 2; break; 
        case "left":  
            colDir = 2; 
            flipHorizontal = true; 
            break; 
        }

        int rowFrame = spriteNum; 

        // 2. 기본 이미지 가져오기
        if (animations != null && colDir < animations.length && rowFrame < animations[colDir].length) {
            image = animations[colDir][rowFrame];
        }

        if (image == null) return; // 이미지가 없으면 그리지 않음

        // [김민정님 코드] 무적 상태일 때 빨간색 틴트(Tint) 적용하기 (마인크래프트 효과)
        if (invincible == true) {
            // 1) 임시 이미지를 하나 만듭니다 (캐릭터 크기만큼)
            BufferedImage tintedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = tintedImage.createGraphics();

            // 2) 원본 캐릭터를 임시 이미지에 그립니다
            g2d.drawImage(image, 0, 0, null);

            // 3) 빨간색을 덮어씌웁니다 (SRC_ATOP: 이미지가 있는 부분에만 색칠)
            g2d.setComposite(AlphaComposite.SrcAtop);
            g2d.setColor(new Color(255, 30, 30, 130));
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            
            // 4) 작업 종료
            g2d.dispose();

            // 5) 이제 그릴 이미지를 '빨간색 처리된 이미지'로 교체합니다
            image = tintedImage;
        }

        // [김민정님 코드] 최종 이미지 화면에 그리기 (좌우 반전 처리 포함)
        if (flipHorizontal) {
            g2.drawImage(image, x + Constants.TILE_SIZE, y, -Constants.TILE_SIZE, Constants.TILE_SIZE, null);
        } else {
            g2.drawImage(image, x, y, Constants.TILE_SIZE, Constants.TILE_SIZE, null);
        }
    }
    
    // [김선욱님 코드] 스탯 관련 메서드: 아이템 효과 적용을 위한 메서드들
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
    
    // [김선욱님 코드] Getter 메서드: GamePanel에서 플레이어 스탯을 가져오기 위한 메서드들
    public int getHP() { return hp; }
    public int getMaxHP() { return maxHP; }
    public double getAttackMultiplier() { return attackMultiplier; }
    public double getAttackSpeedBonus() { return attackSpeedBonus; }
    public double getMoveSpeed() { return baseSpeed; }
    
    // [김민정님 코드] 피격 처리 메서드
    public void receiveDamage(int damage) {
        // [김민정님 코드] 무적 상태라면 데미지 무시
        if (invincible == true) {
            return;
        }

        // [김민정님 코드] 피격 사운드 재생 (20번: player_hit.wav)
        gp.soundManager.playSE(20);

        // [김민정님 코드] 체력 감소
        this.hp -= damage;
        
        // [김민정님 코드] 무적 상태 시작
        invincible = true;

        System.out.println("플레이어 피격! 데미지: " + damage + " / 남은 체력: " + this.hp);
        
        // [김민정님 코드] 사망 처리
        if (this.hp <= 0) {
            this.hp = 0;
            // gp.soundManager.playSE(21); // (필요 시 주석 해제) 플레이어 사망음
            System.out.println("플레이어 사망!");
        }
    }
}
