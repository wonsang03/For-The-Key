package enemy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

// [서상원님 코드] 적 시스템: AI, 애니메이션, 투사체, 공격 판정
public class Enemy {

    public EnemyType type;
    private String name;
    private int hp;
    private int maxHp;
    private double speed;
    private int attackRange;
    private int moveRange; 
    public double x, y;
    public int hitWidth;
    public int hitHeight;
    public int drawWidth;
    public int drawHeight;
    public boolean alive = true;
    public boolean isFixed = false;
    
    public final int IDLE = 0;   
    public final int MOVE = 1;   
    public final int ATTACK = 2; 
    public int currentState = IDLE; 
    private boolean attackAnimationInProgress = false;
    private boolean meleeAttackApplied = false;
    
    private boolean flip = false; 
    private boolean spriteDefaultFacesLeft = true; 
    private BufferedImage[][] sprites;   
    private boolean isAnimated = false; 
    private int spriteCounter = 0;
    private int spriteNum = 0;
    private final int ANIMATION_SPEED = 9;
    
    private ArrayList<SlimeProjectile> projectiles = new ArrayList<>();
    private boolean projectileCreated = false;
    
    private int lastPlayerX = 0;
    private int lastPlayerY = 0;

    // [서상원님 코드] 적 생성자
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
                this.spriteDefaultFacesLeft = false;
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
                this.attackRange = 100;
                this.moveRange = 500;
                this.hitWidth = 80; 
                this.hitHeight = 70;
                this.drawWidth = 80; 
                this.drawHeight = 70;
                this.spriteDefaultFacesLeft = false;
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
            case MINOTAUR:
                this.attackRange = 200;
                this.moveRange = 500;
                this.hitWidth = 200; 
                this.hitHeight = 150;
                this.drawWidth = 200; 
                this.drawHeight = 150;
                this.spriteDefaultFacesLeft = false;
                break;
            case SPORE_FLOWER:
                this.attackRange = 600;
                this.moveRange = 800;
                this.hitWidth = 90; 
                this.hitHeight = 70;
                this.drawWidth = 150; 
                this.drawHeight = 120;
                break;
            case FIRE_IMP:
                this.attackRange = 200;
                this.moveRange = 500;
                this.hitWidth = 105; 
                this.hitHeight = 70;
                this.drawWidth = 90; 
                this.drawHeight = 70;
                this.spriteDefaultFacesLeft = false;
                break;
            case SNOW_MAGE:
                this.attackRange = 100;
                this.moveRange = 300;
                this.hitWidth = 90; 
                this.hitHeight = 70;
                this.drawWidth = 90; 
                this.drawHeight = 70;
                this.spriteDefaultFacesLeft = false;
                break;
            case ICE_GOLEM:
                this.attackRange = 200;
                this.moveRange = 800;
                this.hitWidth = 200;
                this.hitHeight = 150;
                this.drawWidth = 200; 
                this.drawHeight = 150;
                this.spriteDefaultFacesLeft = false;
                break;
            case MAGMA_SLIME_BIG:
                this.attackRange = 350;
                this.moveRange = 700;
                this.hitWidth = 140; 
                this.hitHeight = 140;
                this.drawWidth = 140; 
                this.drawHeight = 140;
                this.spriteDefaultFacesLeft = false;
                break;
            case MAGMA_SLIME_SMALL:
                this.attackRange = 100;
                this.moveRange = 800;
                this.hitWidth = 70; 
                this.hitHeight = 60;
                this.drawWidth = 70; 
                this.drawHeight = 60;
                this.spriteDefaultFacesLeft = false;
                break;
            case FROZEN_KNIGHT:
                this.attackRange = 200;
                this.moveRange = 500;
                this.hitWidth = 150; 
                this.hitHeight = 100;
                this.drawWidth = 150; 
                this.drawHeight = 100;
                this.spriteDefaultFacesLeft = false;
                break;
            case GOLEM:
                this.attackRange = 200;
                this.moveRange = 500;
                this.hitWidth = 200; 
                this.hitHeight = 150;
                this.drawWidth = 200; 
                this.drawHeight = 150;
                this.spriteDefaultFacesLeft = false;
                break;
            case HELL_HOUND:
                this.attackRange = 200;
                this.moveRange = 500;
                this.hitWidth = 90; 
                this.hitHeight = 70;
                this.drawWidth = 90; 
                this.drawHeight = 70;
                break;
            case HELL_KNIGHT:
                this.attackRange = 200;
                this.moveRange = 500;
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
                this.spriteDefaultFacesLeft = false;
                break;
            case SNAKE:
                this.attackRange = 100;
                this.moveRange = 500;
                this.hitWidth = 90; 
                this.hitHeight = 70;
                this.drawWidth = 90; 
                this.drawHeight = 70;
                this.spriteDefaultFacesLeft = false;
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
                this.moveRange = 500;
                this.hitWidth = 90; 
                this.hitHeight = 70;
                this.drawWidth = 90; 
                this.drawHeight = 70;
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

