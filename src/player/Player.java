package player;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;  // 민정 추가
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;  // 민정 추가
//import java.util.Random;     // 민정 추가
import java.util.Iterator;  // 민정 추가

import common.Constants;
import common.Entity;
import main.GamePanel;
import item.Weapon;  // 민정 추가
import item.WeaponType;  //민정 추가

public class Player extends Entity {

    GamePanel gp;
    KeyHandler keyH;
    
    BufferedImage[][] animations;
    int totalFrames = 4;
    
    // [민정 추가] 아이템 획득 애니메이션용 변수
    public boolean obtainingItem = false; // 현재 아이템 자랑 중인가?
    private int obtainCounter = 0;        // 시간(타이머) 체크용
    private BufferedImage obtainedImage;  // 머리 위에 띄울 이미지
    
    // 민정 추가 : 아이템 슬롯 관련
    public int redPotionCount = 0;      // 빨간 물약 개수
    public int elixirCount = 0;         // 엘릭서 개수
    public int ghostCloakCount = 0;     // 유령 망토 개수
    public int selectedItemIndex = 0;   // 0: 1번 슬롯, 1: 2번 슬롯, 2: 3번 슬롯(유령 망토)
    
    // 민정 추가
    public int currentKeyCount = 0; // 현재 획득한 열쇠 개수
    
    // [민정 추가] 무기 인벤토리 및 현재 무기 번호
    public ArrayList<Weapon> inventory = new ArrayList<>();
    public int currentWeaponIndex = 0;
    
    // [민정 추가] 화면 중앙 좌표 (플레이어 그릴 위치)
    public final int screenX;
    public final int screenY;
    
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
    
    // 🔹 유령 망토 무적 시간 (5초 = 300프레임, 60 FPS 기준)
    private int ghostCloakTimer = 0;
    private static final int GHOST_CLOAK_DURATION = 300; // 5초

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        
        // [민정 추가] 화면 정중앙 좌표 계산 (화면크기 / 2 - 타일크기 / 2)
        screenX = Constants.WINDOW_WIDTH / 2 - (Constants.TILE_SIZE / 2);
        screenY = Constants.WINDOW_HEIGHT / 2 - (Constants.TILE_SIZE / 2);
        
