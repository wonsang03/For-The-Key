package enemy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

// [서상원님 코드] 보스 시스템: 패턴 공격, 대시, 투사체
public class Boss {

    private int hp;
    private int maxHp;
    public double x, y;
    public int hitWidth;
    public int hitHeight;
    public int drawWidth;
    public int drawHeight;
    public boolean alive = true;
    
    private int attackRange = 200;
    private int attackDamage = 50;
    
    public final int IDLE = 0;
    public final int MOVE = 1;
    public final int MELEE_ATTACK_1 = 2;
    public final int MELEE_ATTACK_2 = 3;
    public final int ULTIMATE = 7;
    public int currentState = IDLE;
    
    private boolean patternInProgress = false;
    private boolean meleeAttackApplied = false;
    private boolean meleeAttackFrame3Applied = false;
    private boolean meleeAttackFrame6Applied = false;
    
    private int attackCooldown = 0;
    private final int ATTACK_COOLDOWN_TIME = 90;
    private int lastAttackPattern = -1;
    
    private int patternTimer = 0;
    private final int DASH_PATTERN_INTERVAL = 90;
    private final int PROJECTILE_PATTERN_INTERVAL = 300;
    private int timeSinceLastProjectile = 0;
    
    private boolean flip = false; 
    private boolean spriteDefaultFacesLeft = true; 
    private BufferedImage[][] sprites;   
    private boolean isAnimated = false; 
    private int spriteCounter = 0;
    private int spriteNum = 0;
    private final int ANIMATION_SPEED = 9;
    private final int DASH_SLOW_ANIMATION_SPEED = 18;
    
    private int[][][] frameSizes;
    
    private double[][] frameMovement;
    
    private boolean isDashingAngle = false;
    private int dashCount = 0;
    private double dashDirectionX = 0.0;
    private double dashDirectionY = 0.0;
    private boolean waitingForSecondDash = false;
    private int dashDelayCounter = 0;
    private final int DASH_DELAY = 30;
    
    private double totalDashDistance = 0.0;
    private double dashProgress = 0.0;
    private double dashStartX = 0.0;
    private double dashStartY = 0.0;
    
    private double movementScaleFactor = 1.5;
    
    private static final int DASH_FRAME_WIDTH_1 = 105;
    private static final int DASH_FRAME_WIDTH_2 = 80;
    private static final int Y_OFFSET = 48;
    
    private ArrayList<BossProjectile> projectiles = new ArrayList<>();
    private boolean projectileCreated = false;
    private BufferedImage projectileSprite = null;
    private int ultimatePatternTimer = 0;
    private final int ULTIMATE_PATTERN_DURATION = 90;

