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
import main.GamePanel;

// [민정님 추가] UI 렌더링: 타이틀, HUD, 게임오버, 로딩 화면
public class UIRenderer {

    GamePanel gp;
    Font baseFont, emojiFont;
    BufferedImage titleImage, gameOverImage;
    
    BufferedImage statusIcon;
    public boolean showStatusDetail = false;
    
    public int iconX = 40;
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
            
            InputStream is3 = getClass().getResourceAsStream("/ui/icon_status.png");
            if (is3 != null) statusIcon = ImageIO.read(is3);

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

    // [민정님 추가] 플레이어 HUD 그리기: 체력바, 정보창, 아이템 슬롯, 미니맵
    public void drawPlayerHUD(Graphics2D g2) {
        int barX = 40;
        int barY = 45;
        int barWidth = 200;
        int barHeight = 20;

        double hpScale = (double) gp.player.getHP() / gp.player.getMaxHP();
        if(hpScale < 0) hpScale = 0;

        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(barX, barY, barWidth, barHeight);
        g2.setColor(new Color(255, 0, 30));
        g2.fillRect(barX, barY, (int)(barWidth * hpScale), barHeight);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(barX, barY, barWidth, barHeight);

        if (statusIcon != null) {
            g2.drawImage(statusIcon, iconX, iconY, iconSize, iconSize, null);
        } else {
            g2.setColor(Color.CYAN);
            g2.fillOval(iconX, iconY, iconSize, iconSize);
            g2.setColor(Color.WHITE);
            g2.drawString("i", iconX + 12, iconY + 22);
        }

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
            if (gp.getCurrentWeapon() != null) {
                weaponName = gp.getCurrentWeapon().getName();
            }
            
            g2.drawString("🔫 Weapon: " + weaponName, 40, boxY + 65);
            g2.drawString("⚔️ Attack: " + gp.player.getAttackMultiplier(), 40, boxY + 95);
            g2.drawString(String.format("💨 Speed: %.2f", gp.player.getMoveSpeed()), 40, boxY + 125);

            g2.setFont(baseFont.deriveFont(Font.PLAIN, 14f));
            g2.drawString("[1, 2, 3]: 아이템 변경 [E]: 사용 [Q]: 무기 교체", 40, boxY + 165);
        }

        drawItemSlots(g2);
        drawMinimap(g2); 
    }
    
    // [민정님 추가] 아이템 슬롯 그리기
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
