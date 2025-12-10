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

public class Enemy {

    // ========== 필드 변수 ==========
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
    
    // 상태 관리
    public final int IDLE = 0;   
    public final int MOVE = 1;   
    public final int ATTACK = 2; 
    public int currentState = IDLE; 
    private boolean attackAnimationInProgress = false;
    private boolean meleeAttackApplied = false;
    
    // 스프라이트 및 애니메이션
    private boolean flip = false; 
    private boolean spriteDefaultFacesLeft = true; 
    private BufferedImage[][] sprites;   
    private boolean isAnimated = false; 
    private int spriteCounter = 0;
    private int spriteNum = 0;
    private final int ANIMATION_SPEED = 9;
    
    // 투사체 관리
    private ArrayList<SlimeProjectile> projectiles = new ArrayList<>();
    private boolean projectileCreated = false;
    
    // 플레이어 위치 추적
    private int lastPlayerX = 0;
    private int lastPlayerY = 0;

    // ========== 생성자 ==========
    // 적 생성자: 타입별 스탯 및 크기 설정 후 스프라이트 이미지 로드
    public Enemy(EnemyType type, double startX, double startY) {
        // 기본 정보 설정
        this.type = type;
        this.x = startX;
        this.y = startY;
        this.name = type.getName();
        this.maxHp = type.getMaxHp();
        this.hp = this.maxHp;
        this.speed = type.getSpeed();
        
        // 기본값 설정 (타입별로 switch문에서 덮어씀)
        this.attackRange = 300; 
        this.moveRange = 600; 
        this.hitWidth = 48; 
        this.hitHeight = 48; 
        this.drawWidth = 48; 
        this.drawHeight = 48;
        
        // 타입별 스탯 및 크기 설정
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
                this.attackRange = 200;
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
        
        // 스프라이트 이미지 로드
        loadImage(); 
    }

    // ========== 이미지 처리 ==========
    // 이미지를 ARGB 형식으로 변환 (투명도 처리용)
    private BufferedImage ensureARGB(BufferedImage sheet) {
        // ARGB 형식의 새 이미지 생성 (투명도 처리용)
        BufferedImage newSheet = new BufferedImage(
            sheet.getWidth(), 
            sheet.getHeight(), 
            BufferedImage.TYPE_INT_ARGB 
        );
        // 원본 이미지를 새 이미지에 복사
        Graphics2D g2 = newSheet.createGraphics();
        g2.drawImage(sheet, 0, 0, null);
        g2.dispose();
        return newSheet;
    }
    
    // 적 타입별 스프라이트 이미지 로드
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

    // 스프라이트 시트에서 애니메이션 프레임 추출 (가로 방향)
    private void loadPattern(String fileName, int startX, int startY, int w, int h, int stride, int count, int state, boolean... useBlackRemoval) throws IOException {
        try {
            // 스프라이트 시트 파일 읽기
            BufferedImage sheet = ImageIO.read(new File("res/" + fileName));
            if (sheet == null) {
                return;
            }
            
            // 이미지 형식 변환 및 배경 제거
            sheet = ensureARGB(sheet);
            sheet = removePinkBackground(sheet); 
            
            // 검은색 배경 제거 옵션이 있으면 실행
            if (useBlackRemoval.length > 0 && useBlackRemoval[0]) {
                sheet = removeBlackBackground(sheet);
            }
            
            // 스프라이트 배열 초기화 (IDLE, MOVE, ATTACK 3가지 상태)
            if (sprites == null) {
                sprites = new BufferedImage[3][];
            }
            
            // 해당 상태의 프레임 배열 생성
            sprites[state] = new BufferedImage[count];
            
            // 가로 방향으로 프레임 추출
            for(int i = 0; i < count; i++) {
                int cutX = startX + (i * stride);
                // 이미지 경계 체크: 범위를 벗어나면 중단
                if (cutX + w > sheet.getWidth()) break;
                if (startY + h > sheet.getHeight()) break;
                // 프레임 추출
                sprites[state][i] = sheet.getSubimage(cutX, startY, w, h);
            }
        } catch (Exception e) {
            return;
        }
    }

    // 핑크/색상 배경 제거
    private BufferedImage removePinkBackground(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        
        // 제거할 배경 색상 목록 (RGB 값)
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
            new Color(0xd6, 0x85, 0xd0).getRGB()  // 격자 슬라임
        };
        
