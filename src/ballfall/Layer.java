import java.awt.*;

public class Layer {
    public final int y;
    public final int dangerStartAngle;
    public final int dangerArc;
    public final Color pbColor;

    public Layer(int y, int dangerStartAngle, int dangerArc, Color pbColor) {
        this.y = y;
        this.dangerStartAngle = dangerStartAngle;
        this.dangerArc = dangerArc;
        this.pbColor = pbColor;
    }
}
