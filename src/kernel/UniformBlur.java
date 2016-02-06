package kernel;

/**
 * Created by BaronVonBaerenstein on 12/3/2015.
 */
public class UniformBlur extends AlterRGB {

    /**
     * The radius of the kernel (kernel width and height will be (2 * kernelRadius + 1)).
     */
    private int kernelRadius;

    /**
     * Basic constructor for the MedianBlur kernel.
     *
     * @param kernelRadius = the radius of the kernel (kernel width and height will be (2 * kernelRadius + 1)).
     */
    public UniformBlur(int kernelRadius) {
        this.kernelRadius = kernelRadius;
    }

    @Override
    protected int getKernelRadiusX() {
        return this.kernelRadius;
    }

    @Override
    protected int getKernelRadiusY() {
        return this.kernelRadius;
    }

    @Override
    protected int[] alterPixelRGB(int[] reds, int[] greens, int[] blues, int[] data) {

        if (data == null || data.length < 3) {
            data = new int[3];
        }

        int red = 0;
        int green = 0;
        int blue = 0;

        // Sum each color
        int length = reds.length;
        for (int i = 0; i < length; ++i) {
            red += reds[i];
            green += greens[i];
            blue += blues[i];
        }

        // Divide by number summed
        red /= length;
        green /= length;
        blue /= length;

        // Return average RGB
        data[0] = red;
        data[1] = green;
        data[2] = blue;
        return data;
    }
}
