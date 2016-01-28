package kernel;

import java.util.Arrays;

/**
 * Created by BaronVonBaerenstein on 12/3/2015.
 */
public class MedianBlur extends AlterRGB {

    /**
     * The radius of the kernel (kernel width and height will be (2 * kernelRadius + 1)).
     */
    private int kernelRadius;

    /**
     * Basic constructor for the MedianBlur kernel.
     *
     * @param kernelRadius = the radius of the kernel (kernel width and height will be (2 * kernelRadius + 1)).
     */
    public MedianBlur(int kernelRadius) {
        this.kernelRadius = kernelRadius;
    }

    @Override
    protected int getKernelRadius() {
        return this.kernelRadius;
    }

    @Override
    protected int[] alterPixelRGB(int[] reds, int[] greens, int[] blues, int[] data) {

        // Get the median colors and put them in a median color vector for calculating distance
        int[] median = this.getTrueMedianColor(reds, greens, blues);

        // Return the color closest to it
        this.getClosestColor(median, reds, greens, blues, data);
        return data;
    }

    /**
     * Get the median color value for red, green, and blue independently.
     *
     * @param reds = array of red color values.
     * @param greens = array of green color values.
     * @param blues = array of blue color values.
     * @return array with the median red, green and blue.
     */
    private int[] getTrueMedianColor(int[] reds, int[] greens, int[] blues) {

        int copyLength = reds.length;
        int[] redCopy = new int[copyLength];
        int[] greenCopy = new int[copyLength];
        int[] blueCopy = new int[copyLength];

        // Copy and sort the colors
        System.arraycopy(reds, 0, redCopy, 0, copyLength);
        System.arraycopy(greens, 0, greenCopy, 0, copyLength);
        System.arraycopy(blues, 0, blueCopy, 0, copyLength);

        Arrays.sort(redCopy);
        Arrays.sort(greenCopy);
        Arrays.sort(blueCopy);

        // Return the median colors
        int[] median = new int[3];
        int medianIndex = redCopy.length / 2;
        median[0] = redCopy[medianIndex];
        median[1] = greenCopy[medianIndex];
        median[2] = blueCopy[medianIndex];

        return median;
    }

}