        // 모든 픽셀을 순회하며 배경 색상 제거
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixelColor = image.getRGB(x, y);
                // 배경 색상 목록과 일치하면 투명하게 변경
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

    // 검은색 배경 제거
    private BufferedImage removeBlackBackground(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        
        // 모든 픽셀을 순회하며 검은색 픽셀을 투명하게 변경
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                // RGB 값이 0 (검은색)이면 투명하게 변경
                if ((image.getRGB(x, y) & 0xFFFFFF) == 0) {
                    image.setRGB(x, y, 0);
                }
            }
        }
        return image;
    }
    
    // ========== 데미지 및 상태 관리 ==========
    // 데미지 받기: HP 차감 후 사망 처리
    public void takeDamage(int damage) {
        // 이미 사망한 적은 데미지를 받지 않음
        if (!alive) return;
        
        // HP 차감
        hp -= damage;
        
        // HP가 0 이하가 되면 사망 처리
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }
    
    // 사망 여부 확인
    public boolean isDead() {
        return !alive || hp <= 0;
    }
    
    // ========== AI 업데이트 ==========
    // 적 AI 업데이트: 플레이어 추적, 상태 전환, 애니메이션 처리
    public void update(int targetX, int targetY) {
        // 사망한 적은 업데이트하지 않음
        if (!alive) return;
        
        // 플레이어 위치 저장 (부채꼴 히트박스 표시용)
        lastPlayerX = targetX;
        lastPlayerY = targetY;

        // 적 중심 좌표 계산 (월드 좌표)
        double drawY_world = this.y - (this.hitHeight - 48);
        double enemyCenterX = this.x + (this.drawWidth / 2.0);
        double enemyCenterY = drawY_world + (this.drawHeight / 2.0);
        
        // 플레이어와의 거리 계산
        double dx = targetX - enemyCenterX;
        double dy = targetY - enemyCenterY;
        double distance = Math.sqrt(dx*dx + dy*dy);

        // 스프라이트 방향 전환 (플레이어를 향한 방향)
        double flipDx = targetX - this.x;
        if (this.spriteDefaultFacesLeft) {
            // 기본이 왼쪽을 보는 경우: 플레이어가 오른쪽에 있으면 반전
            if (flipDx > 0) flip = true;
            else flip = false;
        } else {
            // 기본이 오른쪽을 보는 경우: 플레이어가 왼쪽에 있으면 반전
            if (flipDx > 0) flip = false;
            else flip = true;
        }

        // 공격 애니메이션 존재 여부 확인
        boolean hasAttackAnimation = (sprites != null && 
                                     sprites[ATTACK] != null && 
                                     sprites[ATTACK].length > 0);
        
        // 원거리 몬스터 여부 확인
        boolean isRanged = (type == EnemyType.GOBLIN ||
                           type == EnemyType.SPORE_FLOWER || 
                           type == EnemyType.FIRE_IMP || 
                           type == EnemyType.SNOW_MAGE);
        
        // 공격 애니메이션 진행 중인 경우
        if (attackAnimationInProgress && hasAttackAnimation) {
            // 원거리 몬스터는 공격 범위 내에 있으면 투사체 생성
            if (distance <= attackRange && isRanged) {
                createProjectileIfNeeded(targetX, targetY);
            }
        }
        // 공격 애니메이션이 진행 중이 아닌 경우
        else {
            // 공격 범위 내에 있는 경우
            if (distance <= attackRange) { 
                // 공격 상태로 전환 (새로운 공격 시작)
                if (currentState != ATTACK || (currentState == ATTACK && !attackAnimationInProgress)) {
                    currentState = ATTACK;
                    spriteNum = 0; 
                    spriteCounter = 0;
                    projectileCreated = false;
                    meleeAttackApplied = false;
                    // 공격 애니메이션이 있으면 진행 시작, 없어도 원거리는 투사체 발사를 위해 진행 시작
                    if (hasAttackAnimation) {
                        attackAnimationInProgress = true;
                    } else if (isRanged) {
                        attackAnimationInProgress = true;
                    }
                }
                
                // 원거리 몬스터는 투사체 생성
                createProjectileIfNeeded(targetX, targetY);
            }
            // 이동 범위 내에 있는 경우
            else if (distance <= moveRange) { 
                // 이동 상태로 전환
                if (currentState != MOVE) {
                    currentState = MOVE;
                    spriteNum = 0; 
                }
                // 고정 모드가 아니면 플레이어를 향해 이동
                if (!isFixed) {
                    this.x += (dx / distance) * speed; 
                    this.y += (dy / distance) * speed;
                }
            }
            // 범위 밖에 있는 경우
            else {
                // 대기 상태로 전환
                if (currentState != IDLE) {
                    currentState = IDLE;
                    spriteNum = 0; 
                }
            }
        }

        // 애니메이션 프레임 업데이트
        int animState = currentState;
        // 현재 상태의 스프라이트가 없으면 IDLE로 대체
        if (sprites == null || sprites[animState] == null) {
            animState = IDLE;
        }

        // 애니메이션이 있는 경우 프레임 업데이트
        if (isAnimated && sprites != null && sprites[animState] != null) {
            spriteCounter++;
            // 애니메이션 속도에 따라 프레임 전환
            if (spriteCounter > ANIMATION_SPEED) {
                spriteNum++;
                // 마지막 프레임이면 처음으로 돌아감
                if (spriteNum >= sprites[animState].length) {
                    spriteNum = 0;
                    // 공격 애니메이션이 끝나면 상태 리셋
                    if (currentState == ATTACK && attackAnimationInProgress) {
                        attackAnimationInProgress = false;
                        projectileCreated = false;
                        meleeAttackApplied = false;
                    }
                }
                spriteCounter = 0;
            }
        } 
        // 공격 애니메이션이 없지만 공격 상태인 경우 (원거리 몬스터)
        else if (currentState == ATTACK && attackAnimationInProgress) {
            spriteCounter++;
            // 일정 시간 후 공격 종료
            if (spriteCounter > ANIMATION_SPEED * 2) {
                attackAnimationInProgress = false;
                projectileCreated = false;
                meleeAttackApplied = false;
                spriteCounter = 0;
            }
        }
        
        // 투사체 업데이트
        updateProjectiles();
    }
    
    // ========== 투사체 관리 ==========
    // 투사체 생성: 공격 모션 중간 프레임에서 발사 (원거리 몬스터)
    private void createProjectileIfNeeded(int targetX, int targetY) {
        // 원거리가 아니면 투사체 생성하지 않음
        if (!isRanged()) return;
        // 공격 상태가 아니면 생성하지 않음
        if (currentState != ATTACK) return;
        // 공격 애니메이션이 진행 중이 아니면 생성하지 않음
        if (!attackAnimationInProgress) return;
        // 이미 투사체를 생성했으면 생성하지 않음
        if (projectileCreated) return;
        
        // 투사체 생성 시점 확인
        boolean shouldCreate = false;
        if (sprites != null && sprites[ATTACK] != null && sprites[ATTACK].length > 0) {
            // 공격 애니메이션이 있으면 절반 프레임에서 발사
            int totalFrames = sprites[ATTACK].length;
            int projectileFrame = totalFrames / 2;
            if (spriteNum >= projectileFrame) {
                shouldCreate = true;
            }
        } else {
            // 공격 애니메이션이 없으면 즉시 발사
            shouldCreate = true;
        }
        
        // 투사체 생성
        if (shouldCreate) {
            // 적 중심 좌표 계산
            double drawY_world = this.y - (this.hitHeight - 48);
            double enemyCenterX = this.x + (this.drawWidth / 2.0);
            double enemyCenterY = drawY_world + (this.drawHeight / 2.0);
            
            // 투사체 속성 설정
            double projectileSpeed = 7.0;
            int projectileDamage = type.getAttack();
            double projectileRange = attackRange * 2.0;
            int projWidth = 16;
            int projHeight = 16;
            
            // 투사체 생성 및 리스트에 추가
            projectiles.add(new SlimeProjectile(
                enemyCenterX, enemyCenterY,
                targetX, targetY,
                projectileSpeed,
                projectileDamage,
                projectileRange,
                projWidth,
                projHeight,
                true // 원거리 투사체
            ));
            
            // 투사체 생성 플래그 설정 (한 번만 생성)
            projectileCreated = true;
        }
    }
    
    // 투사체 업데이트 및 비활성화된 투사체 제거
    private void updateProjectiles() {
        // 모든 투사체 업데이트 및 비활성화된 투사체 제거
        projectiles.removeIf(p -> {
            p.update();
            return !p.isActive();
        });
    }
    
    // 투사체 그리기
    public void drawProjectiles(Graphics2D g2, int cameraX, int cameraY) {
        // 활성화된 모든 투사체 그리기
        for (SlimeProjectile projectile : projectiles) {
            if (projectile.isActive()) {
                projectile.draw(g2, cameraX, cameraY);
            }
        }
    }
    
    // 투사체 리스트 반환 (충돌 체크용)
    public ArrayList<SlimeProjectile> getProjectiles() {
        return projectiles;
    }
    
    // ========== 공격 판정 ==========
    // 근거리 공격 판정: 공격 모션 절반 프레임에서 플레이어가 부채꼴(120도) 공격 범위 내에 있는지 체크
    public boolean canAttackPlayer(int playerX, int playerY) {
        // 원거리 몬스터는 투사체로 공격하므로 근거리 판정 불필요
        if (isRanged()) return false;
        
        // 공격 상태가 아니면 판정하지 않음
        if (currentState != ATTACK) return false;
        // 공격 애니메이션이 진행 중이 아니면 판정하지 않음
        if (!attackAnimationInProgress) return false;
        // 이미 공격 판정이 적용되었으면 판정하지 않음 (한 번만 데미지)
        if (meleeAttackApplied) return false;
        
        // 공격 모션의 절반 프레임에서만 판정
        if (!isHalfFrame()) {
            return false;
        }
        
        // 적 중심 좌표 계산
        double drawY_world = this.y - (this.hitHeight - 48);
        double enemyCenterX = this.x + (this.drawWidth / 2.0);
        double enemyCenterY = drawY_world + (this.drawHeight / 2.0);
        
        // 플레이어와의 거리 계산
        double dx = playerX - enemyCenterX;
        double dy = playerY - enemyCenterY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // 거리 체크: 공격 범위 내에 있어야 함
        if (distance > attackRange) {
            return false;
        }
        
        // 부채꼴 각도 체크 (120도 = ±60도)
        // 적이 플레이어를 향한 방향을 공격 방향으로 사용
        double attackDirection = Math.atan2(dy, dx);
        // 플레이어 방향 벡터 (정규화)
        double playerDirX = dx / distance;
        double playerDirY = dy / distance;
        // 적의 공격 방향 벡터 (플레이어를 향한 방향)
        double attackDirX = Math.cos(attackDirection);
        double attackDirY = Math.sin(attackDirection);
        // 내적을 사용하여 각도 계산 (cos 값)
        double dotProduct = playerDirX * attackDirX + playerDirY * attackDirY;
        
        // 120도 = 2π/3 라디안, cos(60도) = 0.5
        // 부채꼴 범위 내에 있는지 체크 (각도가 60도 이내면 dotProduct >= 0.5)
        boolean canAttack = dotProduct >= 0.5;
        
        // 공격 판정이 성공하면 플래그 설정 (한 번만 데미지 주기 위함)
        if (canAttack) {
            meleeAttackApplied = true;
        }
        
        return canAttack;
    }
    
    // 공격 데미지 반환
    public int getAttackDamage() {
        return type.getAttack();
    }

    // ========== 헬퍼 메서드 ==========
    // 원거리 몬스터 여부 확인
    private boolean isRanged() {
        return (type == EnemyType.GOBLIN ||
                type == EnemyType.SPORE_FLOWER || 
                type == EnemyType.FIRE_IMP || 
                type == EnemyType.SNOW_MAGE);
    }
    
    // 공격 모션 절반 프레임 여부 확인
    private boolean isHalfFrame() {
        if (sprites == null || sprites[ATTACK] == null || sprites[ATTACK].length == 0) {
            return true;
        }
        int totalFrames = sprites[ATTACK].length;
        int halfFrame = totalFrames / 2;
        return (spriteNum >= halfFrame && spriteNum <= halfFrame + 1);
    }

    // ========== 렌더링 ==========
    // 적 렌더링: 스프라이트, 히트박스, 공격 범위, HP바, 이름 표시
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        // 사망한 적은 그리지 않음
        if (!alive) return;

        // 월드 좌표를 화면 좌표로 변환
        int screenX = (int) x - cameraX;
        int screenY = (int) y - cameraY;
        
        // 현재 상태의 스프라이트 확인
        int drawState = currentState;
        if (sprites == null || sprites[drawState] == null) {
            drawState = IDLE;
        }

        // 스프라이트 반전 여부
        boolean currentFlip = flip;
        // 스프라이트 그리기 Y 좌표 계산
        int drawY = screenY - (hitHeight - 48);
        double drawY_world = this.y - (this.hitHeight - 48);
        
        // 스프라이트 그리기
        if (isAnimated && sprites != null && sprites[drawState] != null) {
            if (currentFlip) { 
                // 반전: 오른쪽으로 그리기 (음수 너비 사용)
                g2.drawImage(sprites[drawState][spriteNum], screenX + drawWidth, drawY, -drawWidth, drawHeight, null);
            } else { 
                // 정상: 왼쪽으로 그리기
                g2.drawImage(sprites[drawState][spriteNum], screenX, drawY, drawWidth, drawHeight, null);
            }
        } 

        // 히트박스 그리기 (빨간색 사각형)
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2.0f));

        // 스프라이트 중심 좌표 계산 (화면 좌표)
        double spriteCenterX = screenX + (drawWidth / 2.0);
        double spriteCenterY = (drawY_world - cameraY) + (drawHeight / 2.0);
        
        // 히트박스 위치 계산
        int hitBoxX = (int)(spriteCenterX - (hitWidth / 2.0));
        int hitBoxY = (int)(spriteCenterY - (hitHeight / 2.0));

        // 히트박스 그리기
        g2.drawRect(hitBoxX, hitBoxY, hitWidth, hitHeight);
        
        // 공격 범위 히트박스 표시 (공격 모션 중)
        if (currentState == ATTACK && attackAnimationInProgress) {
            // 근거리 몬스터는 공격 모션 중 항상 히트박스 표시
            boolean shouldShowHitbox = isHalfFrame();
            if (!isRanged()) {
                shouldShowHitbox = true;
            }
            
            // 히트박스 표시 시점
            if (shouldShowHitbox) {
                // 근거리 몬스터만 부채꼴 히트박스 표시
                if (!isRanged()) {
                    int attackRangeRadius = attackRange;
                    
                    // 월드 좌표로 적 중심 계산
                    double enemyCenterX_world = this.x + (this.drawWidth / 2.0);
                    double enemyCenterY_world = drawY_world + (this.drawHeight / 2.0);
                    
                    // 플레이어를 향한 방향 계산 (월드 좌표 기준)
                    double dx = lastPlayerX - enemyCenterX_world;
                    double dy = lastPlayerY - enemyCenterY_world;
                    double angle = Math.atan2(dy, dx);
                    
                    // fillArc 각도 계산 (fillArc는 0도가 3시 방향, 시계 방향 증가)
                    double angleDeg = Math.toDegrees(angle);
                    double fillArcAngle = -angleDeg;
                    double startAngleDeg = fillArcAngle - 60; // -60도부터 시작 (120도 범위)
                    int arcAngleDeg = 120; // 120도 범위
                    
                    int startAngle = (int)Math.round(startAngleDeg);
                    int arcAngle = arcAngleDeg;
                    
                    // 부채꼴 히트박스 채우기 (반투명 빨간색)
                    g2.setColor(new Color(255, 0, 0, 150));
                    g2.fillArc(
                        (int)(spriteCenterX - attackRangeRadius),
                        (int)(spriteCenterY - attackRangeRadius),
                        attackRangeRadius * 2,
                        attackRangeRadius * 2,
                        startAngle,
                        arcAngle
                    );
                    
                    // 부채꼴 히트박스 테두리 그리기 (빨간색)
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

        // HP바 그리기
        int hpBarWidth = 40; 
        int hpBarHeight = 5; 
        
        int barY = screenY - (hitHeight - 48); 
        int barX = screenX + (hitWidth / 2) - (hpBarWidth / 2); 
        
        // HP바 배경 (회색)
        g2.setColor(Color.GRAY);
        g2.fillRect(barX, barY, hpBarWidth, hpBarHeight);
        
        // 현재 HP 비율에 따른 HP바 너비 계산
        int currentHpWidth = (int)((double)hp / maxHp * hpBarWidth);
        // HP바 채우기 (초록색)
        g2.setColor(Color.GREEN);
        g2.fillRect(barX, barY, currentHpWidth, hpBarHeight);
        
        // 이름 표시
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        
        int textY = barY - 5; 
        int textX = barX + (hpBarWidth / 2) - (g2.getFontMetrics().stringWidth(name) / 2);
        
        g2.drawString(name, textX, textY);
        
        // 투사체 그리기
        drawProjectiles(g2, cameraX, cameraY);
    }
    
    // ========== 투사체 클래스 ==========
    // 투사체 클래스: 원거리 몬스터가 발사하는 투사체 관리
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
        
        // 투사체 생성자: 목표 지점을 향한 방향 벡터 계산
        public SlimeProjectile(double startX, double startY, double targetX, double targetY, 
                              double speed, int damage, double range, int hitWidth, int hitHeight, boolean isRanged) {
            // 투사체 초기 위치 및 속성 설정
            this.x = startX;
            this.y = startY;
            this.speed = speed;
            this.damage = damage;
            this.range = range;
            this.hitWidth = hitWidth;
            this.hitHeight = hitHeight;
            this.isRanged = isRanged;
            
            // 목표 지점을 향한 방향 벡터 계산
            double dx = targetX - startX;
            double dy = targetY - startY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            // 방향 벡터 정규화 및 속도 적용
            if (distance > 0) {
                this.dx = (dx / distance) * speed;
                this.dy = (dy / distance) * speed;
            } else {
                this.dx = 0;
                this.dy = 0;
            }
            
            // 이동 거리 초기화
            distanceTraveled = 0;
        }
        
        // 투사체 이동 및 사거리 체크
        public void update() {
            // 비활성화된 투사체는 업데이트하지 않음
            if (!active) return;
            
            // 투사체 이동
            x += dx;
            y += dy;
            // 이동 거리 누적
            distanceTraveled += speed;
            
            // 사거리를 초과하면 비활성화
            if (distanceTraveled >= range) {
                active = false;
            }
        }
        
        // 투사체 그리기: 원거리는 노란색, 슬라임은 초록색 네모
        public void draw(Graphics2D g2, int cameraX, int cameraY) {
            // 비활성화된 투사체는 그리지 않음
            if (!active) return;
            
            // 월드 좌표를 화면 좌표로 변환
            int screenX = (int)x - cameraX;
            int screenY = (int)y - cameraY;
            
            // 원거리 투사체는 노란색 네모로 표시
            if (isRanged) {
                g2.setColor(Color.YELLOW);
                g2.fillRect(screenX - hitWidth / 2, screenY - hitHeight / 2, hitWidth, hitHeight);
                g2.setColor(new Color(200, 150, 0)); // 어두운 노란색 테두리
                g2.drawRect(screenX - hitWidth / 2, screenY - hitHeight / 2, hitWidth, hitHeight);
            } 
            // 슬라임 투사체는 초록색 네모로 표시
            else {
                g2.setColor(Color.GREEN);
                g2.fillRect(screenX - hitWidth / 2, screenY - hitHeight / 2, hitWidth, hitHeight);
                g2.setColor(Color.WHITE);
                g2.drawRect(screenX - hitWidth / 2, screenY - hitHeight / 2, hitWidth, hitHeight);
            }
        }
        
        // 투사체 히트박스 반환 (충돌 체크용)
        public Rectangle getHitBox() {
            return new Rectangle(
                (int)(x - hitWidth / 2.0),
                (int)(y - hitHeight / 2.0),
                hitWidth,
                hitHeight
            );
        }
        
        // 투사체 비활성화
        public void deactivate() {
            active = false;
        }
        
        // 투사체 활성화 여부 확인
        public boolean isActive() {
            return active;
        }
        
        // 투사체 데미지 반환
        public int getDamage() {
            return damage;
        }
        
        // 투사체 X 좌표 반환
        public double getX() {
            return x;
        }
        
        // 투사체 Y 좌표 반환
        public double getY() {
            return y;
        }
    }
}
