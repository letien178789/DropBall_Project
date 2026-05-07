import java.awt.*;

public class Layer {
    public final int y;
    public final int dangerArc;
    public final Color pbColor;
    public double angle;
    public final double rotationSpeed;

    public Layer(int y, int dangerArc, Color pbColor, double angle, double rotationSpeed) {
        this.y = y;
        this.dangerArc = dangerArc;
        this.pbColor = pbColor;
        this.angle = angle;
        this.rotationSpeed = rotationSpeed;
    }
}
