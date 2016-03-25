package transform;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Created by BaronVonBaerenstein on 3/23/2016.
 */
public class ReduceNN implements ITransform {

    private int xFactor;

    private int yFactor;

    public ReduceNN(int xFactor, int yFactor) {

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

        // Create scaled output image object
        int outputWidth = inputWidth / this.xFactor;
        int outputHeight = inputHeight / this.yFactor;
        BufferedImage output = new BufferedImage(outputWidth, outputHeight, input.getType());
        WritableRaster outputRaster = output.getRaster();

        int[] colors = new int[pixelStride];

        // For each coordinate (x, y) in output
        for (int x = 0; x < outputWidth; ++x) {
            for (int y = 0; y < outputHeight; ++y) {

                // Apply inverse transform to find coordinates (v, w) in input
                float v = ReduceNN.getInverseCoordinate(inputWidth, outputWidth, x);
                float w = ReduceNN.getInverseCoordinate(inputHeight, outputHeight, y);
                int roundv = Math.round(v);
                int roundw = Math.round(w);

                // Get bilinear interpolated values for coordinates (v, w)
                inputRaster.getPixel(roundv, roundw, colors);

                // Set values to (x, y) in output
                outputRaster.setPixel(x, y, colors);
            }
        }

        return output;
    }

    private static float getInverseCoordinate(int inputDimMax, int outputDimMax, float coordinate) {
        if (outputDimMax <= 1) return 0f;
        return coordinate * ((inputDimMax - 1) / (float) (outputDimMax - 1));
    }

}