    // [서상원님 코드] 이미지 처리
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
    
    // [서상원님 코드] 스프라이트 이미지 로드
    private void loadImage() {
        try {
            if (type == EnemyType.SLIME) {
                loadPattern("Slime1.png", 20, 317, 30, 20, 80, 2, IDLE, false);
                loadPattern("Slime2.png", 20, 317, 30, 20, 80, 7, MOVE, false); 
                loadPattern("Slime3.png", 20, 317, 50, 20, 80, 12, ATTACK, false);
                isAnimated = true;
            }
            else if (type == EnemyType.GOBLIN) {
                loadPattern("Goblin.png", 10, 70, 40, 58, 70, 1, IDLE);
                loadPattern("Goblin.png", 140, 70, 40, 58, 65, 5, MOVE);
                loadPattern("Goblin.png", 400, 70, 40, 58, 65, 5, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.MAGMA_SLIME_BIG) {
                loadPattern("Slime1.png", 20, 102, 30, 20, 80, 2, IDLE, false);
                loadPattern("Slime2.png", 20, 102, 30, 20, 80, 7, MOVE, false); 
                loadPattern("Slime3.png", 20, 102, 50, 20, 80, 12, ATTACK, false);
                isAnimated = true;
            }
            else if (type == EnemyType.MAGMA_SLIME_SMALL) {
                loadPattern("Slime1.png", 20, 102, 30, 20, 80, 2, IDLE, false);
                loadPattern("Slime2.png", 20, 102, 30, 20, 80, 7, MOVE, false); 
                loadPattern("Slime3.png", 20, 102, 50, 20, 80, 12, ATTACK, false);
                isAnimated = true;
            }
            else if (type == EnemyType.WOLF) {
                loadPattern("Wolf.png", 0, 35, 65, 30, 1, 1, IDLE); 
                loadPattern("Wolf.png", 0, 35, 61, 30, 64, 5, MOVE);
                loadPattern("Wolf.png", 0, 95, 61, 30, 64, 5, ATTACK); 
                isAnimated = true;
            }
            else if (type == EnemyType.MINOTAUR) {
                loadPattern("Minotaur.png", 20, 20, 60, 50, 96, 5, IDLE,false); 
                loadPattern("Minotaur.png", 20, 110, 60, 50, 96, 8, MOVE,false); 
                loadPattern("Minotaur.png", 20, 305, 60, 50, 96, 4, ATTACK,false); 
                isAnimated = true;
            }
            else if (type == EnemyType.SNAKE) {
                loadPattern("Snake.png", 0, 0, 32, 30, 32, 7, IDLE);
                loadPattern("Snake.png", 0, 0, 32, 30, 32, 7, MOVE);
                loadPattern("Snake.png", 0, 0, 32, 30, 32, 7, ATTACK);
                isAnimated = true;
            } 
            else if (type == EnemyType.MUDGOLEM) {
                loadPattern("Fire Golem.png", 15, 15, 30, 35, 64, 4, IDLE);
                loadPattern("Fire Golem.png", 15, 80, 30, 35, 64, 8, MOVE);
                loadPattern("Fire Golem.png", 143, 272, 30, 35, 64, 5, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.SPORE_FLOWER) {
                loadPattern("Flower.png", 0, 0, 255, 255, 0, 1, IDLE);
                loadPattern("Flower.png", 0, 0, 255, 255, 0, 5, IDLE);
                isAnimated = true;
            }
            else if (type == EnemyType.GOLEM) {
                loadPattern("Golem1.png", 20, 20, 50, 40, 90, 8, IDLE);
                loadPattern("Golem2.png", 20, 20, 50, 40, 90, 10, MOVE);
                loadPattern("Golem3.png", 295, 20, 50, 40, 90, 7, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.FROZEN_KNIGHT) {
                loadPattern("Ice Knight.png", 0, 5, 30, 20, 32, 4, IDLE);
                loadPattern("Ice Knight.png", 0, 38, 30, 20, 32, 8, MOVE);
                loadPattern("Ice Knight.png", 0, 520, 30, 20, 32, 5, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.YETI) {
                loadPattern("Yeti.png", 5, 0, 60, 55, 64, 5, IDLE);
                loadPattern("Yeti.png", 10, 70, 60, 55, 64, 7, MOVE);
                loadPattern("Yeti.png", 10, 140, 60, 53, 64, 5, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.SNOW_MAGE) {
                loadPattern("Ice Mage.png", 10, 65, 30, 60, 60, 1, IDLE);
                loadPattern("Ice Mage.png", 10, 65, 30, 60, 48, 3, MOVE);
                loadPattern("Ice Mage.png", 10, 65, 30, 60, 48,3, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.BOMB_SKULL) {
                loadPattern("Skull1.png", 10, 0, 50, 55, 64, 9, IDLE);
                loadPattern("Skull1.png", 10, 0, 50, 55, 64, 9, MOVE);
                loadPattern("Skull2.png", 10, 0, 50, 60, 64, 3, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.HELL_HOUND) {
                loadPattern("hell-hound1.png", 10, 0, 45, 30, 64, 6, IDLE);
                loadPattern("hell-hound2.png", 10, 0, 45, 30, 64, 12, MOVE);
                loadPattern("hell-hound3.png", 10, 0, 50, 45, 64, 6, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.FIRE_IMP) {
                loadPattern("Imp.png", 5, 10, 20, 20, 30, 4, IDLE);
                loadPattern("Imp.png", 5, 75, 20, 20, 26, 6, MOVE);
                loadPattern("Imp.png", 5, 45, 20, 20, 30, 4, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.HELL_KNIGHT) {
                loadPattern("Hell_Knight.png", 20, 30, 40, 35, 80, 9, IDLE);
                loadPattern("Hell_Knight.png", 20, 110, 40, 35, 80, 6, MOVE);
                loadPattern("Hell_Knight.png", 20, 180, 50, 50, 80, 6, ATTACK);
                isAnimated = true;
            }
            else if (type == EnemyType.ICE_GOLEM) {
                loadPattern("Ice Golem1.png", 20, 20, 50, 40, 90, 8, IDLE);
                loadPattern("Ice Golem2.png", 20, 20, 50, 40, 90, 10, MOVE);
                loadPattern("Ice Golem3.png", 295, 20, 50, 40, 90, 7, ATTACK);
                isAnimated = true;
            }
        } catch (IOException e) {
        }
    }

    // [서상원님 코드] 스프라이트 시트에서 애니메이션 프레임 추출
    private void loadPattern(String fileName, int startX, int startY, int w, int h, int stride, int count, int state, boolean... useBlackRemoval) throws IOException {
        try {
            File imageFile = new File("res/" + fileName);
            BufferedImage sheet = ImageIO.read(imageFile);
            if (sheet == null) {
                return;
            }
            
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
                if (startY + h > sheet.getHeight()) break;
                sprites[state][i] = sheet.getSubimage(cutX, startY, w, h);
            }
        } catch (Exception e) {
            return;
        }
    }

    // [서상원님 코드] 핑크/색상 배경 제거
    private BufferedImage removePinkBackground(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        
        int[] bgColors = {
            new Color(0xdd, 0x80, 0xe1).getRGB(), // 슬라임 핑크
            new Color(0xb8, 0xc8, 0xa8).getRGB(), // 늑대 배경
            new Color(0xFF, 0x00, 0xFF).getRGB(), // 핫핑크
            new Color(0x00, 0xFF, 0xFF).getRGB(), // 오크 파란점
            new Color(0x1a, 0x7a, 0x3e).getRGB(), // 포자꽃 초록
            new Color(0x77, 0xfe, 0xb2).getRGB(), // 설녀 배경
            new Color(0x00, 0x7d, 0x00).getRGB(), // 진한초록
            new Color(0x00, 0x80, 0x80).getRGB(), // 청록
            new Color(0x00, 0xff, 0x00).getRGB(), // 라임
            new Color(0xd6, 0x85, 0xd0).getRGB()
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

    // [서상원님 코드] 검은색 배경 제거
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
    
    public boolean shouldExplode = false;
    
    // [서상원님 코드] 데미지 처리
    public void takeDamage(int damage) {
        if (!alive) return;
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }
    
    public boolean isDead() {
        return !alive || hp <= 0;
    }
    
    // [서상원님 코드] AI 업데이트: 플레이어 추적, 공격, 이동
    public void update(int targetX, int targetY) {
        if (!alive) return;
        
        lastPlayerX = targetX;
        lastPlayerY = targetY;
        double drawY_world = this.y - (this.hitHeight - 48);
        double enemyCenterX = this.x + (this.drawWidth / 2.0);
        double enemyCenterY = drawY_world + (this.drawHeight / 2.0);
        
        double dx = targetX - enemyCenterX;
        double dy = targetY - enemyCenterY;
        double distance = Math.sqrt(dx*dx + dy*dy);

        double flipDx = targetX - this.x;
        if (this.spriteDefaultFacesLeft) {
            flip = flipDx > 0;
        } else {
            flip = flipDx <= 0;
        }

        boolean hasAttackAnimation = (sprites != null && 
                                     sprites[ATTACK] != null && 
                                     sprites[ATTACK].length > 0);
        boolean isRanged = (type == EnemyType.SPORE_FLOWER || 
                           type == EnemyType.FIRE_IMP || 
                           type == EnemyType.SNOW_MAGE);
        
        if (attackAnimationInProgress && hasAttackAnimation) {
            if (distance <= attackRange && isRanged) {
                createProjectileIfNeeded(targetX, targetY);
            }
        } else {
            if (distance <= attackRange) { 
                if (currentState != ATTACK || (currentState == ATTACK && !attackAnimationInProgress)) {
                    currentState = ATTACK;
                    spriteNum = 0; 
                    spriteCounter = 0;
                    projectileCreated = false;
                    meleeAttackApplied = false;
                    if (hasAttackAnimation) {
                        attackAnimationInProgress = true;
                    } else if (isRanged) {
                        attackAnimationInProgress = true;
                    }
                }
                createProjectileIfNeeded(targetX, targetY);
            } else if (distance <= moveRange) { 
                if (currentState != MOVE) {
                    currentState = MOVE;
                    spriteNum = 0; 
                }
                if (!isFixed) {
                    this.x += (dx / distance) * speed; 
                    this.y += (dy / distance) * speed;
                }
            } else {
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
                    if (currentState == ATTACK && attackAnimationInProgress) {
                        attackAnimationInProgress = false;
                        projectileCreated = false;
                        meleeAttackApplied = false;
                        
                        if (type == EnemyType.BOMB_SKULL) {
                            shouldExplode = true;
                        }
                    }
                }
                spriteCounter = 0;
            }
        } 
        else if (currentState == ATTACK && attackAnimationInProgress) {
            spriteCounter++;
            if (spriteCounter > ANIMATION_SPEED * 2) {
                attackAnimationInProgress = false;
                projectileCreated = false;
                meleeAttackApplied = false;
                spriteCounter = 0;
                
                if (type == EnemyType.BOMB_SKULL) {
                    shouldExplode = true;
                }
            }
        }
        
        updateProjectiles();
    }
    
    // [서상원님 코드] 투사체 생성
    private void createProjectileIfNeeded(int targetX, int targetY) {
        if (!isRanged()) return;
        if (currentState != ATTACK) return;
        if (!attackAnimationInProgress) return;
        if (projectileCreated) return;
        
        boolean shouldCreate = false;
        if (sprites != null && sprites[ATTACK] != null && sprites[ATTACK].length > 0) {
            int totalFrames = sprites[ATTACK].length;
            int projectileFrame = totalFrames / 2;
            if (spriteNum >= projectileFrame) {
                shouldCreate = true;
            }
        } else {
            shouldCreate = true;
        }
        
        if (shouldCreate) {
            double drawY_world = this.y - (this.hitHeight - 48);
            double enemyCenterX = this.x + (this.drawWidth / 2.0);
            double enemyCenterY = drawY_world + (this.drawHeight / 2.0);
            double projectileSpeed = 7.0;
            int projectileDamage = type.getAttack();
            double projectileRange = attackRange * 2.0;
            int projWidth = 16;
            int projHeight = 16;
            
            projectiles.add(new SlimeProjectile(
                enemyCenterX, enemyCenterY,
                targetX, targetY,
                projectileSpeed,
                projectileDamage,
                projectileRange,
                projWidth,
                projHeight,
                true
            ));
            
            projectileCreated = true;
        }
    }
    
    // [서상원님 코드] 투사체 업데이트
    private void updateProjectiles() {
        projectiles.removeIf(p -> {
            p.update();
            return !p.isActive();
        });
    }
    
    // [서상원님 코드] 투사체 그리기
    public void drawProjectiles(Graphics2D g2, int cameraX, int cameraY) {
        for (SlimeProjectile projectile : projectiles) {
            if (projectile.isActive()) {
                projectile.draw(g2, cameraX, cameraY);
            }
        }
    }
    
    // [서상원님 코드] 투사체 리스트 반환
    public ArrayList<SlimeProjectile> getProjectiles() {
        return projectiles;
    }
    
    // [서상원님 코드] 근거리 공격 판정: 부채꼴(120도) 공격 범위 체크
    public boolean canAttackPlayer(int playerX, int playerY) {
        if (isRanged()) return false;
        if (currentState != ATTACK) return false;
        if (!attackAnimationInProgress) return false;
        if (meleeAttackApplied) return false;
        if (!isHalfFrame()) {
            return false;
        }
        
        double drawY_world = this.y - (this.hitHeight - 48);
        double enemyCenterX = this.x + (this.drawWidth / 2.0);
        double enemyCenterY = drawY_world + (this.drawHeight / 2.0);
        
        double dx = playerX - enemyCenterX;
        double dy = playerY - enemyCenterY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > attackRange) {
            return false;
        }
        
        double attackDirection = Math.atan2(dy, dx);
        double playerDirX = dx / distance;
        double playerDirY = dy / distance;
        double attackDirX = Math.cos(attackDirection);
        double attackDirY = Math.sin(attackDirection);
        double dotProduct = playerDirX * attackDirX + playerDirY * attackDirY;
        
        boolean canAttack = dotProduct >= 0.5;
        
        if (canAttack) {
            meleeAttackApplied = true;
        }
        
        return canAttack;
    }
    
    public int getAttackDamage() {
        return type.getAttack();
    }
    
    // [사운드] 공격 사운드 재생 여부 확인
    public boolean shouldPlayAttackSound() {
        if (currentState != ATTACK || !attackAnimationInProgress) return false;
        // 공격 애니메이션 중간 프레임에서 사운드 재생
        if (sprites != null && sprites[ATTACK] != null && sprites[ATTACK].length > 0) {
            int totalFrames = sprites[ATTACK].length;
            int soundFrame = totalFrames / 3; // 애니메이션의 1/3 지점에서 사운드 재생
            return (spriteNum == soundFrame && spriteCounter == 0);
        }
        return false;
    }
    
    // [사운드] 공격 사운드 인덱스 반환
    public int getAttackSoundIndex() {
        // 적 타입에 따라 사운드 인덱스 반환
        switch (type) {
            case SLIME:
            case MAGMA_SLIME_BIG:
            case MAGMA_SLIME_SMALL:
                return 25; // slime_walk
            case WOLF:
            case HELL_HOUND:
                return 26; // bite
            case GOBLIN:
            case MINOTAUR:
            case HELL_KNIGHT:
            case FROZEN_KNIGHT:
                return 22; // enemy_swing1
            case SPORE_FLOWER:
            case FIRE_IMP:
            case SNOW_MAGE:
                return 24; // enemy_throw
            case BOMB_SKULL:
                return 27; // ice_shatter (폭발음 대체)
            default:
                return 22; // 기본값: enemy_swing1
        }
    }
    
    // [서상원님 코드] 헬퍼 메서드
    private boolean isRanged() {
        return (type == EnemyType.SPORE_FLOWER || 
                type == EnemyType.FIRE_IMP || 
                type == EnemyType.SNOW_MAGE);
    }
    
    private boolean isHalfFrame() {
        if (sprites == null || sprites[ATTACK] == null || sprites[ATTACK].length == 0) {
            return true;
        }
        int totalFrames = sprites[ATTACK].length;
        int halfFrame = totalFrames / 2;
        return (spriteNum >= halfFrame && spriteNum <= halfFrame + 1);
    }

    // [서상원님 코드] 렌더링: 스프라이트, 히트박스, HP바, 투사체
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        if (!alive) return;

        int screenX = (int) x - cameraX;
        int screenY = (int) y - cameraY;
        
        int drawState = currentState;
        if (sprites == null || sprites[drawState] == null) {
            drawState = IDLE;
        }

        boolean currentFlip = flip;
        int drawY = screenY - (hitHeight - 48);
        double drawY_world = this.y - (this.hitHeight - 48);
        
        if (isAnimated && sprites != null && sprites[drawState] != null) {
            if (currentFlip) { 
                g2.drawImage(sprites[drawState][spriteNum], screenX + drawWidth, drawY, -drawWidth, drawHeight, null);
            } else { 
                g2.drawImage(sprites[drawState][spriteNum], screenX, drawY, drawWidth, drawHeight, null);
            }
        } 

        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2.0f));

        double spriteCenterX = screenX + (drawWidth / 2.0);
        double spriteCenterY = (drawY_world - cameraY) + (drawHeight / 2.0);
        
        int hitBoxX = (int)(spriteCenterX - (hitWidth / 2.0));
        int hitBoxY = (int)(spriteCenterY - (hitHeight / 2.0));

        g2.drawRect(hitBoxX, hitBoxY, hitWidth, hitHeight);
        
        if (currentState == ATTACK && attackAnimationInProgress) {
            boolean shouldShowHitbox = isHalfFrame();
            if (!isRanged()) {
                shouldShowHitbox = true;
            }
            
            if (shouldShowHitbox) {
                if (type == EnemyType.BOMB_SKULL) {
                    int explosionRange = 200;
                    g2.setColor(new Color(255, 165, 0, 120));
                    g2.fillOval(
                        (int)(spriteCenterX - explosionRange),
                        (int)(spriteCenterY - explosionRange),
                        explosionRange * 2,
                        explosionRange * 2
                    );
                    g2.setColor(new Color(255, 140, 0));
                    g2.setStroke(new BasicStroke(3.0f));
                    g2.drawOval(
                        (int)(spriteCenterX - explosionRange),
                        (int)(spriteCenterY - explosionRange),
                        explosionRange * 2,
                        explosionRange * 2
                    );
                } else if (!isRanged()) {
                    int attackRangeRadius = attackRange;
                    
                    double enemyCenterX_world = this.x + (this.drawWidth / 2.0);
                    double enemyCenterY_world = drawY_world + (this.drawHeight / 2.0);
                    
                    double dx = lastPlayerX - enemyCenterX_world;
                    double dy = lastPlayerY - enemyCenterY_world;
                    double angle = Math.atan2(dy, dx);
                    
                    double angleDeg = Math.toDegrees(angle);
                    double fillArcAngle = -angleDeg;
                    double startAngleDeg = fillArcAngle - 60;
                    int arcAngleDeg = 120;
                    
                    int startAngle = (int)Math.round(startAngleDeg);
                    int arcAngle = arcAngleDeg;
                    
                    g2.setColor(new Color(255, 0, 0, 150));
                    g2.fillArc(
                        (int)(spriteCenterX - attackRangeRadius),
                        (int)(spriteCenterY - attackRangeRadius),
                        attackRangeRadius * 2,
                        attackRangeRadius * 2,
                        startAngle,
                        arcAngle
                    );
                    
                    g2.setColor(Color.RED);
                    g2.setStroke(new BasicStroke(3.0f));
                    g2.drawArc(
                        (int)(spriteCenterX - attackRangeRadius),
                        (int)(spriteCenterY - attackRangeRadius),
                        attackRangeRadius * 2,
                        attackRangeRadius * 2,
                        startAngle,
                        arcAngle
                    );
                }
            }
        }

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
        
        drawProjectiles(g2, cameraX, cameraY);
    }
    
    // [서상원님 코드] 투사체 클래스: 원거리 몬스터가 발사하는 투사체 관리
    private class SlimeProjectile {
        private double x, y;
        private double dx, dy;
        private double speed;
        private int damage;
        private double distanceTraveled;
        private double range;
        private boolean active = true;
        private int hitWidth;
        private int hitHeight;
        private boolean isRanged;
        
        public SlimeProjectile(double startX, double startY, double targetX, double targetY, 
                              double speed, int damage, double range, int hitWidth, int hitHeight, boolean isRanged) {
            this.x = startX;
            this.y = startY;
            this.speed = speed;
            this.damage = damage;
            this.range = range;
            this.hitWidth = hitWidth;
            this.hitHeight = hitHeight;
            this.isRanged = isRanged;
            
            double dx = targetX - startX;
            double dy = targetY - startY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance > 0) {
                this.dx = (dx / distance) * speed;
                this.dy = (dy / distance) * speed;
            } else {
                this.dx = 0;
                this.dy = 0;
            }
            
            distanceTraveled = 0;
        }
        
        public void update() {
            if (!active) return;
            
            x += dx;
            y += dy;
            distanceTraveled += speed;
            
            if (distanceTraveled >= range) {
                active = false;
            }
        }
        
        public void draw(Graphics2D g2, int cameraX, int cameraY) {
            if (!active) return;
            
            int screenX = (int)x - cameraX;
            int screenY = (int)y - cameraY;
            
            if (isRanged) {
                g2.setColor(Color.YELLOW);
                g2.fillRect(screenX - hitWidth / 2, screenY - hitHeight / 2, hitWidth, hitHeight);
                g2.setColor(new Color(200, 150, 0));
                g2.drawRect(screenX - hitWidth / 2, screenY - hitHeight / 2, hitWidth, hitHeight);
            } 
            else {
                g2.setColor(Color.GREEN);
                g2.fillRect(screenX - hitWidth / 2, screenY - hitHeight / 2, hitWidth, hitHeight);
                g2.setColor(Color.WHITE);
                g2.drawRect(screenX - hitWidth / 2, screenY - hitHeight / 2, hitWidth, hitHeight);
            }
        }
        
        public Rectangle getHitBox() {
            return new Rectangle(
                (int)(x - hitWidth / 2.0),
                (int)(y - hitHeight / 2.0),
                hitWidth,
                hitHeight
            );
        }
        
        public void deactivate() { active = false; }
        public boolean isActive() { return active; }
        public int getDamage() { return damage; }
        public double getX() { return x; }
        public double getY() { return y; }
    }
}
