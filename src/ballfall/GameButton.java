import java.awt.*;

public class GameButton {
    public final Rectangle rect;
    public final String text;

    public GameButton(int x, int y, int w, int h, String text) {
        this.rect = new Rectangle(x, y, w, h);
        this.text = text;
    }

    public boolean clicked(Point p) {
        return rect.contains(p);
    }

    public void draw(Graphics2D g2) {
        g2.setColor(new Color(45, 50, 75));
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        g2.setColor(new Color(160, 180, 255));
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
        g2.setColor(new Color(240, 245, 255));
        FontMetrics fm = g2.getFontMetrics();
        int tx = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int ty = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, tx, ty);
    }
}
