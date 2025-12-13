package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

import common.Constants;
import enemy.Enemy;
import main.GamePanel;

public class UIRenderer {

    GamePanel gp;
    Font baseFont, emojiFont;
    BufferedImage titleImage, gameOverImage;
    
    // 상태창 아이콘 및 토글 변수
    BufferedImage statusIcon;
    public boolean showStatusDetail = false; // true면 정보창 보임, false면 숨김
    
    // 아이콘 위치와 크기 (클릭 감지용)
    public int iconX = 40; // 체력바 아래
    public int iconY = 75;
    public int iconSize = 32;

    private int blinkCounter = 0;

    public UIRenderer(GamePanel gp) {
        this.gp = gp;

        baseFont = new Font("Malgun Gothic", Font.BOLD, 16);
        emojiFont = new Font("Segoe UI Emoji", Font.BOLD, 16);

        try {
            InputStream is = getClass().getResourceAsStream("/ui/title.png");
            if (is != null) titleImage = ImageIO.read(is);

            InputStream is2 = getClass().getResourceAsStream("/ui/gameover.png");
            if (is2 != null) gameOverImage = ImageIO.read(is2);
            
            // [추가] 아이콘 이미지 로드 (없으면 기본 사각형으로 대체됨)
            InputStream is3 = getClass().getResourceAsStream("/ui/icon_status.png");
            if (is3 != null) statusIcon = ImageIO.read(is3);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // [추가] 마우스 클릭 좌표를 받아 아이콘을 눌렀는지 확인하는 메소드
    public void checkStatusIconClick(int mouseX, int mouseY) {
        // 아이콘 영역 안을 클릭했는지 확인
        if (mouseX >= iconX && mouseX <= iconX + iconSize && 
            mouseY >= iconY && mouseY <= iconY + iconSize) {
            
            showStatusDetail = !showStatusDetail; // 켜져있으면 끄고, 꺼져있으면 켬 (토글)
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
        }
    }

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

    // 2. 플레이 중 HUD
    public void drawPlayerHUD(Graphics2D g2) {
        
        // 항상 보이는 것들 (체력바, 아이콘)
        
        // (1) 체력바 그리기
        int barX = 40;
        int barY = 45;
        int barWidth = 200;
        int barHeight = 20;

        double hpScale = (double) gp.player.hp / gp.player.maxHP;
        if(hpScale < 0) hpScale = 0;

        // 배경(회색)
        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(barX, barY, barWidth, barHeight);
        // 게이지(빨강)
        g2.setColor(new Color(255, 0, 30));
        g2.fillRect(barX, barY, (int)(barWidth * hpScale), barHeight);
        // 테두리(흰색)
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(barX, barY, barWidth, barHeight);

        // (2) 아이콘
        if (statusIcon != null) {
            g2.drawImage(statusIcon, iconX, iconY, iconSize, iconSize, null);
        } else {
            // 이미지가 없으면 임시로 파란 버튼 그림
            g2.setColor(Color.CYAN);
            g2.fillOval(iconX, iconY, iconSize, iconSize);
            g2.setColor(Color.WHITE);
            g2.drawString("i", iconX + 12, iconY + 22);
        }


        // 2. 정보창
        if (showStatusDetail) {
        	// 박스 위치 설정 (아이콘 끝나는 지점 107보다 아래인 115에서 시작)
            int boxX = 20;
            int boxY = 115;
            int boxW = 330;
            int boxH = 190;
        	
            // 반투명 배경 박스
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(boxX, boxY, boxW, boxH, 15, 15);
            
            // 박스 테두리 (선택 사항)
            g2.setColor(new Color(255, 255, 255, 100));
            g2.drawRoundRect(boxX, boxY, boxW, boxH, 15, 15);
            
            g2.setColor(Color.WHITE);

            // 텍스트 정보들
            g2.setFont(emojiFont.deriveFont(Font.BOLD, 14f));
            
            // HP 숫자 (바 위에 텍스트로 표시)
            g2.drawString("❤️ HP: " + gp.player.hp + " / " + gp.player.maxHP, 40, boxY + 35);

            // 무기 정보
            g2.setFont(emojiFont);
            String weaponName = "None";
            
            if (gp.player.currentWeapon != null) {
                weaponName = gp.player.currentWeapon.getName();
            }
            
            g2.drawString("🔫 Weapon: " + weaponName, 40, boxY + 65);
            
            // 공격력
            g2.drawString("⚔️ Attack: " + gp.player.attackMultiplier, 40, boxY + 95);

            // 스피드 (위치 아래로 밀림)
            g2.setFont(emojiFont);
            g2.drawString(String.format("💨 Speed: %d", gp.player.speed), 40, boxY + 125);

            g2.setFont(baseFont.deriveFont(Font.PLAIN, 14f));
            g2.drawString("[1, 2, 3]: 아이템 변경 [E]: 사용 [Q]: 무기 교체", 40, boxY + 165);
        }

        // 아이템 슬롯 및 미니맵 (항상 보임)
        drawItemSlots(g2);
        drawMinimap(g2); 
    }
    
    private void drawItemSlots(Graphics2D g2) {
        int slotSize = 60;
        int slotSpacing = 15;
        int startX = 20;
        int startY = Constants.WINDOW_HEIGHT - slotSize - 20;

        for (int i = 0; i < 3; i++) {
            int currentX = startX + (slotSize + slotSpacing) * i;
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(currentX, startY, slotSize, slotSize, 10, 10);
            
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(currentX, startY, slotSize, slotSize, 10, 10);
            
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.setColor(Color.WHITE);
            g2.drawString(String.valueOf(i + 1), currentX + 8, startY + 20);
        }
    }

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
            for (int i = 0; i < gp.enemies.size(); i++) {
                Enemy e = gp.enemies.get(i);
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

    public int getXforCenteredText(String text, Graphics2D g2) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return (Constants.WINDOW_WIDTH / 2) - (length / 2);
    }
}