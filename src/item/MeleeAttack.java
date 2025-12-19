package item;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.util.HashSet;
import java.util.Set;

import common.Constants;
import enemy.Boss;
import enemy.Enemy;
import player.Player;

public class MeleeAttack {
    private Player player;
    private double angle;       // 중심 방향 (라디안)
    private double range;       // 공격 반경
    private double damage;      // 공격력
    private long startTime;     // 시작 시각
    private long duration;      // 지속 시간(ms)
    private boolean active;     // 활성 상태
    private float alpha;        // 투명도
    private double swingArc;    // 휘두름 각도
    private Shape hitbox;       // 현재 프레임의 충돌 영역
    private Set<Enemy> hitEnemies = new HashSet<>();
    private Set<Boss> hitBosses = new HashSet<>();

    public MeleeAttack(Player player, double angle, double range, double damage) {
        this.player = player;
        this.angle = angle;
        this.range = range;
        this.damage = damage;
        this.startTime = System.currentTimeMillis();

        // ⚙️ 무기별 휘두름 속도 및 지속시간
        if (range <= 100) { // 단검
            duration = 150;
            swingArc = 90;
        } else if (range <= 160) { // 롱소드
            duration = 250;
            swingArc = 100;
        } else { // 기사검
            duration = 350;
            swingArc = 120;
        }

        alpha = 1.0f;
        active = true;
    }

    public boolean isActive() {
        if (!active) return false;
        if (System.currentTimeMillis() - startTime > duration) {
            active = false;
            alpha = 0f;
        }
        return active;
    }

    public double getDamage() { return damage; }

    /** 🔹 휘두름 궤적 계산 (충돌용) */
    private Shape computeSwingArc(double cameraX, double cameraY) {
        long elapsed = System.currentTimeMillis() - startTime;
        float progress = Math.min(1f, (float) elapsed / duration);
        alpha = 1.0f - progress;

        double worldX = player.x + Constants.TILE_SIZE / 2.0;
        double worldY = player.y + Constants.TILE_SIZE / 2.0;
        double baseAngleDeg = -Math.toDegrees(angle);
        double swingStart = baseAngleDeg - (swingArc / 2.0) + (swingArc * progress);
        double size = range * 2;

        return new Arc2D.Double(
            worldX - range, worldY - range,
            size, size,
            swingStart, swingArc / 2.0,
            Arc2D.PIE
        );
    }

    public void update(double cameraX, double cameraY) {
        hitbox = computeSwingArc(cameraX, cameraY);
    }

    public Shape getHitbox() { return hitbox; }

    /** ⚔️ 진짜 검을 휘두르는 듯한 이펙트 (잔광 + 중심 라인 + 충격파) */
    public void draw(Graphics2D g, double cameraX, double cameraY) {
        if (!isActive()) return;

        // 좌표 계산
        long elapsed = System.currentTimeMillis() - startTime;
        float progress = Math.min(1f, (float) elapsed / duration);
        double baseAngleDeg = -Math.toDegrees(angle);
        double swingStart = baseAngleDeg - (swingArc / 2.0) + (swingArc * progress);

        double cx = player.x + Constants.TILE_SIZE / 2.0 - cameraX;
        double cy = player.y + Constants.TILE_SIZE / 2.0 - cameraY;
        double size = range * 2;

        // ✅ 무기별 색상 지정
        Color fillColor, coreColor;
        if (range <= 100) { // 단검
            fillColor = new Color(255, 70, 70, 140);    // 붉은 잔광
            coreColor = new Color(255, 150, 150, 220);  // 중앙날
        } else if (range <= 160) { // 롱소드
            fillColor = new Color(255, 230, 120, 150);  // 황금빛 잔광
            coreColor = new Color(255, 255, 200, 240);  // 밝은 중심
        } else { // 기사검
            fillColor = new Color(120, 170, 255, 160);  // 푸른빛 잔광
            coreColor = new Color(200, 230, 255, 240);  // 밝은 중심
        }

        Composite oldComp = g.getComposite();
        Stroke oldStroke = g.getStroke();

        // 🌀 1️⃣ 휘두름 부채꼴 잔광
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.3f));
        Shape swingArcShape = new Arc2D.Double(
            cx - range, cy - range, size, size,
            swingStart, swingArc / 1.4, Arc2D.PIE
        );
        g.setColor(fillColor);
        g.fill(swingArcShape);

        // ⚡ 2️⃣ 중심 라인 (검날)
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.8f));
        double lineStart = swingStart + swingArc / 4.0;
        double x1 = cx + Math.cos(Math.toRadians(-lineStart)) * range * 0.4;
        double y1 = cy + Math.sin(Math.toRadians(-lineStart)) * range * 0.4;
        double x2 = cx + Math.cos(Math.toRadians(-lineStart)) * range;
        double y2 = cy + Math.sin(Math.toRadians(-lineStart)) * range;

        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(coreColor);
        g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);

        // 🌫️ 3️⃣ 중심의 흰색 잔광 (빛의 여운)
        g.setColor(new Color(255, 255, 255, (int)(120 * alpha)));
        g.setStroke(new BasicStroke(2f));
        g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);

        // 💥 4️⃣ 충격파 (원형 파동)
        int shockR = (int)(progress * range * 0.5);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.6f));
        g.setColor(new Color(coreColor.getRed(), coreColor.getGreen(), coreColor.getBlue(), 100));
        g.setStroke(new BasicStroke(2f));
        g.drawOval((int)(cx - shockR), (int)(cy - shockR), shockR * 2, shockR * 2);

        // 복원
        g.setComposite(oldComp);
        g.setStroke(oldStroke);
    }

    // ✅ 중복 타격 방지
    public boolean hasHit(Enemy e) { return hitEnemies.contains(e); }
    public void markHit(Enemy e) { hitEnemies.add(e); }
    public boolean hasHit(Boss boss) { return hitBosses.contains(boss); }
    public void markHit(Boss boss) { hitBosses.add(boss); }

    public void update() { }
}
