package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;

import common.Constants; 
import enemy.Enemy;
import item.Item;       // [민정 추가] 아이콘 이미지 가져오기용
import item.ItemType;   // [민정 추가] 아이템 타입 확인용
import item.Weapon;  // [민정 추가] 무기 정보 가져오기
import main.GamePanel;
import map.MapLoader; // [민정 추가] 스테이지 정보 가져오기용

// [민정님 추가] UI 렌더링: 타이틀, HUD, 게임오버, 로딩 화면
public class UIRenderer {

    GamePanel gp;
    Font keyFont; // [민정 추가] keyFont 추가
    Font baseFont, emojiFont;
    BufferedImage titleImage, gameOverImage;
    
    BufferedImage keyIconImage; // [민정 추가] 열쇠 아이콘 이미지 변수 추가
    BufferedImage hpBarImage;   // [민정 추가] 체력바 이미지 변수
    BufferedImage statusIcon;
    
    public boolean showStatusDetail = false;
    
    public int iconX = 40;
    public int iconY = 65;
    public int iconSize = 40;
    
    private int blinkCounter = 0;
    
    public UIRenderer(GamePanel gp) {
        this.gp = gp;
        
        baseFont = new Font("Malgun Gothic", Font.BOLD, 16);
        emojiFont = new Font("Segoe UI Emoji", Font.BOLD, 16);
        // [민정 추가] 열쇠 표시용 폰트 (크고 잘 보이게)
        keyFont = new Font("Malgun Gothic", Font.BOLD, 28);

        try {
            InputStream is = getClass().getResourceAsStream("/ui/title.png");
            if (is != null) titleImage = ImageIO.read(is);
            
            InputStream is2 = getClass().getResourceAsStream("/ui/gameover.png");
            if (is2 != null) gameOverImage = ImageIO.read(is2);
            
            InputStream is3 = getClass().getResourceAsStream("/ui/icon_status.png");
            if (is3 != null) statusIcon = ImageIO.read(is3);
            
            // [민정 추가] 열쇠 아이콘 이미지 로드
            InputStream is4 = getClass().getResourceAsStream("/ui/key_icon.png");
            if (is4 != null) {
                keyIconImage = ImageIO.read(is4);
            }
            
            // [민정 추가] 체력바 이미지 로드
            InputStream is5 = getClass().getResourceAsStream("/ui/hp_bar.png");
            if (is5 != null) {
                hpBarImage = ImageIO.read(is5);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void checkStatusIconClick(int mouseX, int mouseY) {
        if (mouseX >= iconX && mouseX <= iconX + iconSize && 
            mouseY >= iconY && mouseY <= iconY + iconSize) {
            showStatusDetail = !showStatusDetail;
        }
    }

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        blinkCounter++;
        
        if (gp.gameState == gp.titleState) {
            drawTitleScreen(g2);
        } else if (gp.gameState == gp.playState) {
            drawPlayerHUD(g2);
        } else if (gp.gameState == gp.gameOverState) {
            drawGameOverScreen(g2);
        } else if (gp.gameState == gp.loadingState) {
            drawLoadingScreen(g2);
        }
    }

    // [민정님 추가] 타이틀 화면 그리기
    public void drawTitleScreen(Graphics2D g2) {
        int screenW = Constants.WINDOW_WIDTH;
        int screenH = Constants.WINDOW_HEIGHT;

        if (titleImage != null) {
            g2.drawImage(titleImage, 0, 0, screenW, screenH, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, screenW, screenH);

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 80F));
            String text = "For The Key";
            int x = getXforCenteredText(text, g2);
            int y = screenH / 2 - 20;

            g2.setColor(Color.GRAY);
            g2.drawString(text, x + 5, y + 5);
            g2.setColor(Color.WHITE);
            g2.drawString(text, x, y);

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 30F));
            String text1 = "Press";
            String text2 = "to Start";

