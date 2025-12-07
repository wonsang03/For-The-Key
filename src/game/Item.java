package game;

import java.awt.*;

public class Item {
    private double x, y;
    private ItemType type;
    private boolean picked = false;

    public Item(double x, double y, ItemType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void draw(Graphics2D g2) {
        if (picked) return;
        g2.setColor(type.getRarity().getColor());
        g2.fillOval((int)x, (int)y, 25, 25);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        g2.drawString(type.getName(), (int)x - 10, (int)y - 5);
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, 25, 25);
    }

    public ItemType getType() { return type; }
    public boolean isPicked() { return picked; }
    public void pickUp() { picked = true; }
}
