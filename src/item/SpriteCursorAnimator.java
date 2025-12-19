package item;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class SpriteCursorAnimator {
    private BufferedImage spriteSheet;
    private BufferedImage[] frames;
    private int frameCount;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private long frameDelay = 60; // 프레임 간격 (ms)
    private int frameWidth;
    private int frameHeight;
    private boolean loaded = false;
    private boolean playing = false;
    private boolean finished = true;

    public SpriteCursorAnimator(String path, int frameCount) {
        this.frameCount = frameCount;
        loadSprite(path);
    }

    private void loadSprite(String path) {
        try {
            spriteSheet = ImageIO.read(new File(path));
            frameWidth = spriteSheet.getWidth() / frameCount;
            frameHeight = spriteSheet.getHeight();
            frames = new BufferedImage[frameCount];

            for (int i = 0; i < frameCount; i++) {
                frames[i] = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }

            loaded = true;
            System.out.println("✅ 커서 스프라이트 로드 완료 (" + frameCount + " 프레임)");
        } catch (Exception e) {
            System.out.println("⚠️ 커서 스프라이트 로드 실패: " + e.getMessage());
            loaded = false;
        }
    }

    /** 🎬 클릭 시 호출 (애니메이션 시작) */
    public void play() {
        if (!loaded) return;
        currentFrame = 0;
        playing = true;
        finished = false;
        lastFrameTime = System.currentTimeMillis();
    }

    /** 🔄 업데이트 (paintComponent 호출될 때마다) */
    public void draw(Graphics2D g, int mouseX, int mouseY, int size) {
        if (!loaded || !playing) return;

        long now = System.currentTimeMillis();
        if (now - lastFrameTime > frameDelay) {
            currentFrame++;
            lastFrameTime = now;

            if (currentFrame >= frameCount) {
                playing = false;
                finished = true;
                return;
            }
        }

        BufferedImage frame = frames[currentFrame];
        int drawX = mouseX - size / 2;
        int drawY = mouseY - size / 2;
        g.drawImage(frame, drawX, drawY, size, size, null);
    }

    public boolean isPlaying() { return playing; }
    public boolean isFinished() { return finished; }
}
