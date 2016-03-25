package transform;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Created by BaronVonBaerenstein on 3/23/2016.
 */
public class Reduce implements ITransform {

    private int xFactor;

    private int yFactor;

    public Reduce(int xFactor, int yFactor) {

        if (xFactor < 1 || yFactor < 1) {
            throw new IllegalArgumentException("Magnification factors must be positive integers.");
        }

        this.xFactor = xFactor;
        this.yFactor = yFactor;
    }

    @Override
    public BufferedImage apply(BufferedImage input) {

        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        Raster inputRaster = input.getRaster();
        ComponentSampleModel sampleModel = (ComponentSampleModel) input.getSampleModel();
        int pixelStride = sampleModel.getPixelStride();

        // TODO figure out how to scale down
        // Create scaled output image object
        int outputWidth = inputWidth / this.xFactor;
        int outputHeight = inputHeight / this.yFactor;
        BufferedImage output = new BufferedImage(outputWidth, outputHeight, input.getType());
        WritableRaster outputRaster = output.getRaster();

        int[] inputColors = new int[4 * pixelStride];
        int[] outputColors = new int[pixelStride];

        // For each coordinate (x, y) in output
        for (int x = 0; x < outputWidth; ++x) {
            for (int y = 0; y < outputHeight; ++y) {

                // Apply inverse transform to find coordinates (v, w) in input
                float v = Reduce.getInverseCoordinate(inputWidth, outputWidth, x);
                float w = Reduce.getInverseCoordinate(inputHeight, outputHeight, y);
                int flatv = (int) (v == inputWidth - 1 ? v - 1 : Math.floor(v));
                int flatw = (int) (w == inputHeight - 1 ? w - 1 : Math.floor(w));
                float xfrac = v - flatv;
                float yfrac = w - flatw;

                // Get bilinear interpolated values for coordinates (v, w)
                inputRaster.getPixels(flatv, flatw, 2, 2, inputColors);
                for (int i = 0; i < pixelStride; ++i) {
                    outputColors[i] = Reduce.getInterpolatedValue(
                            inputColors[i],
                            inputColors[pixelStride + i],
                            inputColors[2 * pixelStride + i],
                            inputColors[3 * pixelStride + i],
                            xfrac,
                            yfrac
                    );
                }

                // Set values to (x, y) in output
                outputRaster.setPixel(x, y, outputColors);
            }
        }

        return output;
    }

    private static float getInverseCoordinate(int inputDimMax, int outputDimMax, float coordinate) {
        if (outputDimMax <= 1f) return 0f;
        return (coordinate + 1) * ((inputDimMax - 1) / (float) (outputDimMax - 1));
    }

    /**
     * Calculate value at coordinates v, w given surrounding values via bilinear interpolation.
     *
     * @param s00 = value to top-left of v, w
     * @param s01 = value to top-right of v, w
     * @param s10 = value to bottom-left of v, w
     * @param s11 = value to bottom-right of v, w
     * @param xfrac = relative position between horizontal coordinates
     * @param yfrac = relative position between vertical coordinates
     * @return the bilinear interpolated value
     */
    private static int getInterpolatedValue(int s00, int s01, int s10, int s11, float xfrac, float yfrac) {

        // Use relative location to weight values at surrounding points
        float result = (1 - yfrac) * ((1 - xfrac) * s00 + xfrac * s01) + yfrac * ((1 - xfrac) * s10 + xfrac * s11);
        int iresult = Math.round(result);

        return iresult;
    }


}
