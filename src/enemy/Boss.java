package enemy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Boss {

    // ========== 필드 변수 ==========
    private String name;
    private int hp;
    private int maxHp;
    public double x, y;
    public int hitWidth;
    public int hitHeight;
    public int drawWidth;
    public int drawHeight;
    public boolean alive = true;
    
    // ========== 상태 관리 ==========
    // 보스 패턴 상태 (총 7개)
    public final int IDLE = 0;              // 대기
    public final int MOVE = 1;              // 이동
    public final int MELEE_ATTACK_1 = 2;    // 근거리 공격 1
    public final int MELEE_ATTACK_2 = 3;    // 근거리 공격 2
    public final int RANGED_ATTACK_1 = 4;   // 원거리 공격 1
    public final int RANGED_ATTACK_2 = 5;   // 원거리 공격 2
    public final int ULTIMATE = 6;          // 필살기
    public int currentState = IDLE;
    
    // 패턴 실행 관련
    private boolean patternInProgress = false;  // 패턴 실행 중 여부
    private int patternFrame = 0;              // 패턴 프레임 카운터 
    
    // 스프라이트 및 애니메이션
    private boolean flip = false; 
    private boolean spriteDefaultFacesLeft = true; 
    private BufferedImage[][] sprites;   
    private boolean isAnimated = false; 
    private int spriteCounter = 0;
    private int spriteNum = 0;
    private final int ANIMATION_SPEED = 9;

    // ========== 생성자 ==========
    // 보스 생성자: 기본 스탯 및 크기 설정 후 스프라이트 이미지 로드
    public Boss(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.name = "Boss";
        this.maxHp = 10000;
        this.hp = this.maxHp;
        
        // 기본 크기 설정 (필요에 따라 조정)
        this.hitWidth = 200; 
        this.hitHeight = 200; 
        this.drawWidth = 200; 
        this.drawHeight = 200;
        
        // 스프라이트 이미지 로드
        loadImage(); 
    }

    // ========== 이미지 처리 ==========
    // 이미지를 ARGB 형식으로 변환 (투명도 처리용)
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
    
    // 보스 스프라이트 이미지 로드
    private void loadImage() {
        try {
            // loadPattern("Boss.png", 0, 0, 50, 58, 70, 10, IDLE); // IDLE
            // loadPattern("Boss.png", 0, 0, 50, 58, 70, 10, MOVE); // MOVE
            // loadPattern("Boss.png", 0, 0, 50, 58, 70, 10, MELEE_ATTACK_1); // MELEE_ATTACK_1
            // loadPattern("Boss.png", 0, 0, 50, 58, 70, 10, MELEE_ATTACK_2); // MELEE_ATTACK_2
            // loadPattern("Boss.png", 0, 0, 50, 58, 70, 10, RANGED_ATTACK_1); // RANGED_ATTACK_1
            // loadPattern("Boss.png", 0, 0, 50, 58, 70, 10, RANGED_ATTACK_2); // RANGED_ATTACK_2
            // loadPattern("Boss.png", 0, 0, 50, 58, 70, 10, ULTIMATE); // ULTIMATE
            
            // isAnimated = true;
        } catch (Exception e) {
            // 이미지 로드 실패 시 무시
        }
    }

    // 스프라이트 시트에서 애니메이션 프레임 추출 (가로 방향) - 배경 제거 없음
    private void loadPattern(String fileName, int startX, int startY, int w, int h, int stride, int count, int state) throws IOException {
        try {
            BufferedImage sheet = ImageIO.read(new File("res/" + fileName));
            if (sheet == null) {
                return;
            }
            
            // 배경 제거 없이 그대로 사용
            sheet = ensureARGB(sheet);
            
            if (sprites == null) {
                sprites = new BufferedImage[7][];  // 7개 패턴 상태
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

    // 핑크/색상 배경 제거
    private BufferedImage removePinkBackground(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        
        int[] bgColors = {
            new Color(0xdd, 0x80, 0xe1).getRGB(),
            new Color(0xb8, 0xc8, 0xa8).getRGB(),
            new Color(0xFF, 0x00, 0xFF).getRGB(),
            new Color(0x00, 0xFF, 0xFF).getRGB(),
            new Color(0x1a, 0x7a, 0x3e).getRGB(),
            new Color(0x77, 0xfe, 0xb2).getRGB(),
            new Color(0x00, 0x7d, 0x00).getRGB(),
            new Color(0x00, 0x80, 0x80).getRGB(),
            new Color(0x00, 0xff, 0x00).getRGB(),
            new Color(0xd6, 0x85, 0xd0).getRGB()
        };
        
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = image.getRGB(x, y);
                boolean isBg = false;
                for (int bgColor : bgColors) {
                    if (rgb == bgColor) {
                        isBg = true;
                        break;
                    }
                }
                result.setRGB(x, y, isBg ? 0x00000000 : rgb);
            }
        }
        return result;
    }

    // 검은색 배경 제거
    private BufferedImage removeBlackBackground(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = image.getRGB(x, y);
                Color c = new Color(rgb);
                if (c.getRed() < 10 && c.getGreen() < 10 && c.getBlue() < 10) {
                    result.setRGB(x, y, 0x00000000);
                } else {
                    result.setRGB(x, y, rgb);
                }
            }
        }
        return result;
    }

    // ========== 업데이트 ==========
    // 보스 업데이트: 애니메이션 처리 및 패턴 실행
    public void update(int targetX, int targetY) {
        // 애니메이션 업데이트
        if (isAnimated && sprites != null && sprites[currentState] != null) {
            spriteCounter++;
            if (spriteCounter >= ANIMATION_SPEED) {
                spriteCounter = 0;
                spriteNum++;
                if (spriteNum >= sprites[currentState].length) {
                    spriteNum = 0;
                    // 애니메이션이 끝나면 패턴 종료 처리
                    if (patternInProgress) {
                        onPatternEnd();
                    }
                }
            }
        }
        
        // 플레이어 방향에 따라 스프라이트 뒤집기
        if (targetX < x) {
            flip = spriteDefaultFacesLeft;
        } else {
            flip = !spriteDefaultFacesLeft;
        }
        
        // 패턴별 행동 처리
        executePattern(targetX, targetY);
    }
    
    // ========== 패턴 실행 ==========
    // 패턴별 행동 처리
    private void executePattern(int targetX, int targetY) {
        switch (currentState) {
            case IDLE:
                // executeIdle(targetX, targetY);
                break;
                
            case MOVE:
                // executeMove(targetX, targetY);
                break;
                
            case MELEE_ATTACK_1:
                // executeMeleeAttack1(targetX, targetY);
                break;
                
            case MELEE_ATTACK_2:
                // executeMeleeAttack2(targetX, targetY);
                break;
                
            case RANGED_ATTACK_1:
                // executeRangedAttack1(targetX, targetY);
                break;
                
            case RANGED_ATTACK_2:
                // executeRangedAttack2(targetX, targetY);
                break;
                
            case ULTIMATE:
                // executeUltimate(targetX, targetY);
                break;
        }
        
        patternFrame++;
    }
    
    // 패턴 시작
    private void startPattern(int pattern) {
        currentState = pattern;
        patternInProgress = true;
        patternFrame = 0;
        spriteNum = 0;
        spriteCounter = 0;
    }
    
    // 패턴 종료
    private void onPatternEnd() {
        patternInProgress = false;
        patternFrame = 0;
        // 패턴 종료 후 기본 상태로 복귀 또는 다음 패턴 결정
        // currentState = IDLE;
    }
    
    // ========== 패턴별 행동 메서드 ==========
    /*
    private void executeIdle(int targetX, int targetY) {
    }
    
    private void executeMove(int targetX, int targetY) {
    }
    
    private void executeMeleeAttack1(int targetX, int targetY) {
    }
    
    private void executeMeleeAttack2(int targetX, int targetY) {
    }
    
    private void executeRangedAttack1(int targetX, int targetY) {
    }
    
    private void executeRangedAttack2(int targetX, int targetY) {
    }
    
    private void executeUltimate(int targetX, int targetY) {
    }
    */

    // ========== 렌더링 ==========
    // 보스 렌더링: 스프라이트, 히트박스, HP바 표시
    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        if (!alive) return;
        
        // 월드 좌표를 화면 좌표로 변환
        double drawY_world = this.y - (this.hitHeight - 48);
        int screenX = (int)this.x - cameraX;
        int screenY = (int)drawY_world - cameraY;
        
        // 스프라이트 그리기
        if (sprites != null && sprites[currentState] != null && sprites[currentState].length > 0) {
            BufferedImage sprite = sprites[currentState][spriteNum];
            if (sprite != null) {
                if (flip) {
                    g2.drawImage(sprite, screenX + drawWidth, screenY, -drawWidth, drawHeight, null);
                } else {
                    g2.drawImage(sprite, screenX, screenY, drawWidth, drawHeight, null);
                }
            }
        } else {
            // 스프라이트가 없으면 보라색 사각형으로 표시
            g2.setColor(new Color(128, 0, 128, 200));
            g2.fillRect(screenX, screenY, drawWidth, drawHeight);
            g2.setColor(Color.WHITE);
            g2.drawString("BOSS", screenX + drawWidth / 2 - 20, screenY + drawHeight / 2);
        }
        
        // 히트박스 표시 (빨간색 사각형)
        double spriteCenterX = this.x + (this.drawWidth / 2.0);
        double spriteCenterY = drawY_world + (this.drawHeight / 2.0);
        int hitBoxScreenX = (int)(spriteCenterX - (this.hitWidth / 2.0)) - cameraX;
        int hitBoxScreenY = (int)(spriteCenterY - (this.hitHeight / 2.0)) - cameraY;
        g2.setColor(new Color(255, 0, 0, 100));
        g2.fillRect(hitBoxScreenX, hitBoxScreenY, this.hitWidth, this.hitHeight);
        g2.setColor(Color.RED);
        g2.drawRect(hitBoxScreenX, hitBoxScreenY, this.hitWidth, this.hitHeight);
        
        // HP바 표시
        int hpBarWidth = drawWidth;
        int hpBarHeight = 10;
        int hpBarX = screenX;
        int hpBarY = screenY - 20;
        g2.setColor(Color.RED);
        g2.fillRect(hpBarX, hpBarY, hpBarWidth, hpBarHeight);
        g2.setColor(Color.GREEN);
        int currentHpWidth = (int)(hpBarWidth * ((double)hp / maxHp));
        g2.fillRect(hpBarX, hpBarY, currentHpWidth, hpBarHeight);
        g2.setColor(Color.WHITE);
        g2.drawRect(hpBarX, hpBarY, hpBarWidth, hpBarHeight);
    }

    // ========== 데미지 및 상태 관리 ==========
    // 데미지 받기
    public void takeDamage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }

    // 사망 여부 확인
    public boolean isDead() {
        return !alive || hp <= 0;
    }
}
