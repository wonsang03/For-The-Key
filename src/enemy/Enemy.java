package enemy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Enemy {

    public EnemyType type; // MAGMA_SLIME_BIG 분열 처리를 위해 public으로 변경
    private String name;
    private int hp;
    private int maxHp;
    private double speed;

    private int attackRange;
    private int moveRange; 

    public double x, y; // 월드 좌표
    public int hitWidth;  // 히트박스(충돌) 너비
    public int hitHeight; // 히트박스(충돌) 높이
    
    // 드로잉 크기 필드 (수동 설정용)
    public int drawWidth;
    public int drawHeight;
    
    public boolean alive = true;
    
    public final int IDLE = 0;   
    public final int MOVE = 1;   
    public final int ATTACK = 2; 
    public int currentState = IDLE; 

    private boolean flip = false; 
    private boolean spriteDefaultFacesLeft = true; 
    
    private BufferedImage[][] sprites;   
    private boolean isAnimated = false; 

    private int spriteCounter = 0;
    private int spriteNum = 0;
    private final int ANIMATION_SPEED = 9;
    
    // 공격 모션이 진행 중인지 추적하는 플래그
    private boolean attackAnimationInProgress = false; 

    public Enemy(EnemyType type, double startX, double startY) {
        this.type = type;
        this.x = startX;
        this.y = startY;
        this.name = type.getName();
        this.maxHp = type.getMaxHp();
        this.hp = this.maxHp;
        this.speed = type.getSpeed();
        
        this.attackRange = 300; 
        this.moveRange = 600; 
        this.hitWidth = 48; 
        this.hitHeight = 48; 
        this.drawWidth = 48; 
        this.drawHeight = 48;
        
        switch (type) {
            case SLIME:
                this.attackRange = 100;
                this.moveRange = 600;
                this.hitWidth = 80; 
                this.hitHeight = 70;
                this.drawWidth = 80; 
                this.drawHeight = 70;
                break;
                
            case WOLF:
                this.attackRange = 100;
                this.moveRange = 600;
                this.hitWidth = 70; 
                this.hitHeight = 50;
                this.drawWidth = 70; 
                this.drawHeight = 50;
                break;
                
            case GOBLIN:
                this.attackRange = 400;
                this.moveRange = 800;
                this.hitWidth = 80; 
                this.hitHeight = 70;
                this.drawWidth = 80; 
                this.drawHeight = 70;
                break;
                
            case ORC:
                this.attackRange = 100;
                this.moveRange = 800;
                this.hitWidth = 200; 
                this.hitHeight = 150;
                this.drawWidth = 200; 
                this.drawHeight = 150;
                this.spriteDefaultFacesLeft = false;
                break;
                
            case SPORE_FLOWER:
                this.attackRange = 600;
                this.moveRange = 800;
                this.hitWidth = 80; 
                this.hitHeight = 70;
                this.drawWidth = 60; 
                this.drawHeight = 50;
                break;
                
            case FIRE_IMP:
                this.attackRange = 400;
                this.moveRange = 600;
                this.hitWidth = 105; 
                this.hitHeight = 70;
                this.drawWidth = 90; 
                this.drawHeight = 70;
                break;
                
            case SNOW_MAGE:
                this.attackRange = 100;
                this.moveRange = 300;
                this.hitWidth = 90; 
                this.hitHeight = 70;
                this.drawWidth = 90; 
                this.drawHeight = 70;
                break;
                
            case ICE_GOLEM:
                this.attackRange = 200;
                this.moveRange = 800;
                this.hitWidth = 200; 
                this.hitHeight = 150;
                this.drawWidth = 200; 
                this.drawHeight = 150;
                break;
                
            case MAGMA_SLIME_BIG:
                this.attackRange = 350;
                this.moveRange = 700;
                this.hitWidth = 140; 
                this.hitHeight = 140;
                this.drawWidth = 140; 
                this.drawHeight = 140;
                break;
                
            case MAGMA_SLIME_SMALL:
                this.attackRange = 100;
                this.moveRange = 800;
                this.hitWidth = 70; 
                this.hitHeight = 60;
                this.drawWidth = 70; 
                this.drawHeight = 60;
                break;
                
            case FROZEN_KNIGHT:
                this.attackRange = 100;
                this.moveRange = 800;
                this.hitWidth = 200; 
                this.hitHeight = 150;
                this.drawWidth = 200; 
                this.drawHeight = 150;
                break;
                
            case GOLEM:
                this.attackRange = 200;
                this.moveRange = 800;
                this.hitWidth = 200; 
                this.hitHeight = 150;
                this.drawWidth = 200; 
                this.drawHeight = 150;
                this.spriteDefaultFacesLeft = false;
                break;
                
            case HELL_HOUND:
                this.attackRange = 200;
                this.moveRange = 800;
                this.hitWidth = 90; 
                this.hitHeight = 70;
                this.drawWidth = 150; 
                this.drawHeight = 100;
                this.spriteDefaultFacesLeft = false;
                break;
                
            case HELL_KNIGHT:
                this.attackRange = 200;
                this.moveRange = 800;
                this.hitWidth = 90; 
                this.hitHeight = 70;
                this.drawWidth = 120; 
                this.drawHeight = 90;
                this.spriteDefaultFacesLeft = false;
                break;
                
            case YETI:
                this.attackRange = 200;
                this.moveRange = 500;
                this.hitWidth = 200; 
                this.hitHeight = 150;
                this.drawWidth = 200; 
                this.drawHeight = 150;
                break;
                
            case CROCODILE:
                this.attackRange = 100;
                this.moveRange = 500;
                this.hitWidth = 90; 
                this.hitHeight = 70;
                this.drawWidth = 90; 
                this.drawHeight = 70;
                break;
                
            case MUDGOLEM:
                this.attackRange = 200;
                this.moveRange = 500;
                this.hitWidth = 200; 
                this.hitHeight = 150;
                this.drawWidth = 200; 
                this.drawHeight = 150;
                break;
                
            case BOMB_SKULL:
                this.attackRange = 100;
                this.moveRange = 800;
                this.hitWidth = 200; 
                this.hitHeight = 150;
                this.drawWidth = 200; 
                this.drawHeight = 150;
                this.spriteDefaultFacesLeft = false;
                break;
                
            default:
                this.attackRange = 100;
                this.moveRange = 800;
                this.hitWidth = 200; 
                this.hitHeight = 150;
                this.drawWidth = 200; 
                this.drawHeight = 150;
                break;
        }
        
        loadImage(); 
    }

    private BufferedImage ensureARGB(BufferedImage sheet) {
        BufferedImage newSheet = new BufferedImage(
            sheet.getWidth(), 
            sheet.getHeight(), 
            BufferedImage.TYPE_INT_ARGB 
        );
        Graphics2D g2 = newSheet.createGraphics();
        g2.drawImage(sheet, 0, 0, null);
        g2.dispose();
        return newSheet;
    }
    
    private void loadImage() {
        try {
            if (type == EnemyType.SLIME) {
                loadPattern("Slimes.png", 25, 34, 32, 40, 54, 8, IDLE, false);
                loadPattern("Slimes.png", 25, 34, 32, 40, 54, 8, MOVE, false); 
                loadPattern("Slimes.png", 25, 34, 32, 40, 54, 8, ATTACK, false);
                
                isAnimated = true;
            }
            else if (type == EnemyType.GOBLIN) {
                loadPattern("Goblin.png", 0, 54, 32, 38, 34, 16, IDLE);
                loadPattern("Goblin.png", 0, 105, 32, 38, 34, 6, MOVE);
                loadPattern("Goblin.png", 0, 162, 42, 45, 50, 8, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.MAGMA_SLIME_BIG) {
                loadPattern("Slimes.png", 25, 295, 32, 70, 54, 8, IDLE);
                loadPattern("Slimes.png", 25, 295, 32, 70, 54, 8, MOVE);
                loadPattern("Slimes.png", 25, 295, 32, 70, 54, 8, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.MAGMA_SLIME_SMALL) {
                // MAGMA_SLIME_SMALL은 MAGMA_SLIME_BIG과 같은 이미지를 사용하지만 크기만 작음
                loadPattern("Slimes.png", 25, 295, 32, 70, 54, 8, IDLE);
                loadPattern("Slimes.png", 25, 295, 32, 70, 54, 8, MOVE);
                loadPattern("Slimes.png", 25, 295, 32, 70, 54, 8, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.WOLF) {
                loadPattern("Wolf.png", 0, 478, 42, 32, 49, 2, IDLE); 
                loadPattern("Wolf.png", 0, 80, 42, 32, 49, 3, MOVE);
                loadPattern("Wolf.png", 160, 274, 70, 50, 80, 4, ATTACK); 
                isAnimated = true;
            }
            else if (type == EnemyType.ORC) {
                loadPattern("Orc.png", 7, 7, 80, 86, 80, 5, IDLE,false); 
                loadPattern("Orc.png", 7, 7, 80, 86, 80, 5, MOVE,false); 
                loadPattern("Orc.png", 288, 190, 86, 100, 95, 5, ATTACK,false); 
                isAnimated = true;
            }
            else if (type == EnemyType.CROCODILE) {
                loadPattern("Crocodile.png", 0, 0, 58, 25, 66, 1, IDLE);
                loadPattern("Crocodile.png", 0, 0, 58, 25, 66, 3, MOVE);
                loadPattern("Crocodile.png", 5, 83, 52, 25, 65, 3, ATTACK);
                isAnimated = true;
            } 
            else if (type == EnemyType.MUDGOLEM) {
                loadPattern("low Golem.png", 5, 5, 110, 93, 113, 5, IDLE);
                loadPattern("low Golem.png", 5, 106, 110, 93, 113, 5, MOVE);
                loadPattern("low Golem.png", 345, 210, 110, 93, 113, 7, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.SPORE_FLOWER) {
                loadPattern("Flower.png", 7, 32, 24, 27, 26, 2, IDLE);
                loadPattern("Flower.png", 7, 70, 19, 17, 26, 4, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.GOLEM) {
                loadPattern("Golem.png", 2, 2, 75, 60, 82, 11, IDLE);
                loadPattern("Golem.png", 2, 2, 75, 60, 82, 11, MOVE);
                loadPattern("Golem.png", 2, 255, 80, 60, 82, 8, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.FROZEN_KNIGHT) {
                loadPattern("Knight.png", 2, 595, 27, 29, 32, 4, IDLE);
                isAnimated = true;
            }
            else if (type == EnemyType.YETI) {
                loadPattern("Yeti.png", 8, 30, 112, 114, 112, 3, IDLE);
                loadPattern("Yeti.png", 8, 180, 112, 114, 120, 4, MOVE);
                loadPattern("Yeti.png", 10, 330, 100, 214, 160, 4, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.SNOW_MAGE) {
                loadPattern("Snowlady.png", 0, 330, 85, 97, 86, 4, IDLE);
                loadPattern("Snowlady.png", 0, 330, 85, 97, 86, 4, MOVE);
                loadPattern("Snowlady.png", 0, 860, 85, 80, 102, 5, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.BOMB_SKULL) {
                loadPattern("Bomb Skull.png", 1, 1, 17, 29, 19, 4, IDLE);
                loadPattern("Bomb Skull.png", 1, 1, 17, 29, 19, 4, MOVE);
                loadPattern("Bomb Skull.png", 1, 1, 17, 29, 19, 4, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.HELL_HOUND) {
                loadPattern("Hell_Hound.png", 1, 1, 79, 79, 82, 12, IDLE);
                loadPattern("Hell_Hound.png", 1, 1, 79, 79, 82, 12, MOVE);
                loadPattern("Hell_Hound.png", 412, 255, 79, 79, 82, 6, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.FIRE_IMP) {
                loadPattern("Imp.png", 139, 49, 100, 60, 255, 4, IDLE);
                loadPattern("Imp.png", 139, 49, 100, 60, 255, 4, MOVE);
                loadPattern("Imp.png", 640, 40, 100, 85, 255, 3, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.HELL_KNIGHT) {
                loadPattern("Hell_Knight.png", 35, 86, 35, 40, 38, 4, IDLE);
                loadPattern("Hell_Knight.png", 35, 86, 35, 40, 38, 4, IDLE);
                loadPattern("Hell_Knight.png", 3, 340, 47, 40, 50, 4, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.ICE_GOLEM) {
                BufferedImage sheet = ImageIO.read(new File("res/Ice Golem.png"));
                
                sheet = ensureARGB(sheet);
                sheet = removePinkBackground(sheet);
                sheet = removeBlackBackground(sheet); 
                
                if (sprites == null) sprites = new BufferedImage[3][];
                
                sprites[MOVE] = new BufferedImage[10];
                for(int i = 0; i < 6; i++) {
                    sprites[MOVE][i] = sheet.getSubimage(10 + (i * 104), 220, 90, 120);
                }
                for(int i = 0; i < 4; i++) {
                    sprites[MOVE][6 + i] = sheet.getSubimage(10 + (i * 104), 325, 88, 120);
                }
                
                sprites[ATTACK] = new BufferedImage[7];
                for(int i = 0; i < 6; i++) {
                     sprites[ATTACK][i] = sheet.getSubimage(20 + (i * 85), 463, 90, 118);
                }
                for(int i = 0; i < 1; i++) {
                    sprites[ATTACK][6 + i] = sheet.getSubimage(15 + (i * 104), 600, 90, 100);
                }
                
                sprites[IDLE] = new BufferedImage[4];
                for(int i = 0; i < 4; i++) {
                   sprites[IDLE][i] = sheet.getSubimage(12 + (i * 104), 12, 90, 90);
                }

                isAnimated = true;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPattern(String fileName, int startX, int startY, int w, int h, int stride, int count, int state, boolean... useBlackRemoval) throws IOException {
        BufferedImage sheet = ImageIO.read(new File("res/" + fileName));
        
        sheet = ensureARGB(sheet);

        sheet = removePinkBackground(sheet); 
        
        if (useBlackRemoval.length > 0 && useBlackRemoval[0]) {
            sheet = removeBlackBackground(sheet);
        }
        
        if (sprites == null) {
            sprites = new BufferedImage[3][];
        }
        
        sprites[state] = new BufferedImage[count];
        
        for(int i = 0; i < count; i++) {
            int cutX = startX + (i * stride);
            if (cutX + w > sheet.getWidth()) break;
            sprites[state][i] = sheet.getSubimage(cutX, startY, w, h);
        }
    }

    // 배경 제거
    private BufferedImage removePinkBackground(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        
        int[] bgColors = {
            new Color(0xdd, 0x80, 0xe1).getRGB(), // 슬라임
            new Color(0xb8, 0xc8, 0xa8).getRGB(), // 늑대
            new Color(0xFF, 0x00, 0xFF).getRGB(), // 핫핑크
            new Color(0x00, 0xFF, 0xFF).getRGB(), // 오크 파란점
            new Color(0x1a, 0x7a, 0x3e).getRGB(), // 포자꽃
            new Color(0x77, 0xfe, 0xb2).getRGB(), // 설녀
            new Color(0x00, 0x7d, 0x00).getRGB(), // 진한초록
            new Color(0x00, 0x80, 0x80).getRGB(), // 청록
            new Color(0x00, 0xff, 0x00).getRGB(), // 라임
            new Color(0xd6, 0x85, 0xd0).getRGB()  // 격자 슬라임
        };
        
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixelColor = image.getRGB(x, y);
                for(int bg : bgColors) {
                    if(pixelColor == bg) {
                        image.setRGB(x, y, 0); 
                        break;
                    }
                }
            }
        }
        return image;
    }

    // 오직 검은색(#000000)만 지우는 함수 (검은 배경 몬스터 전용)
    private BufferedImage removeBlackBackground(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if ((image.getRGB(x, y) & 0xFFFFFF) == 0) {
                    image.setRGB(x, y, 0);
                }
            }
        }
        return image;
    }
    
    // 데미지를 받는 메서드
    public void takeDamage(int damage) {
        if (!alive) return;
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }
    
    // 적이 죽었는지 확인하는 메서드
    public boolean isDead() {
        return !alive || hp <= 0;
    }
    
    public void update(int targetX, int targetY) {
        if (!alive) return;


        double drawY_world = this.y - (this.hitHeight - 48);
        double enemyCenterX = this.x + (this.drawWidth / 2.0);
        double enemyCenterY = drawY_world + (this.drawHeight / 2.0);
        
        double dx = targetX - enemyCenterX;
        double dy = targetY - enemyCenterY;
        double distance = Math.sqrt(dx*dx + dy*dy);

        double flipDx = targetX - this.x;
        
        if (this.spriteDefaultFacesLeft) {
            if (flipDx > 0) flip = true;
            else flip = false;
        } else {
            if (flipDx > 0) flip = false;
            else flip = true;
        }
        
        // 원거리 몬스터 판별
        boolean isRanged = (type == EnemyType.GOBLIN || 
                            type == EnemyType.SPORE_FLOWER || 
                            type == EnemyType.SNOW_MAGE || 
                            type == EnemyType.FIRE_IMP);

        // 공격 모션이 있는지 확인
        boolean hasAttackAnimation = (sprites != null && 
                                     sprites[ATTACK] != null && 
                                     sprites[ATTACK].length > 0);
        
        // 공격 모션이 진행 중이면 다른 상태로 변경하지 않음
        // 공격 모션이 없는 경우에는 attackAnimationInProgress가 false이므로 상태 변경 가능
        if (attackAnimationInProgress && hasAttackAnimation) {
            // 공격 모션 중에는 이동하지 않음
            if (isRanged) {
                // 원거리 몬스터는 공격 범위에 들어오면 멈춤
            }
            // 공격 모션이 끝날 때까지 대기 (상태 변경하지 않음)
        }
        // 공격 모션이 완료되었거나 공격 모션이 없거나 공격 모션이 아닐 때만 상태 변경
        else {
            // 1. 공격 범위: distance <= attackRange
            if (distance <= attackRange) { 
                if (currentState != ATTACK) {
                    currentState = ATTACK;
                    spriteNum = 0; 
                    spriteCounter = 0;
                    // 공격 모션이 있으면 공격 모션 진행 플래그 설정
                    if (hasAttackAnimation) {
                        attackAnimationInProgress = true;
                    }
                }
                
                if (isRanged) {
                    // 원거리 몬스터는 공격 범위에 들어오면 멈춤
                }
            }
            // 2. 이동/추적 범위: attackRange < distance <= moveRange
            else if (distance <= moveRange) { 
                if (currentState != MOVE) {
                    currentState = MOVE;
                    spriteNum = 0; 
                }
                this.x += (dx / distance) * speed; 
                this.y += (dy / distance) * speed;
            }
            // 3. 대기 범위: distance > moveRange
            else {
                if (currentState != IDLE) {
                    currentState = IDLE;
                    spriteNum = 0; 
                }
            }
        }


        int animState = currentState;
        if (sprites == null || sprites[animState] == null) {
            animState = IDLE;
        }

        if (isAnimated && sprites != null && sprites[animState] != null) {
            spriteCounter++;
            if (spriteCounter > ANIMATION_SPEED) {
                spriteNum++;
                if (spriteNum >= sprites[animState].length) {
                    spriteNum = 0;
                    // 공격 모션이 완료되었는지 확인 (ATTACK 상태이고 spriteNum이 0으로 돌아갔을 때)
                    if (currentState == ATTACK && attackAnimationInProgress) {
                        attackAnimationInProgress = false; // 공격 모션 완료
                    }
                }
                spriteCounter = 0;
            }
        }
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        if (!alive) return;

        // 월드 좌표(this.x, y)를 화면 좌표(screenX, screenY)로 변환
        int screenX = (int) x - cameraX;
        int screenY = (int) y - cameraY;
        
        int drawState = currentState;
        if (sprites == null || sprites[drawState] == null) {
            drawState = IDLE;
        }

        // 오크 공격 모션 방향 수정 로직 (제거된 상태 유지)
        boolean currentFlip = flip;
        
        // drawY는 히트박스 높이를 기준으로 계산
        int drawY = screenY - (hitHeight - 48); 
        
        if (isAnimated && sprites != null && sprites[drawState] != null) {
            
            if (currentFlip) { 
                // drawWidth와 drawHeight 사용
                g2.drawImage(sprites[drawState][spriteNum], screenX + drawWidth, drawY, -drawWidth, drawHeight, null);
            } else { 
                // drawWidth와 drawHeight 사용
                g2.drawImage(sprites[drawState][spriteNum], screenX, drawY, drawWidth, drawHeight, null);
            }
        } 
        

        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2.0f));
        

        double spriteCenterX = screenX + (drawWidth / 2.0);
        double spriteCenterY = drawY + (drawHeight / 2.0);
        
        // 히트박스는 몬스터(스프라이트)의 중심에 맞춰서 그려집니다
        int hitBoxX = (int)(spriteCenterX - (hitWidth / 2.0));
        int hitBoxY = (int)(spriteCenterY - (hitHeight / 2.0));
        

        g2.drawRect(hitBoxX, hitBoxY, hitWidth, hitHeight);

        int hpBarWidth = 40; 
        int hpBarHeight = 5; 
        
        int barY = screenY - (hitHeight - 48); 
        int barX = screenX + (hitWidth / 2) - (hpBarWidth / 2); 
        
        g2.setColor(Color.GRAY);
        g2.fillRect(barX, barY, hpBarWidth, hpBarHeight);
        
        int currentHpWidth = (int)((double)hp / maxHp * hpBarWidth);
        g2.setColor(Color.GREEN);
        g2.fillRect(barX, barY, currentHpWidth, hpBarHeight);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        
        int textY = barY - 5; 
        int textX = barX + (hpBarWidth / 2) - (g2.getFontMetrics().stringWidth(name) / 2);
        
        g2.drawString(name, textX, textY); 
    }
}