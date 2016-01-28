package kernel;

/**
 * Created by BaronVonBaerenstein on 12/3/2015.
 */
public class Sharpen extends AlterRGB {

    // Spatial filtering constants
    private static final int KERNEL_RADIUS = 1;

    // Sharpen kernel constants
    private static final int[] NEGATIVE_INDICES = { 1, 3, 5, 7 };
    private static final int CENTER_INDEX = 4;

    // Other constants
    private static final int COLOR_MIN = 0;
    private static final int COLOR_MAX = 255;

    /**
     * This comes from an arbitrary choice of weighting in the derivation of the kernel.
     */
    private static final int MAGIC_NUMBER = 4;

    /**
     * Center of the kernel.
     */
    private int A;

    /**
     * The center value of the kernel matrix.
     */
    private int centerMultiplier;

    public Sharpen(int A) {
        this.A = A;
        this.centerMultiplier = A + MAGIC_NUMBER;
    }

    @Override
    protected int getKernelRadius() {
        return KERNEL_RADIUS;
    }

    @Override
    protected int[] alterPixelRGB(int[] reds, int[] greens, int[] blues, int[] data) {

        if (data == null || data.length < 3) {
            data = new int[3];
        }

        data[0] = this.sharpenColor(reds);
        data[1] = this.sharpenColor(greens);
        data[2] = this.sharpenColor(blues);

        return data;
        //return this.getClosestColor(color, reds, greens, blues);
    }

    /**
     * Calculate the sharpening for a single color (i.e. R, G, or B).
     *
     * @param colorList = a list of the R, G, or B color values inside the kernel bounds.
     * @return the resulting color.
     */
    private int sharpenColor(int[] colorList) {

        int value = 0;
        for (int i = 0; i < NEGATIVE_INDICES.length; ++i) {
            value += (-1 * colorList[NEGATIVE_INDICES[i]]);
        }
        value += this.centerMultiplier * colorList[CENTER_INDEX];
        value /= this.A;

        // TODO Figure out why this is wrong/how to aggregate sharpened colors
        value = this.roundColorValue(value);

        return value;
    }

    /**
     * Rounds a color value to within the 0-255 RGB bounds, if necessary.
     * If already in bounds, simply returns the value unchanged.
     *
     * @param value = the value to round.
     * @return the value rounded, if out of bounds, otherwise unchanged.
     */
    private int roundColorValue(int value) {

        if (value < COLOR_MIN) {
            value = COLOR_MIN;
        }
        else if (value > COLOR_MAX) {
            value = COLOR_MAX;
        }

        return value;
    }
}
