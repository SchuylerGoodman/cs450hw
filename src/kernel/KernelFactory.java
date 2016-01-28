package kernel;

import com.sun.javaws.exceptions.InvalidArgumentException;

/**
 * Created by BaronVonBaerenstein on 1/28/2016.
 */
public class KernelFactory {

    public static final String UNIFORM_BLUR = "Uniform Blur";
    public static final String MEDIAN_BLUR = "Median Blur";
    public static final String SHARPEN = "Sharpen";
    public static final String EDGE_DETECTION = "Edge Detection";

    public static final String[] CHOICES = new String[] {
            UNIFORM_BLUR,
            MEDIAN_BLUR,
            SHARPEN,
            EDGE_DETECTION
    };

    public IKernel create(String type) throws InvalidArgumentException {
        switch (type) {
            case UNIFORM_BLUR:
                return new UniformBlur(1);
            case MEDIAN_BLUR:
                return new MedianBlur(1);
            case SHARPEN:
                return new Sharpen(1);
            case EDGE_DETECTION:
                return new EdgeDetection();
            default:
                throw new InvalidArgumentException(new String[] {"Invalid kernel type - not implemented"});
        }
    }
}
