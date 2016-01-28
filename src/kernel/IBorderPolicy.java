package kernel;

/**
 * Created by BaronVonBaerenstein on 12/3/2015.
 */
public interface IBorderPolicy {

    /**
     * Gets the RGB color value at the given coordinates in the image.
     *
     * @param x = the x-coordinate
     * @param y = the y-coordinate
     * @param data = an array of 3+ ints to hold the data.
     *			   If null or too small, an array will be
     *			   allocated and returned. Otherwise, data
     *			   will be filled and returned.
     * @return the filled data array or a new allocated array.
     */
    int[] getPixel(int x, int y, int[] data);

    /**
     * Gets the red color value at the given coordinates in the image.
     *
     * @param x = the x-coordinate.
     * @param y = the y-coordinate.
     * @return the red color value.
     */
    //int getRed(int x, int y);

    /**
     * Gets the green color value at the given coordinates in the image.
     *
     * @param x = the x-coordinate.
     * @param y = the y-coordinate.
     * @return the green color value.
     */
    //int getGreen(int x, int y);

    /**
     * Gets the blue color value at the given coordinates in the image.
     *
     * @param x = the x-coordinate.
     * @param y = the y-coordinate.
     * @return the blue color value.
     */
    //int getBlue(int x, int y);
}
