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
    
    BufferedImage[][] animations;
    int totalFrames = 4;
    
    // [김선욱님 코드] 스탯 시스템: 아이템 효과 적용을 위한 필드 추가
    private int maxHP = 100;
    private int hp = 100;
    private double attackMultiplier = 1.0;
    private double attackSpeedBonus = 0.0;
    private double baseSpeed = 4.0;

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
            if (moveY < 0) {
                direction = "up";
            } else if (moveY > 0) {
                direction = "down";
            } else if (moveX < 0) {
                direction = "left";
            } else if (moveX > 0) {
                direction = "right";
            }
        }

        if (isMoving) {
            spriteCounter++;
            if (spriteCounter > 8) {
                spriteNum++;
                if (spriteNum >= totalFrames) {
                    spriteNum = 0;
                }
                spriteCounter = 0;
            }
        } else {
            spriteNum = 0;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage image = null;

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

        if (animations != null && colDir < animations.length && rowFrame < animations[colDir].length) {
            image = animations[colDir][rowFrame];
        }

        if (image == null) {
            g2.setColor(java.awt.Color.CYAN);
            g2.fillRect(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
            g2.setColor(java.awt.Color.BLACK);
            g2.drawRect(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
            return;
        }

        if (flipHorizontal && image != null) {
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
    
    public void receiveDamage(int damage) {
        this.hp -= damage;
        System.out.println("플레이어 피격! 데미지: " + damage + " / 남은 체력: " + this.hp);
        if (this.hp <= 0) {
            this.hp = 0;
            System.out.println("플레이어 사망!");
        }
    }
}
