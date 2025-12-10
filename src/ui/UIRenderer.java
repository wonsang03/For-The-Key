package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import common.Constants; 
import enemy.Enemy; // [추가] 적 클래스 import
import main.GamePanel;

public class UIRenderer {

    GamePanel gp;
    Font baseFont, emojiFont;
    BufferedImage titleImage, gameOverImage;
    
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
            
        } catch (Exception e) {
            e.printStackTrace();
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

    // 1. 게임 시작 화면
    public void drawTitleScreen(Graphics2D g2) {
        if (titleImage != null) {
            g2.drawImage(titleImage, 0, 0, gp.getWidth(), gp.getHeight(), null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
            
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 80F));
            String text = "For The Key";
            int x = getXforCenteredText(text, g2);
            int y = gp.getHeight() / 2 - 20; 
            
            g2.setColor(Color.GRAY);
            g2.drawString(text, x+5, y+5);
            g2.setColor(Color.WHITE);
            g2.drawString(text, x, y);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 30F));
            String text1 = "Press";
            String text2 = "to Start";
            
            int text1Len = (int)g2.getFontMetrics().getStringBounds(text1, g2).getWidth();
            int text2Len = (int)g2.getFontMetrics().getStringBounds(text2, g2).getWidth();
            int keyWidth = 140; 
            int spacing = 20;
            
            int totalWidth = text1Len + spacing + keyWidth + spacing + text2Len;
            int startX = (gp.getWidth() - totalWidth) / 2;
            int BaseY = gp.getHeight() / 2 + 100;
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
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(20, 20, 330, 180, 15, 15);
        g2.setColor(Color.WHITE);
        g2.setFont(emojiFont);
        g2.drawString("❤️ HP: " + gp.player.getHP() + " / " + gp.player.getMaxHP(), 40, 50);
        g2.drawString("🔫 Weapon: " + gp.getCurrentWeapon().getName(), 40, 75);
        g2.setFont(baseFont.deriveFont(Font.PLAIN, 14f));
        g2.drawString(String.format(" - Damage: %.0f | Range: %.0fpx", 
                gp.getCurrentWeapon().getDamage(), gp.getCurrentWeapon().getRange()), 55, 95);
        g2.setFont(emojiFont);
        g2.drawString(String.format("⚔️ ATK Multi: x%.2f", gp.player.getAttackMultiplier()), 40, 120);
        g2.drawString(String.format("💨 Speed: %.2f", gp.player.getMoveSpeed()), 40, 140);
        
        g2.setFont(baseFont.deriveFont(Font.PLAIN, 14f));
        g2.drawString("[1, 2, 3]: 아이템 변경 [E]: 사용 [Q]: 무기 교체", 40, 180);
        if (gp.getCurrentRoom() != null) {
            g2.drawString("Room: " + gp.getCurrentRoom().getRoomId(), 40, 200);
        }
        int slotSize = 60;   
        int slotSpacing = 15; 
        int startX = 20;     
        int startY = gp.getHeight() - slotSize - 20; 
        for (int i = 0; i < 3; i++) {
            int currentX = startX + (slotSize + slotSpacing) * i;
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(currentX, startY, slotSize, slotSize, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setStroke(new java.awt.BasicStroke(2));
            g2.drawRoundRect(currentX, startY, slotSize, slotSize, 10, 10);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.setColor(Color.WHITE);
            g2.drawString(String.valueOf(i + 1), currentX + 8, startY + 20);
        }
        drawMinimap(g2);
    }

    // 3. 게임 오버 화면
    public void drawGameOverScreen(Graphics2D g2) {
        if (gameOverImage != null) {
            g2.drawImage(gameOverImage, 0, 0, gp.getWidth(), gp.getHeight(), null);
        } else {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, gp.getWidth(), gp.getHeight());
            g2.setFont(new Font("Arial", Font.BOLD, 90)); 
            String text = "GAME OVER";
            int x = getXforCenteredText(text, g2);
            int y = gp.getHeight() / 2;
            g2.setColor(Color.BLACK);
            g2.drawString(text, x + 7, y + 7); 
            g2.setColor(Color.RED); 
            g2.drawString(text, x, y);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 30F));
            String text1 = "Press";
            String text2 = "to Retry";
            
            int text1Len = (int)g2.getFontMetrics().getStringBounds(text1, g2).getWidth();
            int text2Len = (int)g2.getFontMetrics().getStringBounds(text2, g2).getWidth();
            int keyWidth = 60; 
            int spacing = 20;
            
            int totalWidth = text1Len + spacing + keyWidth + spacing + text2Len;
            int startX = (gp.getWidth() - totalWidth) / 2;
            int BaseY = gp.getHeight() / 2 + 100;
            g2.setColor(Color.WHITE);
            g2.drawString(text1, startX, BaseY);
            g2.drawString(text2, startX + text1Len + spacing + keyWidth + spacing, BaseY);
            if (blinkCounter % 60 < 40) {
                drawKeyButton(g2, "R", startX + text1Len + spacing, BaseY - 35, keyWidth, 50);
            }
        }
    }

    // 미니맵
    public void drawMinimap(Graphics2D g2) {
        if (gp.getCurrentRoom() == null) return;
        char[][] map = gp.getCurrentRoom().getMap();
        int col = map[0].length;
        int row = map.length;
        
        // 미니맵 크기
        int scale = 8; 
        
        int mapW = col * scale;
        int mapH = row * scale;
        
        int x = gp.getWidth() - mapW - 20;
        int y = 20;
        // 배경
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(x, y, mapW, mapH);
        
        // 테두리
        g2.setColor(Color.WHITE);
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawRect(x, y, mapW, mapH);
        // 맵 구조 (벽, 문)
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < col; c++) {
                char tile = map[r][c];
                if (tile == 'W' || tile == '#') { 
                    g2.setColor(Color.GRAY);
                    g2.fillRect(x + c * scale, y + r * scale, scale, scale);
                } else if (tile == 'D') {
                    g2.setColor(Color.YELLOW);  // 문 표시 (노란색)
                    g2.fillRect(x + c * scale, y + r * scale, scale, scale);
                }
            }
        }
        // 적 표시 (빨간색 점)
        if (gp.enemies != null) {
            for (int i = 0; i < gp.enemies.size(); i++) {
                Enemy e = gp.enemies.get(i);
                // 적이 살아있을 때만 표시 (isDead() 메소드가 있다고 가정)
                if (e != null && !e.isDead()) {
                    double eCol = e.x / Constants.TILE_SIZE;
                    double eRow = e.y / Constants.TILE_SIZE;
                    
                    g2.setColor(Color.RED);
                    // 적 점 크기는 조금 작게 (scale)
                    g2.fillOval(x + (int)(eCol * scale), y + (int)(eRow * scale), scale, scale);
                }
            }
        }
        // 플레이어 표시 (초록색 점)
        double playerCol = gp.player.x / Constants.TILE_SIZE;
        double playerRow = gp.player.y / Constants.TILE_SIZE;
        
        g2.setColor(Color.GREEN);
        int dotSize = scale + 4; 
        g2.fillOval(x + (int)(playerCol * scale) - 2, y + (int)(playerRow * scale) - 2, dotSize, dotSize);
    }

    public void drawKeyButton(Graphics2D g2, String keyName, int x, int y, int width, int height) {
        int thickness = 15; 
        int cornerRadius = 20;
        g2.setColor(new Color(30, 30, 30)); 
        g2.fillRoundRect(x, y + thickness, width, height, cornerRadius, cornerRadius);
        g2.setColor(Color.WHITE); 
        g2.fillRoundRect(x, y, width, height, cornerRadius, cornerRadius);
        g2.setColor(Color.BLACK);
        g2.setStroke(new java.awt.BasicStroke(4)); 
        g2.drawRoundRect(x, y, width, height, cornerRadius, cornerRadius);
        g2.setColor(Color.BLACK); 
        g2.setFont(new Font("Arial", Font.BOLD, 24)); 
        
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (width - fm.stringWidth(keyName)) / 2;
        int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();
        
        g2.drawString(keyName, textX, textY);
    }

    public int getXforCenteredText(String text, Graphics2D g2) {
        int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return (gp.getWidth()/2) - (length/2);
    }
}