    // [서상원님 코드] 보스 생성자
    public Boss(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.maxHp = 10000;
        this.hp = this.maxHp;
        
        this.hitWidth = 120; 
        this.hitHeight = 120; 
        this.drawWidth = 120; 
        this.drawHeight = 120;
        
        this.attackRange = 200;
        this.spriteDefaultFacesLeft = false;
        
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
            int[] xCoords = {10, 145, 310, 465};
            int[] yCoords = {340, 340, 340, 340};
            int[] widths = {40, 105, 80, 35};
            int[] heights = {30, 30, 30, 30};
            loadAnimationFromPoints("Boss.png", xCoords, yCoords, widths, heights, MELEE_ATTACK_2);
            loadPattern("Boss.png", 15, 20, 40, 30, 128, 5, IDLE);
            loadPattern("Boss.png", 15, 148, 40, 30, 128, 8, MOVE);
            loadPattern("Boss.png", 14, 275, 40, 30, 128, 12, MELEE_ATTACK_1);
            
            loadProjectileSprite("Boss.png", 940, 340, 25, 30);
            
            isAnimated = true;
        } catch (IOException e) {
        }
    }
    
    public void loadProjectileSprite(String fileName, int x, int y, int width, int height) {
        try {
            File imageFile = new File("res/" + fileName);
            BufferedImage sheet = ImageIO.read(imageFile);
            if (sheet == null || x + width > sheet.getWidth() || y + height > sheet.getHeight() || x < 0 || y < 0) {
                return;
            }
            sheet = ensureARGB(sheet);
            sheet = removePinkBackground(sheet);
            projectileSprite = sheet.getSubimage(x, y, width, height);
        } catch (Exception e) {}
    }
    
    private void loadAnimationFromPoints(String fileName, int[] xCoords, int[] yCoords,
                                         int[] widths, int[] heights, int state) throws IOException {
        try {
            File imageFile = new File("res/" + fileName);
            BufferedImage sheet = ImageIO.read(imageFile);
            if (sheet == null) {
                return;
            }
            
            sheet = ensureARGB(sheet);
            sheet = removePinkBackground(sheet);
            
            if (sprites == null) {
                sprites = new BufferedImage[8][];
                frameSizes = new int[8][][];
                frameMovement = new double[8][];
            }
            
            int frameCount = Math.min(Math.min(xCoords.length, yCoords.length), 
                                     Math.min(widths.length, heights.length));
            sprites[state] = new BufferedImage[frameCount];
            frameSizes[state] = new int[frameCount][2];
            frameMovement[state] = new double[frameCount];
            
            double totalMovement = 0.0;
            
            for (int i = 0; i < frameCount; i++) {
                int x = xCoords[i];
                int y = yCoords[i];
                int w = widths[i];
                int h = heights[i];
                
                if (x + w > sheet.getWidth() || y + h > sheet.getHeight() || x < 0 || y < 0) {
                    break;
                }
                
                sprites[state][i] = sheet.getSubimage(x, y, w, h);
                frameSizes[state][i][0] = w;
                frameSizes[state][i][1] = h;
                
                if (i == 0) {
                    frameMovement[state][i] = 0.0;
                } else {
                    int prevX = xCoords[i - 1];
                    int prevW = widths[i - 1];
                    int currentX = xCoords[i];
                    
                    double prevCenterX = prevX + prevW / 2.0;
                    double currentCenterX = currentX + w / 2.0;
                    double movement = (currentCenterX - prevCenterX) * movementScaleFactor;
                    
                    if (w == DASH_FRAME_WIDTH_1 || w == DASH_FRAME_WIDTH_2) {
                        frameMovement[state][i] = movement;
                        totalMovement += Math.abs(movement);
                    } else {
                        frameMovement[state][i] = 0.0;
                    }
                }
            }
            
            if (state == MELEE_ATTACK_2) {
                if (frameCount > 1) {
                    int firstX = xCoords[0];
                    int firstW = widths[0];
                    int lastX = xCoords[frameCount - 1];
                    int lastW = widths[frameCount - 1];
                    
                    double firstCenterX = firstX + firstW / 2.0;
                    double lastCenterX = lastX + lastW / 2.0;
                    totalDashDistance = Math.abs(lastCenterX - firstCenterX) * movementScaleFactor * 0.5;
                } else {
                    totalDashDistance = totalMovement * 0.5;
                }
            }
        } catch (Exception e) {
            return;
        }
    }
    
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
                sprites = new BufferedImage[8][];
                frameSizes = new int[8][][];
            }
            
            sprites[state] = new BufferedImage[count];
            frameSizes[state] = new int[count][2];
            
            for(int i = 0; i < count; i++) {
                int cutX = startX + (i * stride);
                if (cutX + w > sheet.getWidth() || startY + h > sheet.getHeight()) break;
                sprites[state][i] = sheet.getSubimage(cutX, startY, w, h);
                frameSizes[state][i][0] = w;
                frameSizes[state][i][1] = h;
            }
        } catch (Exception e) {
            return;
        }
    }

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

    // [서상원님 코드] 보스 업데이트: 패턴 선택, 애니메이션, 이동
    public void update(int targetX, int targetY) {
        if (!alive) return;
        
        flip = this.spriteDefaultFacesLeft ? (targetX > this.x) : (targetX <= this.x);
        if (attackCooldown > 0) attackCooldown--;
        
        patternTimer++;
        timeSinceLastProjectile++;
        
        if (waitingForSecondDash && currentState == IDLE) {
            dashDelayCounter++;
            if (dashDelayCounter >= DASH_DELAY) {
                startSecondDash(targetX, targetY);
            }
        } else if (!patternInProgress) {
            if (attackCooldown <= 0 && !waitingForSecondDash) {
                double random = Math.random();
                if (random < 0.7) {
                    int nextPattern;
                    if (lastAttackPattern == MELEE_ATTACK_1) {
                        nextPattern = MELEE_ATTACK_2;
                    } else if (lastAttackPattern == MELEE_ATTACK_2) {
                        nextPattern = MELEE_ATTACK_1;
                    } else {
                        nextPattern = MELEE_ATTACK_1;
                    }
                    startPattern(nextPattern, targetX, targetY);
                    lastAttackPattern = nextPattern;
                } else {
                    startPattern(ULTIMATE, targetX, targetY);
                    lastAttackPattern = ULTIMATE;
                }
            } else if (currentState != IDLE) {
                currentState = IDLE;
                spriteNum = 0;
                spriteCounter = 0;
            }
        }
        
        int animState = currentState;
        if (currentState == ULTIMATE) {
            animState = IDLE;
        }
        if (sprites == null || sprites[animState] == null) {
            animState = IDLE;
        }
        
        if (isAnimated && sprites != null && sprites[animState] != null) {
            if (patternInProgress && (animState == MELEE_ATTACK_1 || animState == MELEE_ATTACK_2)) {
                boolean isDashingFrame = isDashingFrame(animState, spriteNum);
                int currentAnimationSpeed = isDashingFrame ? DASH_SLOW_ANIMATION_SPEED : ANIMATION_SPEED;
                
                spriteCounter++;
                if (spriteCounter >= currentAnimationSpeed) {
                    spriteCounter = 0;
                    spriteNum++;
                    if (spriteNum >= sprites[animState].length) {
                        spriteNum = 0;
                        if (patternInProgress) {
                            onPatternEnd();
                        }
                    }
                    
                    if (patternInProgress && 
                        frameMovement != null && frameMovement[animState] != null &&
                        spriteNum < frameMovement[animState].length) {
                        
                        if (animState == MELEE_ATTACK_2) {
                            applyDashMovement(animState, spriteNum, targetX, targetY);
                        } else if (animState == MELEE_ATTACK_1) {
                            applyMeleeAttack1Movement(animState, spriteNum, targetX, targetY);
                        }
                    }
                }
            } else {
                if (patternInProgress && currentState == ULTIMATE) {
                    if (sprites[IDLE] != null) {
                        spriteCounter++;
                        if (spriteCounter >= ANIMATION_SPEED) {
                            spriteCounter = 0;
                            spriteNum = (spriteNum + 1) % sprites[IDLE].length;
                        }
                    }
                    ultimatePatternTimer++;
                    if (ultimatePatternTimer >= ULTIMATE_PATTERN_DURATION) {
                        onPatternEnd();
                        ultimatePatternTimer = 0;
                    }
                } else {
                    spriteCounter++;
                    if (spriteCounter >= ANIMATION_SPEED) {
                        spriteCounter = 0;
                        spriteNum = (spriteNum + 1) % sprites[animState].length;
                    }
                }
            }
        } else if (patternInProgress && currentState == ULTIMATE) {
            ultimatePatternTimer++;
            if (ultimatePatternTimer >= ULTIMATE_PATTERN_DURATION) {
                onPatternEnd();
                ultimatePatternTimer = 0;
            }
        }
        
        if (patternInProgress && currentState == ULTIMATE) {
            createUltimateProjectiles(targetX, targetY);
        }
        
        updateProjectiles();
    }
    
    private void startPattern(int pattern, int targetX, int targetY) {
        currentState = pattern;
        patternInProgress = true;
        spriteNum = 0;
        spriteCounter = 0;
        resetDashAngle();
        waitingForSecondDash = false;
        dashDelayCounter = 0;
        projectileCreated = false;
        ultimatePatternTimer = 0;
        meleeAttackApplied = false;
        meleeAttackFrame3Applied = false;
        meleeAttackFrame6Applied = false;
        
        if (pattern == MELEE_ATTACK_2) {
            dashCount = 1;
            dashStartX = this.x;
            dashStartY = this.y;
            dashProgress = 0.0;
            calculateAndSetDashDirection(targetX, targetY);
        }
    }
    
    
    private void onPatternEnd() {
        patternInProgress = false;
        projectileCreated = false;
        meleeAttackApplied = false;
        meleeAttackFrame3Applied = false;
        meleeAttackFrame6Applied = false;
        
        if (currentState == MELEE_ATTACK_2) {
            if (dashCount == 1) {
                waitingForSecondDash = true;
                dashDelayCounter = 0;
                resetState(IDLE);
                return;
            } else if (dashCount == 2) {
                dashCount = 0;
                waitingForSecondDash = false;
                resetState(IDLE);
                attackCooldown = ATTACK_COOLDOWN_TIME;
                return;
            }
        }
        
        if (currentState == ULTIMATE) {
            resetState(IDLE);
            attackCooldown = ATTACK_COOLDOWN_TIME * 2;
            return;
        }
        
        resetState(IDLE);
        attackCooldown = ATTACK_COOLDOWN_TIME;
    }
    
    private void resetState(int state) {
        currentState = state;
        spriteNum = 0;
        spriteCounter = 0;
        resetDashAngle();
        patternInProgress = false;
    }
    
    private boolean isDashingFrame(int animState, int frameIndex) {
        if (!patternInProgress || (animState != MELEE_ATTACK_1 && animState != MELEE_ATTACK_2)) {
            return false;
        }
        int frameWidth = getFrameWidth(animState, frameIndex);
        return frameWidth == DASH_FRAME_WIDTH_1 || frameWidth == DASH_FRAME_WIDTH_2;
    }
    
    private int getFrameWidth(int animState, int frameIndex) {
        if (frameSizes != null && frameSizes[animState] != null && 
            frameIndex < frameSizes[animState].length) {
            return frameSizes[animState][frameIndex][0];
        }
        return 0;
    }
    
    private void performDash(double movement, int targetX, int targetY) {
        BossPosition pos = calculateBossPosition();
        double dx = targetX - pos.centerX;
        double dy = targetY - pos.centerY;
        double distanceSquared = dx * dx + dy * dy;
        
        if (distanceSquared > 0) {
            double distance = Math.sqrt(distanceSquared);
            isDashingAngle = true;
            
            double dirX = dx / distance;
            double dirY = dy / distance;
            this.x += movement * dirX;
            this.y += movement * dirY;
        }
    }
    
    private BossPosition calculateBossPosition() {
        double drawY_world = this.y - (this.hitHeight - Y_OFFSET);
        double centerX = this.x + (this.drawWidth * 0.5);
        double centerY = drawY_world + (this.drawHeight * 0.5);
        return new BossPosition(centerX, centerY);
    }
    
    private void calculateAndSetDashDirection(int targetX, int targetY) {
        BossPosition pos = calculateBossPosition();
        double dx = targetX - pos.centerX;
        double dy = targetY - pos.centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            dashDirectionX = dx / distance;
            dashDirectionY = dy / distance;
            isDashingAngle = true;
        }
    }
    
    private void startSecondDash(int targetX, int targetY) {
        waitingForSecondDash = false;
        dashDelayCounter = 0;
        dashCount = 2;
        dashStartX = this.x;
        dashStartY = this.y;
        dashProgress = 0.0;
        calculateAndSetDashDirection(targetX, targetY);
        currentState = MELEE_ATTACK_2;
        patternInProgress = true;
        spriteNum = 0;
        spriteCounter = 0;
        meleeAttackApplied = false;
        meleeAttackFrame3Applied = false;
        meleeAttackFrame6Applied = false;
    }
    
    private static class BossPosition {
        final double centerX;
        final double centerY;
        
        BossPosition(double centerX, double centerY) {
            this.centerX = centerX;
            this.centerY = centerY;
        }
    }
    
    private void applyDashMovement(int animState, int frameIndex, int targetX, int targetY) {
        if (dashCount == 0) return;
        
        if (sprites != null && sprites[animState] != null && sprites[animState].length > 0) {
            int totalFrames = sprites[animState].length;
            dashProgress = (double)(spriteNum + 1) / totalFrames;
            
            double targetX_pos = dashStartX + dashDirectionX * totalDashDistance;
            double targetY_pos = dashStartY + dashDirectionY * totalDashDistance;
            
            this.x = dashStartX + (targetX_pos - dashStartX) * dashProgress;
            this.y = dashStartY + (targetY_pos - dashStartY) * dashProgress;
        }
    }
    
    private void applyMeleeAttack1Movement(int animState, int frameIndex, int targetX, int targetY) {
        boolean currentIsDashingFrame = isDashingFrame(animState, frameIndex);
        double movement = frameMovement[animState][frameIndex];
        
        if (movement != 0.0 && currentIsDashingFrame) {
            performDash(movement, targetX, targetY);
        } else if (!currentIsDashingFrame && isDashingAngle) {
            resetDashAngle();
        }
    }
    
    private void resetDashAngle() {
        isDashingAngle = false;
    }

    private void createUltimateProjectiles(int targetX, int targetY) {
        if (currentState != ULTIMATE || !patternInProgress || projectileCreated) return;
        
        BossPosition pos = calculateBossPosition();
        int projectileCount = 16;
        for (int i = 0; i < projectileCount; i++) {
            double angle = (2.0 * Math.PI * i) / projectileCount;
            double targetDistance = 1000.0;
            projectiles.add(new BossProjectile(
                pos.centerX, pos.centerY,
                pos.centerX + Math.cos(angle) * targetDistance,
                pos.centerY + Math.sin(angle) * targetDistance,
                6.0, attackDamage, attackRange * 4.0, 25, 30, projectileSprite
            ));
        }
        projectileCreated = true;
    }
    
    private void updateProjectiles() {
        projectiles.removeIf(p -> { p.update(); return !p.isActive(); });
    }
    
    public void drawProjectiles(Graphics2D g2, int cameraX, int cameraY) {
        projectiles.forEach(p -> { if (p.isActive()) p.draw(g2, cameraX, cameraY); });
    }
    
    public ArrayList<BossProjectile> getProjectiles() {
        return projectiles;
    }

    // [서상원님 코드] 보스 공격 판정
    public boolean canAttackPlayer(int playerX, int playerY) {
        if (!patternInProgress) return false;
        
        if (currentState == MELEE_ATTACK_1) {
            if (spriteNum == 1 && !meleeAttackFrame3Applied) {
                BossPosition pos = calculateBossPosition();
                double dx = playerX - pos.centerX;
                double dy = playerY - pos.centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance <= attackRange) {
                    double attackDirection = Math.atan2(dy, dx);
                    double playerDirX = dx / distance;
                    double playerDirY = dy / distance;
                    double attackDirX = Math.cos(attackDirection);
                    double attackDirY = Math.sin(attackDirection);
                    double dotProduct = playerDirX * attackDirX + playerDirY * attackDirY;
                    
                    if (dotProduct >= 0.5) {
                        meleeAttackFrame3Applied = true;
                        return true;
                    }
                }
            } else if (spriteNum == 7 && !meleeAttackFrame6Applied) {
                BossPosition pos = calculateBossPosition();
                double dx = playerX - pos.centerX;
                double dy = playerY - pos.centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance <= attackRange) {
                    double attackDirection = Math.atan2(dy, dx);
                    double playerDirX = dx / distance;
                    double playerDirY = dy / distance;
                    double attackDirX = Math.cos(attackDirection);
                    double attackDirY = Math.sin(attackDirection);
                    double dotProduct = playerDirX * attackDirX + playerDirY * attackDirY;
                    
                    if (dotProduct >= 0.5) {
                        meleeAttackFrame6Applied = true;
                        return true;
                    }
                }
            }
            return false;
        }
        
        if (currentState == MELEE_ATTACK_2) {
            if (meleeAttackApplied) return false;
            
            if (sprites == null || sprites[currentState] == null || sprites[currentState].length == 0) {
                return false;
            }
            int totalFrames = sprites[currentState].length;
            int halfFrame = totalFrames / 2;
            if (spriteNum != halfFrame) return false;
            
            BossPosition pos = calculateBossPosition();
            double dx = playerX - pos.centerX;
            double dy = playerY - pos.centerY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance > attackRange) return false;
            
            double attackDirection = Math.atan2(dy, dx);
            double playerDirX = dx / distance;
            double playerDirY = dy / distance;
            double attackDirX = Math.cos(attackDirection);
            double attackDirY = Math.sin(attackDirection);
            double dotProduct = playerDirX * attackDirX + playerDirY * attackDirY;
            
            if (dotProduct >= 0.5) {
                meleeAttackApplied = true;
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isHalfFrame() {
        if (sprites == null || sprites[currentState] == null || sprites[currentState].length == 0) {
            return false;
        }
        int totalFrames = sprites[currentState].length;
        int halfFrame = totalFrames / 2;
        return (spriteNum >= halfFrame && spriteNum <= halfFrame + 1);
    }
    
    public int getAttackDamage() {
        return attackDamage;
    }
    

    public void draw(Graphics2D g2, int cameraX, int cameraY) {
        if (!alive) return;
        
        double drawY_world = this.y - (this.hitHeight - Y_OFFSET);
        int screenX = (int)this.x - cameraX;
        int screenY = (int)drawY_world - cameraY;
        int renderState = currentState == ULTIMATE ? IDLE : currentState;
        
        if (sprites != null && sprites[renderState] != null && sprites[renderState].length > 0) {
            int originalState = currentState;
            currentState = renderState;
            drawSprite(g2, screenX, screenY, drawY_world, cameraX, cameraY);
            currentState = originalState;
        } else {
            g2.setColor(new Color(128, 0, 128, 200));
            g2.fillRect(screenX, screenY, drawWidth, drawHeight);
            g2.setColor(Color.WHITE);
            g2.drawString("BOSS", screenX + drawWidth / 2 - 20, screenY + drawHeight / 2);
        }
        
        int hpBarWidth = drawWidth;
        int hpBarHeight = 10;
        int hpBarX = screenX;
        int hpBarY = screenY - 20;
        g2.setColor(Color.RED);
        g2.fillRect(hpBarX, hpBarY, hpBarWidth, hpBarHeight);
        g2.setColor(Color.GREEN);
        g2.fillRect(hpBarX, hpBarY, (int)(hpBarWidth * ((double)hp / maxHp)), hpBarHeight);
        g2.setColor(Color.WHITE);
        g2.drawRect(hpBarX, hpBarY, hpBarWidth, hpBarHeight);
        
        drawProjectiles(g2, cameraX, cameraY);
    }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
        if (hp <= 0) alive = false;
    }

    public boolean isDead() {
        return !alive || hp <= 0;
    }
    
    private void drawSprite(Graphics2D g2, int screenX, int screenY, double drawY_world, int cameraX, int cameraY) {
        int originalWidth = drawWidth;
        int originalHeight = drawHeight;
        
        if (frameSizes != null && frameSizes[currentState] != null && spriteNum < frameSizes[currentState].length) {
            originalWidth = frameSizes[currentState][spriteNum][0];
            originalHeight = frameSizes[currentState][spriteNum][1];
        } else if (sprites[currentState][spriteNum] != null) {
            originalWidth = sprites[currentState][spriteNum].getWidth();
            originalHeight = sprites[currentState][spriteNum].getHeight();
        }
        
        double scaleRatio = (double)drawHeight / originalHeight;
        int scaledWidth = (int)(originalWidth * scaleRatio);
        int scaledHeight = drawHeight;
        
        double spriteCenterX_world = this.x + (this.drawWidth * 0.5);
        double spriteCenterY_world = drawY_world + (this.drawHeight * 0.5);
        int spriteCenterX_screen = (int)spriteCenterX_world - cameraX;
        int spriteCenterY_screen = (int)spriteCenterY_world - cameraY;
        
        if (isDashingAngle) {
            boolean shouldFlip = (dashDirectionX < 0);
            double rotationAngle = Math.atan2(dashDirectionY, dashDirectionX);
            java.awt.geom.AffineTransform oldTransform = g2.getTransform();
            g2.translate(spriteCenterX_screen, spriteCenterY_screen);
            g2.rotate(rotationAngle);
            g2.translate(-spriteCenterX_screen, -spriteCenterY_screen);
            if (shouldFlip) {
                g2.drawImage(sprites[currentState][spriteNum], screenX + drawWidth, screenY, -scaledWidth, scaledHeight, null);
            } else {
                g2.drawImage(sprites[currentState][spriteNum], screenX, screenY, scaledWidth, scaledHeight, null);
            }
            g2.setTransform(oldTransform);
        } else {
            if (flip) {
                g2.drawImage(sprites[currentState][spriteNum], screenX + drawWidth, screenY, -scaledWidth, scaledHeight, null);
            } else {
                g2.drawImage(sprites[currentState][spriteNum], screenX, screenY, scaledWidth, scaledHeight, null);
            }
        }
    }
    
    
    // [서상원님 코드] 보스 투사체 클래스
    public static class BossProjectile {
        private double x, y;
        private double dx, dy;
        private double speed;
        private int damage;
        private double distanceTraveled;
        private double range;
        private boolean active = true;
        private int hitWidth;
        private int hitHeight;
        private BufferedImage sprite;
        
        public BossProjectile(double startX, double startY, double targetX, double targetY, 
                              double speed, int damage, double range, int hitWidth, int hitHeight, 
                              BufferedImage sprite) {
            this.x = startX;
            this.y = startY;
            this.speed = speed;
            this.damage = damage;
            this.range = range;
            this.hitWidth = hitWidth;
            this.hitHeight = hitHeight;
            this.sprite = sprite;
            
            double dx = targetX - startX;
            double dy = targetY - startY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance > 0) {
                this.dx = (dx / distance) * speed;
                this.dy = (dy / distance) * speed;
            } else {
                this.dx = this.dy = 0;
            }
            distanceTraveled = 0;
        }
        
        public void update() {
            if (!active) return;
            x += dx;
            y += dy;
            distanceTraveled += speed;
            if (distanceTraveled >= range) active = false;
        }
        
        public void draw(Graphics2D g2, int cameraX, int cameraY) {
            if (!active) return;
            
            int screenX = (int)x - cameraX;
            int screenY = (int)y - cameraY;
            
            if (sprite != null) {
                double angle = Math.atan2(dy, dx);
                java.awt.geom.AffineTransform oldTransform = g2.getTransform();
                g2.translate(screenX, screenY);
                g2.rotate(angle);
                g2.translate(-hitWidth / 2, -hitHeight / 2);
                g2.drawImage(sprite, 0, 0, hitWidth, hitHeight, null);
                g2.setTransform(oldTransform);
            } else {
                g2.setColor(new Color(128, 0, 255));
                g2.fillRect(screenX - hitWidth / 2, screenY - hitHeight / 2, hitWidth, hitHeight);
                g2.setColor(new Color(200, 100, 255));
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