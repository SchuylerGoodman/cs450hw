package transform;

import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.Raster;

/**
 * Created by BaronVonBaerenstein on 3/23/2016.
 */
public class Magnify implements ITransform {

    private int xFactor;

    private int yFactor;

    public Magnify(int xFactor, int yFactor) {

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

        // Create scaled output image object
        int outputWidth = inputWidth * this.xFactor;
        int outputHeight = inputHeight * this.yFactor;
        BufferedImage output = new BufferedImage(outputWidth, outputHeight, input.getType());

        int[] inputColors = new int[4 * sampleModel.getPixelStride()];
        int[] rgb = new int[3];
        // For each coordinate (x, y) in output
        for (int x = 0; x < outputWidth; ++x) {
            for (int y = 0; y < outputHeight; ++y) {

                // Apply inverse transform to find coordinates (v, w) in input
                double v = Magnify.getInverseCoordinate(this.xFactor, x);
                double w = Magnify.getInverseCoordinate(this.yFactor, y);

                // Apply bilinear interpolated value for coordinates (v, w) to (x, y) in output
                inputRaster.getPixels((int) Math.floor(v), (int) Math.floor(w), 2, 2, inputColors);
                // TODO pass correct inputcolors items in here
                double interpolatedValue = Magnify.getInterpolatedValue(
                        inputColors[0],
                        inputColors[1],
                        inputColors[2],
                        inputColors[3],
                        v - Math.floor(v),
                        w - Math.floor(w)
                );

            }
        }

        return null;
    }

    private static double getInverseCoordinate(int factor, int coordinate) {
        return ((double)coordinate) / factor;
    }

    private static double getInterpolatedValue(int s00, int s01, int s10, int s11, double xfrac, double yfrac) {
        return 0.0;
    }


}