        setDefaultValues();
        getPlayerImage();
    }

    // [민정 수정]
    public void setDefaultValues() {
        x = Constants.TILE_SIZE * 5;
        y = Constants.TILE_SIZE * 5;
        speed = (int)baseSpeed;
        direction = "down";
        spriteNum = 0;

        // [민정 추가] 게임 시작 시 Dagger와 Pistol 지급
        inventory.clear();
        inventory.add(new Weapon(WeaponType.DAGGER)); // 0번: 단검 (기본)
        inventory.add(new Weapon(WeaponType.PISTOL)); // 1번: 권총
        
        currentWeaponIndex = 0; // 처음엔 0번(단검) 들기
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
    	// [민정 추가] 아이템 획득 중이면 움직임을 멈추고 타이머만 돌림
        if (obtainingItem) {
            obtainCounter++;
            // 🔹 무기 줍는 애니메이션 시간 증가 (기존 120 → 180, 약 3초)
            if (obtainCounter > 180) { 
                obtainingItem = false;
                obtainCounter = 0;
            }
            return; // 아래의 이동 코드를 실행하지 않고 여기서 끝냄
        }
        
        // 아이템 충돌 체크는 GamePanel.checkItemPickups()에서 처리

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

        // [김민정님 코드] 무적 시간 관리
        if (invincible == true) {
            // 🔹 유령 망토 무적 시간이 남아있으면 유지
            if (ghostCloakTimer > 0) {
                ghostCloakTimer--;
                if (ghostCloakTimer <= 0) {
                    invincible = false;
                    System.out.println("👻 유령 망토 무적 종료");
                }
            } else {
                // 일반 피격 무적 (약 0.33초)
                invincibleCounter++;
                if (invincibleCounter > 20) { // 60프레임 = 약 1초
                    invincible = false;
                    invincibleCounter = 0;
                }
            }
        }
        
        // [민정 추가] 아이템 슬롯 변경 로직
        if (keyH.onePressed) {
            selectedItemIndex = 0; // 1번 키 누르면 -> 0번 인덱스(빨간물약) 선택
        }
        else if (keyH.twoPressed) {
            selectedItemIndex = 1; // 2번 키 누르면 -> 1번 인덱스(엘릭서) 선택
        }
        else if (keyH.threePressed) {
            selectedItemIndex = 2; // 3번 키 누르면 -> 2번 인덱스(유령 망토) 선택
        }
        
        checkNextStage(); // [민정 추가] : 스테이지 이동 (F키)
        checkWeaponPickUp(); // [민정 추가] : 무기 줍기 (G키)
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        
        // [민정 추가] 아이템 획득 애니메이션
        if (obtainingItem) {
            // 1. 플레이어는 정면 모습
            if (animations != null && animations.length > 0) {
                image = animations[0][0]; 
            }
            
            // 2. ★ 핵심 수정 ★ 
            // 복잡한 계산(screenX, cameraX) 다 필요 없습니다.
            // 아래 걷는 코드와 똑같이 그냥 'x', 'y'를 쓰면 됩니다!
            
            // 3. 플레이어 그리기
            if (image != null) {
                g2.drawImage(image, (int)x, (int)y, Constants.TILE_SIZE, Constants.TILE_SIZE, null);
            }
            
            // 4. 머리 위에 획득한 아이템 그리기 (y 좌표만 조금 위로)
            if (obtainedImage != null) {
                g2.drawImage(obtainedImage, (int)x, (int)y - 48, Constants.TILE_SIZE, Constants.TILE_SIZE, null);
            }
            
            return; // 여기서 함수 끝!
        }

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

    // [특수 효과] 유령 망토 (GHOST_CLOAK) 사용 시 무적 상태 활성화
    public void activateGhostCloak() {
        // 🔹 유령 망토 무적 5초 활성화
        this.invincible = true;
        this.ghostCloakTimer = GHOST_CLOAK_DURATION; // 5초 (300프레임)
        this.invincibleCounter = 0;
        System.out.println("👻 유령 망토 무적 발동! (5초간 무적)");
    }
    
    // [민정 추가] 무기 교체 (Q키 누르면 호출)
    public void swapWeapon() {
        if (inventory.isEmpty()) return;

        currentWeaponIndex++;
        // 마지막 무기를 넘어가면 다시 0번(처음)으로 돌아옴
        if (currentWeaponIndex >= inventory.size()) {
            currentWeaponIndex = 0;
        }
        System.out.println("무기 교체! 현재 무기: " + inventory.get(currentWeaponIndex).getName());

        // GamePanel 쪽 currentWeapon(WeaponType)도 함께 동기화
        if (gp != null) {
            gp.syncCurrentWeaponFromPlayer();
        }
    }

    // [민정 추가] 현재 들고 있는 무기 가져오기 (공격할 때 사용)
    public Weapon getCurrentWeapon() {
        if (inventory.isEmpty()) return null;
        return inventory.get(currentWeaponIndex);
    }
    
    // [추가] 현재 들고 있지 않은 '다음 무기'를 찾는 함수
    public Weapon getNextWeapon() {
        // 1. 무기가 1개 이하(없거나 하나뿐)라면 '다음 무기'는 없음
        if (inventory.size() <= 1) return null;

        // 2. 다음 무기의 번호 계산 (현재 번호 + 1)
        int nextIndex = currentWeaponIndex + 1;
        
        // 3. 마지막 번호를 넘어가면 다시 0번으로 (순환)
        if (nextIndex >= inventory.size()) {
            nextIndex = 0;
        }
        // 4. 해당 번호의 무기를 리턴
        return inventory.get(nextIndex);
    }
    
    // [추가] X 타일 위에서 F키 입력 시 스테이지 이동 시도
    public void checkNextStage() {
        int centerX = x + common.Constants.TILE_SIZE / 2;
        int centerY = y + common.Constants.TILE_SIZE / 2;

        char currentTile = gp.getTileChar(centerX, centerY); 

        // X 타일 위에서 F키를 눌렀는지 확인
        if (currentTile == 'X' && gp.keyH.fPressed) {

            int currentStage = map.MapLoader.getCurrentStage();
            int needed = map.StageInfo.getRequiredKeyCount(currentStage);

            // 열쇠 개수 확인
            if (currentKeyCount >= needed) {
                System.out.println("스테이지 클리어! (필요 열쇠: " + needed + ")");

                gp.soundManager.playSE(14); // 철컥 소리
                gp.soundManager.playSE(11); // 클리어 소리

                gp.nextStage(); // 다음 스테이지로 이동

                gp.keyH.fPressed = false; // 중복 입력 방지
            } else {
                System.out.println("열쇠가 부족합니다! (현재: " + currentKeyCount + " / 필요: " + needed + ")");
            }
        }
    }
    
    // 상자 열기 (테스트용 등)
    public void openChest() {
        java.util.Random random = new java.util.Random();
        if (random.nextInt(10) < 6) { 
            WeaponType[] types = WeaponType.values();
            WeaponType type = types[random.nextInt(types.length)];
            dropWeaponOnGround(new Weapon(type), this.x, this.y + 40); 
        }
    }

    public void dropWeaponOnGround(Weapon w, int dropX, int dropY) {
        // [확인됨] Weapon의 worldX, worldY는 public임
        w.worldX = dropX;
        w.worldY = dropY;
        
        // [확인됨] GamePanel의 groundWeapons는 public임
        if (gp.groundWeapons != null) {
             gp.groundWeapons.add(w);
        }
    }

    // [G키] 무기 줍기
    public void checkWeaponPickUp() {
        // [확인됨] KeyHandler에 gPressed 있음
        if (gp.keyH.gPressed) { 
            for (int i = 0; i < gp.groundWeapons.size(); i++) {
                Weapon w = gp.groundWeapons.get(i);
                
                double dist = Math.sqrt(Math.pow(x - w.worldX, 2) + Math.pow(y - w.worldY, 2));
                
                if (dist < 50) { 
                    swapWeapon(w); 
                    gp.keyH.gPressed = false; // 중복 방지
                    break; 
                }
            }
        }
    }

    // 인벤토리 슬롯 관리 및 무기 교체 로직
    public void swapWeapon(Weapon newWeapon) {
        Weapon droppedWeapon = null;
        int slotIndex = 0;

        // [확인됨] WeaponType에 isRanged() 있음
        if (newWeapon.getType().isRanged()) {
            slotIndex = 1; // 원거리 무기는 1번 슬롯 (2번째 칸)
        } else {
            slotIndex = 0; // 근거리 무기는 0번 슬롯 (1번째 칸)
        }

        // 인벤토리 크기 확보
        while (inventory.size() <= slotIndex) {
            inventory.add(null);
        }

        // 기존 슬롯에 무기가 있었다면 빼두기
        if (inventory.size() > slotIndex) droppedWeapon = inventory.get(slotIndex);
        
        // 새 무기 장착
        if (inventory.size() > slotIndex) inventory.set(slotIndex, newWeapon);
        else inventory.add(newWeapon);
        
        // 바닥에 있는 무기 리스트에서 삭제
        gp.groundWeapons.remove(newWeapon); 
        
        // 기존 무기가 있었다면 바닥에 떨구기
        if (droppedWeapon != null) {
            dropWeaponOnGround(droppedWeapon, this.x, this.y); 
        } 
        System.out.println("무기 획득: " + newWeapon.getName());

        // 새 무기 장착 후에도 GamePanel 쪽 무기 타입과 동기화
        if (gp != null) {
            gp.syncCurrentWeaponFromPlayer();
        }
    }
    
    // [민정 추가] 상자가 열릴 때 호출할 함수
    public void playObtainEffect(BufferedImage itemImage) {
        this.obtainingItem = true;
        this.obtainedImage = itemImage;
        this.obtainCounter = 0;
        
//        // 획득 효과음 재생 (15번: item_get.wav)
//        gp.playSE(15); 
    }
    
    // (아이템 충돌 체크는 GamePanel.checkItemPickups()에서 처리)
}