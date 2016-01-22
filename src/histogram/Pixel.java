package histogram;

/**
 * Created by BaronVonBaerenstein on 1/22/2016.
 */
public class Pixel {
    private final int x;
    private final int y;
    private final int rgb;

    public Pixel(Pixel other) {
        this(other.getX(), other.getY(), other.getRGB());
    }

    public Pixel(int x, int y, int rgb) {
        this.x = x;
        this.y = y;
        this.rgb = rgb;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getRGB() { return rgb; }
}
