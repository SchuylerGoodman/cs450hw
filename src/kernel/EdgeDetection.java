package kernel;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.awt.*;

/**
 * Created by BaronVonBaerenstein on 12/4/2015.
 */
public class EdgeDetection extends AlterRGB {

    // Spatial filtering constants
    private static final int KERNEL_RADIUS = 1;
    private static final int TOTAL_KERNEL_SIZE = (int) Math.pow(2 * KERNEL_RADIUS + 1, 2);

    // Sobel filter constants
    private static final int[] X_DERIVATIVE_FILTER = { -1, 0, 1, -2, 0, 2, -1, 0, 1 };
    private static final int[] Y_DERIVATIVE_FILTER = { -1, -2, -1, 0, 0, 0, 1, 2, 1 };
    private static final int SOBEL_NORMALIZER = Math.abs(X_DERIVATIVE_FILTER[0]) + X_DERIVATIVE_FILTER[2] +
                                                Math.abs(X_DERIVATIVE_FILTER[3]) + X_DERIVATIVE_FILTER[5] +
                                                Math.abs(X_DERIVATIVE_FILTER[6]) + X_DERIVATIVE_FILTER[8];

    private static final int MAX_COLOR = 255;

    @Override
    protected int getKernelRadius() {
        return KERNEL_RADIUS;
    }

    @Override
    protected int[] alterPixelRGB(int[] reds, int[] greens, int[] blues, int[] data) throws InvalidArgumentException {

        if (data == null || data.length < 3) {
            data = new int[3];
        }

        if (!validateColorArray(reds) || !validateColorArray(greens) || !validateColorArray(blues)) {
            throw new InvalidArgumentException(new String[] {"Invalid total kernel size in color array."});
        }

        float[] hsb = new float[3];
        double xDerivative = 0.0;
        double yDerivative = 0.0;
        for (int i = 0; i < TOTAL_KERNEL_SIZE; ++i) {
            Color.RGBtoHSB(reds[i], greens[i], blues[i], hsb);
            double brightness = hsb[2];
            xDerivative += X_DERIVATIVE_FILTER[i] * brightness;
            yDerivative += Y_DERIVATIVE_FILTER[i] * brightness;
        }

        xDerivative /= SOBEL_NORMALIZER;
        yDerivative /= SOBEL_NORMALIZER;

        // Get the magnitude of the derivative gradient
        double gradientMagnitude = Math.sqrt(Math.pow(xDerivative, 2) + Math.pow(yDerivative, 2));

        // Map brightness to RGB color space
        int colorValue = (int) ( gradientMagnitude * MAX_COLOR );

        // Edge detection is grayscale
        data[0] = colorValue;
        data[1] = colorValue;
        data[2] = colorValue;

        return data;
    }

    private boolean validateColorArray(int[] color) {

        if (color.length != TOTAL_KERNEL_SIZE) {
            return false;
        }
        return true;
    }
}
