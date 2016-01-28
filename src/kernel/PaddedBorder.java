package kernel;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by BaronVonBaerenstein on 12/3/2015.
 */
public class PaddedBorder implements IBorderPolicy {

    private static final int RGB_COLOR_SIZE = 3;

    /**
     * The color to use as padding around the image.
     */
    private int[] padColor;

    /**
     * The image being padded.
     */
    private BufferedImage image;

    /**
     * Basic constructor for a PaddedBorder.
     *
     * @param padColor = the color to use for padding.
     * @param image = the image to pad.
     */
    public PaddedBorder(int[] padColor, BufferedImage image) {
        this.padColor = padColor;
        this.image = image;
    }

    @Override
    public int[] getPixel(int x, int y, int[] data) {

        if (data == null || data.length < 3) {
            data = new int[RGB_COLOR_SIZE];
        }

        if (this.coordinatesInBounds(x, y)) {
            int rgbHash = image.getRGB(x, y);
            PaddedBorder.parseRGBHash(rgbHash, data);
        }
        else {
            System.arraycopy(this.padColor, 0, data, 0, RGB_COLOR_SIZE);
        }

        return data;
    }

    /*
    @Override
    public int getRed(int x, int y) {

        if (this.coordinatesInBounds(x, y)) {
            return image.getRed(x, y);
        }
        return this.padColor[0];
    }

    @Override
    public int getGreen(int x, int y) {

        if (this.coordinatesInBounds(x, y)) {
            return image.getGreen(x, y);
        }
        return this.padColor[1];
    }

    @Override
    public int getBlue(int x, int y) {

        if (this.coordinatesInBounds(x, y)) {
            return image.getBlue(x, y);
        }
        return this.padColor[2];
    }
    */

    /**
     * Checks if the given coordinates are within the bounds of the image.
     *
     * @param x = x-coordinate to check.
     * @param y = y-coordinate to check.
     * @return true if within bounds, otherwise false.
     */
    private boolean coordinatesInBounds(int x, int y) {

        if ( (x >= 0 && x < image.getWidth()) && (y >= 0 && y < image.getHeight()) ) {
            return true;
        }
        return false;
    }

    private static int[] parseRGBHash(int rgbHash, int[] rgb) {

        if (rgb == null || rgb.length != 3) {
            rgb = new int[3];
        }

        rgb[0] = (rgbHash >> 16) & 0xFF;
        rgb[1] = (rgbHash >> 8) & 0xFF;
        rgb[2] = rgbHash & 0xFF;

        return rgb;
    }
}
