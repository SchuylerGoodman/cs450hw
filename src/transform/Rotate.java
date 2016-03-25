package transform;

import histogram.Pixel;
import javafx.util.Pair;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Created by BaronVonBaerenstein on 3/25/2016.
 */
public class Rotate implements ITransform {

    private float radians;

    public Rotate(float radians) {

        this.radians = radians;
    }

    @Override
    public BufferedImage apply(BufferedImage input) {

        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        float vMiddle = (inputWidth - 1) / 2f;
        float wMiddle = (inputHeight - 1) / 2f;
        Raster inputRaster = input.getRaster();
        ComponentSampleModel sampleModel = (ComponentSampleModel) input.getSampleModel();
        int pixelStride = sampleModel.getPixelStride();

        // Find maximum width and height of image after rotation
        Point2D.Float[] rotatedCorners = new Point2D.Float[4];
        rotatedCorners[0] = Rotate.rotate(0, vMiddle, 0, wMiddle, this.radians);
        rotatedCorners[1] = Rotate.rotate(inputWidth - 1, vMiddle, 0, wMiddle, this.radians);
        rotatedCorners[2] = Rotate.rotate(0, vMiddle, inputHeight - 1, wMiddle, this.radians);
        rotatedCorners[3] = Rotate.rotate(inputWidth - 1, vMiddle, inputHeight - 1, wMiddle, this.radians);

        float xMax, yMax, xMin, yMin;
        xMax = yMax = Float.MIN_VALUE;
        xMin = yMin = Float.MAX_VALUE;
        for (int i = 0; i < rotatedCorners.length; ++i) {
            Point2D.Float corner = rotatedCorners[i];
            if (corner.x > xMax) xMax = corner.x;
            if (corner.x < xMin) xMin = corner.x;
            if (corner.y > yMax) yMax = corner.y;
            if (corner.y < yMin) yMin = corner.y;
        }
        int outputWidth = (int) (Math.ceil(xMax) - Math.floor(xMin));
        int outputHeight = (int) (Math.ceil(yMax) - Math.floor(yMin));

        // Create new image with that width and height
        BufferedImage output = new BufferedImage(outputWidth, outputHeight, input.getType());
        WritableRaster outputRaster = output.getRaster();
        float xMiddle = (outputWidth - 1) / 2f;
        float yMiddle = (outputHeight - 1) / 2f;

        int[] inputColors = new int[4 * pixelStride];
        int[] outputColors = new int[pixelStride];

        // For each pixel (x, y) in output image
        for (int x = 0; x < outputWidth; ++x) {
            for (int y = 0; y < outputHeight; ++y) {

                // Do inverse rotation transform of (x, y) to find location (v, w) in input image
                Point2D.Float inputCoordinates = Rotate.rotate(x, xMiddle, y, yMiddle, -1 * this.radians);
                float v = inputCoordinates.x - (xMiddle - vMiddle);
                float w = inputCoordinates.y - (yMiddle - wMiddle);

                // If (v, w) inside bounds of input image, use bilinear interpolation to find color to use
                // Otherwise, leave it black
                if (Rotate.inBounds(v, w, 0, inputWidth - 1, 0, inputHeight - 1)) {
                    int flatV = (int) (v == inputWidth - 1 ? v - 1 : Math.floor(v));
                    int flatW = (int) (w == inputHeight - 1 ? w - 1 : Math.floor(w));
                    float xFraction = v - flatV;
                    float yFraction = w - flatW;

                    // Get bilinear interpolated values for coordinates (v, w)
                    inputRaster.getPixels(flatV, flatW, 2, 2, inputColors);
                    for (int i = 0; i < pixelStride; ++i) {
                        outputColors[i] = Rotate.getInterpolatedValue(
                                inputColors[i],
                                inputColors[pixelStride + i],
                                inputColors[2 * pixelStride + i],
                                inputColors[3 * pixelStride + i],
                                xFraction,
                                yFraction
                        );
                    }
                }
                else {
                    for (int i = 0; i < pixelStride; ++i) {
                        outputColors[i] = 0;
                    }
                }

                // Set values to (x, y) in output
                outputRaster.setPixel(x, y, outputColors);
            }
        }

        return output;
    }

    private static Point2D.Float rotate(int x, float xc, int y, float yc, float radians) {
        float v = (float) ((x - xc) * Math.cos(radians) - (y - yc) * Math.sin(radians) + xc);
        float w = (float) ((x - xc) * Math.sin(radians) + (y - yc) * Math.cos(radians) + yc);

        return new Point2D.Float(v, w);
    }

    private static boolean inBounds(float x, float y, int xStart, int xEnd, int yStart, int yEnd) {
        if (x < xStart || x > xEnd) return false;
        if (y < yStart || y > yEnd) return false;
        return true;
    }

    /**
     * Calculate value at coordinates v, w given surrounding values via bilinear interpolation.
     * @param s00 = value to top-left of v, w
     * @param s01 = value to top-right of v, w
     * @param s10 = value to bottom-left of v, w
     * @param s11 = value to bottom-right of v, w
     * @param xFraction = relative position between horizontal coordinates
     * @param yFraction = relative position between vertical coordinates
     * @return the bilinear interpolated value
     */
    private static int getInterpolatedValue(int s00, int s01, int s10, int s11, float xFraction, float yFraction) {

        // Use relative location to weight values at surrounding points
        float result = (1 - yFraction) * ((1 - xFraction) * s00 + xFraction * s01)
                + yFraction * ((1 - xFraction) * s10 + xFraction * s11);
        int iresult = Math.round(result);

        return iresult;
    }
}