            int text1Len = (int) g2.getFontMetrics().getStringBounds(text1, g2).getWidth();
            int text2Len = (int) g2.getFontMetrics().getStringBounds(text2, g2).getWidth();
            int keyWidth = 140;
            int spacing = 20;

            int totalWidth = text1Len + spacing + keyWidth + spacing + text2Len;
            int startX = (screenW - totalWidth) / 2;
            int BaseY = screenH / 2 + 100;

            g2.setColor(Color.WHITE);
            g2.drawString(text1, startX, BaseY);
            g2.drawString(text2, startX + text1Len + spacing + keyWidth + spacing, BaseY);
            
            if (blinkCounter % 60 < 40) {
                drawKeyButton(g2, "ENTER", startX + text1Len + spacing, BaseY - 35, keyWidth, 50);
            }
        }
    }

    // [민정 수정: 체력바(public void drawPlayerHUD(Graphics2D g2))] 플레이어 HUD 그리기: 체력바, 정보창, 아이템 슬롯, 미니맵
    public void drawPlayerHUD(Graphics2D g2) {
    	// 1. 위치 설정
        int barX = 45;
        int barY = 25;   
        // 2. 크기 설정 (이 숫자를 바꾸면 체력바 크기가 바뀝니다)
        int barWidth = 250;
        int barHeight = 35;  
        
        // 3. 체력바 그리기
        double hpScale = (double) gp.player.getHP() / gp.player.getMaxHP();
        if(hpScale < 0) hpScale = 0;

        // 4. 그리기 로직
        if (hpBarImage != null) {
            // 테두리 두께 설정 
            int borderLeft = 12;   
            int borderTop = 10;    
            int borderRight = 12;  
            int borderBottom = 10; 

            int innerWidth = barWidth - (borderLeft + borderRight);
            int innerHeight = barHeight - (borderTop + borderBottom);
            int currentFillWidth = (int)(innerWidth * hpScale); // 빨간색 길이 계산
            
            // 이미지 그리기 (여기서 barWidth, barHeight를 넣어줘서 크기를 강제로 맞춥니다!)
            g2.drawImage(hpBarImage, barX, barY, barWidth, barHeight, null);  
            // 빨간색 막대 그리기
            g2.setColor(new Color(255, 0, 30));
            g2.fillRoundRect(barX + borderLeft, barY + borderTop, currentFillWidth, innerHeight, 15, 15);
        } else {
            // 이미지가 없을 때를 위한 비상용 코드 (유지해도 되고 지워도 됨)
            g2.setColor(Color.GRAY);
            g2.fillRect(barX, barY, barWidth, barHeight);
        }
        
        // [민정 추가] 무기 아이콘 그리기 (Player에서 정보 가져옴)
        // Q를 누르면 바뀔 '다음 무기'를 보여줍니다.
        Weapon nextWeapon = gp.player.getNextWeapon();

        if (nextWeapon != null) {
            // 1. 위치 및 크기 설정
            int weaponX = barX + barWidth + 15; 
            int weaponY = barY - 5; 
            int size = 45; 

            // 2. 반투명 배경 상자
            g2.setColor(new Color(0, 0, 0, 100)); 
            g2.fillRoundRect(weaponX, weaponY, size, size, 10, 10);
            
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2)); 
            g2.drawRoundRect(weaponX, weaponY, size, size, 10, 10);

            // 3. 이미지 그리기
            BufferedImage img = nextWeapon.getWeaponImage();
            if (img != null) {
                g2.drawImage(img, weaponX + 5, weaponY + 5, size - 10, size - 10, null);
            } else {
                // 이미지가 없을 때 비상용 텍스트
                g2.setColor(Color.RED);
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                g2.drawString("No Img", weaponX + 5, weaponY + 25);
            }
        }

        // 상태 정보창 아이콘 (i 버튼)
        if (statusIcon != null) {
            g2.drawImage(statusIcon, iconX, iconY, iconSize, iconSize, null);
        } else {
            g2.setColor(Color.CYAN);
            g2.fillOval(iconX, iconY, iconSize, iconSize);
            g2.setColor(Color.WHITE);
            g2.drawString("i", iconX + 12, iconY + 22);
        }

        // [민정 수정] 상태 정보창 (Tab 키)
        if (showStatusDetail) {
            int boxX = 20;
            int boxY = 115;
            int boxW = 330;
            int boxH = 190;
            
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 15, 15);
            g2.setColor(new Color(255, 255, 255, 100));
            g2.drawRoundRect(boxX, boxY, boxW, boxH, 15, 15);
            
            g2.setColor(Color.WHITE);
            g2.setFont(emojiFont.deriveFont(Font.BOLD, 14f));
            
            g2.drawString("❤️ HP: " + gp.player.getHP() + " / " + gp.player.getMaxHP(), 40, boxY + 35);

            g2.setFont(emojiFont);
            String weaponName = "None";
            
            // [민정 수정] GamePanel이 아니라 Player에게서 현재 무기를 가져오기
            if (gp.player.getCurrentWeapon() != null) {
                weaponName = gp.player.getCurrentWeapon().getName();
            }
            
