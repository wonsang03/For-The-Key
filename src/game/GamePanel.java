package game;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable, MouseMotionListener, MouseListener {

    private Thread gameThread;
    private Player player;
    private WeaponType currentWeapon = WeaponType.PISTOL;
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<ItemType> acquiredItems = new ArrayList<>();
    private ArrayList<DamageText> damageTexts = new ArrayList<>();

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private int mouseX = Constants.WINDOW_WIDTH / 2;
    private int mouseY = Constants.WINDOW_HEIGHT / 2;
    private long lastShootTime = 0;

    // 💀 적 스폰 관련
    private long lastSpawnTime = 0;
    private long spawnInterval = 3000; // 3초마다 생성
    private int maxEnemies = 5;

    public GamePanel() {
        this.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        addMouseMotionListener(this);
        addMouseListener(this);

        player = new Player(Constants.WINDOW_WIDTH / 2 - 20, Constants.WINDOW_HEIGHT / 2);

        // 초기 1마리 생성
        spawnEnemy();

        bindKey("pressed W", () -> upPressed = true);
        bindKey("released W", () -> upPressed = false);
        bindKey("pressed S", () -> downPressed = true);
        bindKey("released S", () -> downPressed = false);
        bindKey("pressed A", () -> leftPressed = true);
        bindKey("released A", () -> leftPressed = false);
        bindKey("pressed D", () -> rightPressed = true);
        bindKey("released D", () -> rightPressed = false);
        bindKey("pressed Q", () -> changeWeapon(false));
        bindKey("pressed E", () -> changeWeapon(true));
    }

    private void bindKey(String keyStroke, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyStroke), keyStroke);
        getActionMap().put(keyStroke, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        while (gameThread != null) {
            player.move(upPressed, downPressed, leftPressed, rightPressed);

            // 💀 적 스폰 로직
            long now = System.currentTimeMillis();
            if (now - lastSpawnTime > spawnInterval && enemies.size() < maxEnemies) {
                spawnEnemy();
                lastSpawnTime = now;
            }

            updateBullets();
            updateEnemies();
            checkCollisions();
            checkItemPickups();

            damageTexts.removeIf(dt -> {
                dt.update();
                return dt.isExpired();
            });

            repaint();

            try { Thread.sleep(16); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    /**
     * 🎲 랜덤 위치로 적 생성
     */
    private void spawnEnemy() {
        int margin = 100; // 가장자리로부터 거리
        int side = (int)(Math.random() * 4); // 0:상, 1:하, 2:좌, 3:우
        int x = 0, y = 0;

        switch (side) {
            case 0: // 위쪽
                x = (int)(Math.random() * Constants.WINDOW_WIDTH);
                y = margin;
                break;
            case 1: // 아래쪽
                x = (int)(Math.random() * Constants.WINDOW_WIDTH);
                y = Constants.WINDOW_HEIGHT - margin;
                break;
            case 2: // 왼쪽
                x = margin;
                y = (int)(Math.random() * Constants.WINDOW_HEIGHT);
                break;
            case 3: // 오른쪽
                x = Constants.WINDOW_WIDTH - margin;
                y = (int)(Math.random() * Constants.WINDOW_HEIGHT);
                break;
        }

        enemies.add(new Enemy(x, y));
        System.out.println("👾 적 스폰: (" + x + ", " + y + ")");
    }

    private void shoot() {
        long now = System.currentTimeMillis();
        long delay = (long)(currentWeapon.getAttackSpeed() * 1000 / (1 + player.getAttackSpeedBonus()));
        if (now - lastShootTime < delay) return;
        lastShootTime = now;

        double px = player.getX() + 20;
        double py = player.getY() + 25;
        double angle = Math.atan2(mouseY - py, mouseX - px);
        double bulletSpeed = 10;

        if (currentWeapon == WeaponType.SHOTGUN) {
            int pellets = 5;
            double spread = Math.toRadians(15);
            double start = angle - spread / 2;
            double step = spread / (pellets - 1);
            for (int i = 0; i < pellets; i++) {
                double a = start + step * i;
                bullets.add(new Bullet(px, py, a, bulletSpeed, currentWeapon.getDamage(), currentWeapon.getRange()));
            }
        } else {
            bullets.add(new Bullet(px, py, angle, bulletSpeed, currentWeapon.getDamage(), currentWeapon.getRange()));
        }
    }

    private void updateBullets() {
        bullets.removeIf(b -> { b.update(); return !b.isActive(); });
    }

    private void updateEnemies() {
        ArrayList<Enemy> dead = new ArrayList<>();
        for (Enemy e : enemies) {
            e.update();
            if (!e.isAlive()) {
                dead.add(e);
                if (Math.random() < 0.6) { // 아이템 드롭
                    ItemType drop = ItemType.getRandom();
                    items.add(new Item(e.getX(), e.getY(), drop));
                }
            }
        }
        enemies.removeAll(dead);
    }

    private void checkCollisions() {
        for (Bullet b : bullets) {
            if (!b.isActive()) continue;
            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;
                Rectangle bulletRect = new Rectangle((int)b.getX(), (int)b.getY(), 10, 10);
                Rectangle enemyRect = new Rectangle((int)e.getX(), (int)e.getY(), e.getWidth(), e.getHeight());
                if (bulletRect.intersects(enemyRect)) {
                    double dmg = currentWeapon.getDamage() * player.getAttackMultiplier();
                    e.takeDamage(dmg);
                    b.deactivate();

                    Color dmgColor = dmg >= 50 ? Color.RED : Color.YELLOW;
                    damageTexts.add(new DamageText(e.getX() + e.getWidth() / 2, e.getY() - 10,
                            String.valueOf((int)dmg), dmgColor));
                }
            }
        }
    }

    private void checkItemPickups() {
        Rectangle playerRect = new Rectangle((int)player.getX(), (int)player.getY(), 40, 40);
        for (Item item : items) {
            if (!item.isPicked() && playerRect.intersects(item.getBounds())) {
                item.pickUp();
                acquiredItems.add(item.getType());
                applyItemEffect(item.getType());
            }
        }
    }

    private void applyItemEffect(ItemType type) {
        if (type == null) return;

        if (type.getAttackBuff() != 0) player.addAttackBonus(type.getAttackBuff());
        if (type.getHpBuff() != 0) player.addMaxHP(type.getHpBuff());
        if (type.getSpeedBuff() != 0) player.addSpeedBonus(type.getSpeedBuff());
        if (type.getAttackSpeedBuff() != 0) player.addAttackSpeedBonus(type.getAttackSpeedBuff());

        if (type == ItemType.RED_POTION) player.heal(30);
        if (type == ItemType.ELIXIR) player.heal(player.getMaxHP());

        damageTexts.add(new DamageText(player.getX(), player.getY() - 20,
                "+" + type.getName(), Color.CYAN));
    }

    private void changeWeapon(boolean next) {
        currentWeapon = next ? WeaponType.next(currentWeapon) : WeaponType.previous(currentWeapon);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        player.draw(g2);
        enemies.forEach(e -> e.draw(g2));
        bullets.forEach(b -> b.draw(g2));
        items.forEach(i -> i.draw(g2));
        damageTexts.forEach(dt -> dt.draw(g2));

        drawPlayerHUD(g2);
    }

    private void drawPlayerHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(20, 20, 330, 160, 15, 15);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
        g2.drawString("❤️ HP: " + player.getHP() + " / " + player.getMaxHP(), 40, 50);

        g2.drawString("🔫 Weapon: " + currentWeapon.getName(), 40, 75);
        g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        g2.drawString(String.format(" - Damage: %.0f | Range: %.0fpx | Speed: %.2fs",
                currentWeapon.getDamage(), currentWeapon.getRange(), currentWeapon.getAttackSpeed()), 55, 95);

        g2.drawString(String.format("⚔️ ATK Multiplier: x%.2f", player.getAttackMultiplier()), 40, 120);
        g2.drawString(String.format("💨 Move Speed: %.2f", player.getMoveSpeed()), 40, 140);
        g2.drawString(String.format("⚡ Attack Speed Bonus: +%.2f", player.getAttackSpeedBonus()), 40, 160);
    }

    @Override public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override public void mousePressed(MouseEvent e) { if (SwingUtilities.isLeftMouseButton(e)) shoot(); }
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