//            if (gp.getCurrentWeapon() != null) {
//                weaponName = gp.getCurrentWeapon().getName();
//            }  // 기존에 있던 함수
            
            g2.drawString("🔫 Weapon: " + weaponName, 40, boxY + 65);
            g2.drawString("⚔️ Attack: " + gp.player.getAttackMultiplier(), 40, boxY + 95);
            g2.drawString(String.format("💨 Speed: %.2f", gp.player.getMoveSpeed()), 40, boxY + 125);

            g2.setFont(baseFont.deriveFont(Font.PLAIN, 14f));
            g2.drawString("[1, 2, 3]: 아이템 변경 [E]: 사용 [Q]: 무기 교체", 40, boxY + 165);
        }

        drawItemSlots(g2); 
        drawKeyStatus(g2); // [민정 추가] 열쇠 상태 그리기 (화면 중앙 상단)
        drawMinimap(g2); 
    }
    
    // [민정 추가] 열쇠 획득 현황 표시
    public void drawKeyStatus(Graphics2D g2) {
        int currentStage = MapLoader.getCurrentStage();
        
        // 스테이지 1~4에서만 표시
        if (currentStage >= 1 && currentStage <= 4) {
            int acquiredKeys = gp.player.currentKeyCount;
            int requiredKeys = currentStage;
            
            // 텍스트 내용 (숫자만)
            String text = acquiredKeys + " / " + requiredKeys;
            
            g2.setFont(keyFont);
            
            // 1. 사이즈 계산 (중앙 정렬을 위해)
            int iconWidth = 10;  // 아이콘 가로 크기 (원하는 대로 조절 가능)
            int iconHeight = 33; // 아이콘 세로 크기
            int spacing = 10;    // 아이콘과 글자 사이 간격
            
            // 글자 길이 계산
            int textWidth = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
            
            // 전체 가로 길이 = 아이콘 + 간격 + 글자
            int totalWidth = iconWidth + spacing + textWidth;
            
            // 2. 시작 위치 계산 (화면 중앙)
            int startX = (Constants.WINDOW_WIDTH / 2) - (totalWidth / 2);
            int startY = 50; 
            
            // 3. 아이콘 그리기 (이미지가 로드되었을 때만)
            if (keyIconImage != null) {
                // 글자 베이스라인에 맞춰 살짝 위로 올리기 위해 y - 25 정도 조정
                g2.drawImage(keyIconImage, startX, startY - 25, iconWidth, iconHeight, null);
            } else {
                // 이미지가 없으면 임시로 노란색 사각형 그림
                g2.setColor(Color.YELLOW);
                g2.fillRect(startX, startY - 25, iconWidth, iconHeight);
            }

            // 4. 글자 그리기 (아이콘 옆에)
            int textX = startX + iconWidth + spacing;
            
            // 그림자
            g2.setColor(Color.BLACK);
            g2.drawString(text, textX + 2, startY + 2);
            
            // 글자 색상 (완료 시 초록, 진행 중일 때 금색)
            if (acquiredKeys >= requiredKeys) {
                g2.setColor(Color.GREEN);
            } else {
                g2.setColor(new Color(255, 215, 0)); // Gold
            }
            g2.drawString(text, textX, startY);
        }
    }
    
    // [민정 수정] 아이템 슬롯 그리기
    private void drawItemSlots(Graphics2D g2) {
        int slotSize = 45;       // 슬롯 크기
        int startX = 20;         // 시작 X 좌표
        int startY = Constants.WINDOW_HEIGHT - slotSize - 20; // 시작 Y 좌표

        // 1번 슬롯: 빨간 물약 (인덱스 0)
        drawSingleSlot(g2, startX, startY, slotSize, slotSize, 
                       ItemType.RED_POTION, gp.player.redPotionCount, 0, "1");

        // 2번 슬롯: 엘릭서 (인덱스 1) - 옆으로 한 칸 띄움
        drawSingleSlot(g2, startX + slotSize + 15, startY, slotSize, slotSize, 
                       ItemType.ELIXIR, gp.player.elixirCount, 1, "2");
    }

    // 슬롯 1개를 그리는 도우미 메서드
    private void drawSingleSlot(Graphics2D g2, int x, int y, int w, int h, 
                                ItemType type, int count, int slotIndex, String keyName) {
        
        // 1. 슬롯 배경 (반투명 검정)
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(x, y, w, h, 10, 10);

        // 2. 테두리 (선택된 슬롯은 노란색, 아니면 흰색)
        if (gp.player.selectedItemIndex == slotIndex) {
            g2.setColor(Color.YELLOW);
            g2.setStroke(new BasicStroke(3)); // 두껍게
        } else {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1)); // 얇게
        }
        g2.drawRoundRect(x, y, w, h, 10, 10);

        // 3. 아이템 이미지 (Item 클래스에서 가져옴)
        BufferedImage icon = Item.getIconImage(type);
        if (icon != null) {
            // 슬롯 안에 꽉 차게 그리기 (여백 5px)
            g2.drawImage(icon, x + 5, y + 5, w - 10, h - 10, null);
        }

        // 4. 아이템 개수 (우측 하단)
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        String sCount = String.valueOf(count);
        
        // 개수 글자 위치 계산 (우측 정렬)
        int strW = (int)g2.getFontMetrics().getStringBounds(sCount, g2).getWidth();
        g2.drawString(sCount, x + w - strW - 5, y + h - 5);

        // 5. 단축키 번호 (좌측 상단, 작게)
        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString(keyName, x + 4, y + 12);
    }

    // [민정님 추가] 게임오버 화면 그리기
    public void drawGameOverScreen(Graphics2D g2) {
        int screenW = Constants.WINDOW_WIDTH;
        int screenH = Constants.WINDOW_HEIGHT;

        if (gameOverImage != null) {
            g2.drawImage(gameOverImage, 0, 0, screenW, screenH, null);
        } else {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, screenW, screenH);
            
            g2.setFont(new Font("Arial", Font.BOLD, 90));
            String text = "GAME OVER";
            int x = getXforCenteredText(text, g2);
            int y = screenH / 2;

            g2.setColor(Color.BLACK);
            g2.drawString(text, x + 7, y + 7);
            g2.setColor(Color.RED);
            g2.drawString(text, x, y);

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 30F));
            String text1 = "Press";
            String text2 = "to Retry";

            int text1Len = (int) g2.getFontMetrics().getStringBounds(text1, g2).getWidth();
            int text2Len = (int) g2.getFontMetrics().getStringBounds(text2, g2).getWidth();
            int keyWidth = 60;
            int spacing = 20;

            int totalWidth = text1Len + spacing + keyWidth + spacing + text2Len;
            int startX = (screenW - totalWidth) / 2;
            int BaseY = screenH / 2 + 100;

            g2.setColor(Color.WHITE);
            g2.drawString(text1, startX, BaseY);
            g2.drawString(text2, startX + text1Len + spacing + keyWidth + spacing, BaseY);
            
            if (blinkCounter % 60 < 40) {
                drawKeyButton(g2, "R", startX + text1Len + spacing, BaseY - 35, keyWidth, 50);
            }
        }
    }

    // [민정님 추가] 미니맵 그리기
    public void drawMinimap(Graphics2D g2) {
        if (gp.getCurrentRoom() == null) return;
        
        char[][] map = gp.getCurrentRoom().getMap();
        int col = map[0].length;
        int row = map.length;

        int scale = 8;
        int mapW = col * scale;
        int mapH = row * scale;

        int x = Constants.WINDOW_WIDTH - mapW - 20;
        int y = 20;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(x, y, mapW, mapH);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, mapW, mapH);

        for (int r = 0; r < row; r++) {
            for (int c = 0; c < col; c++) {
                char tile = map[r][c];
                if (tile == 'W' || tile == '#') {
                    g2.setColor(Color.GRAY);
                    g2.fillRect(x + c * scale, y + r * scale, scale, scale);
                } else if (tile == 'D') {
                    g2.setColor(Color.YELLOW);
                    g2.fillRect(x + c * scale, y + r * scale, scale, scale);
                }
            }
        }

        if (gp.enemies != null) {
            for (Enemy e : gp.enemies) {
                if (e != null && !e.isDead()) {
                    double eCol = (double)e.x / Constants.TILE_SIZE;
                    double eRow = (double)e.y / Constants.TILE_SIZE;
                    g2.setColor(Color.RED);
                    g2.fillOval(x + (int)(eCol * scale), y + (int)(eRow * scale), scale, scale);
                }
            }
        }

        double playerCol = (double)gp.player.x / Constants.TILE_SIZE;
        double playerRow = (double)gp.player.y / Constants.TILE_SIZE;

        g2.setColor(Color.GREEN);
        int dotSize = scale + 4;
        g2.fillOval(x + (int) (playerCol * scale) - 2, y + (int) (playerRow * scale) - 2, dotSize, dotSize);
    }

    // [민정님 추가] 키 버튼 그리기
    public void drawKeyButton(Graphics2D g2, String keyName, int x, int y, int width, int height) {
        int thickness = 15;
        int cornerRadius = 20;
        g2.setColor(new Color(30, 30, 30));
        g2.fillRoundRect(x, y + thickness, width, height, cornerRadius, cornerRadius);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, width, height, cornerRadius, cornerRadius);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(x, y, width, height, cornerRadius, cornerRadius);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (width - fm.stringWidth(keyName)) / 2;
        int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(keyName, textX, textY);
    }

    // [민정님 추가] 로딩 화면 그리기
    public void drawLoadingScreen(Graphics2D g2) {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - gp.loadingStartTime;
        
        if (elapsed < gp.STAGE_NAME_DURATION) {
            drawStageName(g2);
        } else {
            long fadeElapsed = elapsed - gp.STAGE_NAME_DURATION;
            float fadeProgress = Math.min(1.0f, fadeElapsed / (float)gp.FADE_IN_DURATION);
            
            drawGameScreenForFade(g2);
            
            int alpha = (int)(255 * (1.0f - fadeProgress));
            g2.setColor(new Color(0, 0, 0, alpha));
            g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
        }
    }
    
    // [민정님 추가] 스테이지 이름 표시
    private void drawStageName(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
        
        int stageNum = map.MapLoader.getCurrentStage();
        String stageName = getStageName(stageNum);
        String stageText = "스테이지 " + stageNum;
        String nameText = "<" + stageName + ">";
        
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        
        Font pixelFont = new Font(Font.MONOSPACED, Font.BOLD, 72);
        g2.setFont(pixelFont);
        
        int stageX = getXforCenteredText(stageText, g2);
        int stageY = gp.getHeight() / 2 - 40;
        
        g2.setColor(Color.GRAY);
        g2.drawString(stageText, stageX + 5, stageY + 5);
        g2.setColor(Color.WHITE);
        g2.drawString(stageText, stageX, stageY);
        
        int nameX = getXforCenteredText(nameText, g2);
        int nameY = gp.getHeight() / 2 + 40;
        
        g2.setColor(Color.GRAY);
        g2.drawString(nameText, nameX + 5, nameY + 5);
        g2.setColor(Color.WHITE);
        g2.drawString(nameText, nameX, nameY);
        
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
    
    // [민정님 추가] 페이드인을 위한 게임 화면 그리기
    private void drawGameScreenForFade(Graphics2D g2) {
        int originalState = gp.gameState;
        gp.gameState = gp.playState;
        
        // 맵
        if (gp.currentRoom != null && gp.tileManager != null) {
            Graphics2D g2Map = (Graphics2D) g2.create();
            g2Map.translate(-(int)gp.cameraX, -(int)gp.cameraY);
            gp.tileManager.render(g2Map, gp.currentRoom.getMap());
            g2Map.dispose();
        }
        
        if (gp.enemies != null) {
            for (Enemy enemy : gp.enemies) {
                enemy.draw(g2, (int)gp.cameraX, (int)gp.cameraY);
            }
        }
        
        if (gp.boss != null && gp.boss.alive) {
            gp.boss.draw(g2, (int)gp.cameraX, (int)gp.cameraY);
        }
        
        if (gp.bullets != null) {
            for (item.Bullet bullet : gp.bullets) {
                int screenX = (int)bullet.getX() - (int)gp.cameraX;
                int screenY = (int)bullet.getY() - (int)gp.cameraY;
                if (screenX >= -10 && screenX <= Constants.WINDOW_WIDTH + 10 &&
                    screenY >= -10 && screenY <= Constants.WINDOW_HEIGHT + 10) {
                    Graphics2D g2Copy = (Graphics2D) g2.create();
                    g2Copy.translate(-(int)gp.cameraX, -(int)gp.cameraY);
                    bullet.draw(g2Copy);
                    g2Copy.dispose();
                }
            }
        }
        
        if (gp.items != null) {
            for (item.Item item : gp.items) {
                java.awt.Rectangle bounds = item.getBounds();
                int screenX = (int)bounds.getX() - (int)gp.cameraX;
                int screenY = (int)bounds.getY() - (int)gp.cameraY;
                if (screenX >= -25 && screenX <= Constants.WINDOW_WIDTH + 25 &&
                    screenY >= -25 && screenY <= Constants.WINDOW_HEIGHT + 25) {
                    Graphics2D g2Copy = (Graphics2D) g2.create();
                    g2Copy.translate(-(int)gp.cameraX, -(int)gp.cameraY);
                    item.draw(g2Copy);
                    g2Copy.dispose();
                }
            }
        }
        
        if (gp.player != null) {
            Graphics2D g2Player = (Graphics2D) g2.create();
            g2Player.translate(-(int)gp.cameraX, -(int)gp.cameraY);
            gp.player.draw(g2Player);
            g2Player.dispose();
        }
        
        if (gp.damageTexts != null) {
            for (item.DamageText dt : gp.damageTexts) {
                Graphics2D g2Copy = (Graphics2D) g2.create();
                g2Copy.translate(-(int)gp.cameraX, -(int)gp.cameraY);
                dt.draw(g2Copy);
                g2Copy.dispose();
            }
        }
        
        gp.gameState = originalState;
    }

    public int getXforCenteredText(String text, Graphics2D g2) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return (Constants.WINDOW_WIDTH / 2) - (length / 2);
    }
    
    private String getStageName(int stageNum) {
        switch (stageNum) {
            case 1: return "미아의 숲";
            case 2: return "늪지대";
            case 3: return "얼음 동굴";
            case 4: return "지옥의 전당";
            case 5: return "알현실";
            default: return "알 수 없는 스테이지";
        }
    }
}