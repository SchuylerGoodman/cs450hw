package kernel;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.awt.image.BufferedImage;

/**
 * Created by BaronVonBaerenstein on 1/28/2016.
 */
public class KernelFactory {

    public static final String UNIFORM_BLUR = "Uniform Blur";
    public static final String MEDIAN_BLUR = "Median Blur";
    public static final String SHARPEN = "Sharpen";
    public static final String EDGE_DETECTION = "Edge Detection";
    public static final String TEMPLATE_MATCHING = "Template Matching";
    public static final String DOUBLE_TEMPLATE_MATCHING = "Double Template Matching";

    public static final String[] FILTER_CHOICES = new String[] {
            UNIFORM_BLUR,
            MEDIAN_BLUR,
            SHARPEN,
            EDGE_DETECTION
    };

    public static final String[] TEMPLATE_CHOICES = new String[] {
            TEMPLATE_MATCHING,
            DOUBLE_TEMPLATE_MATCHING
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

    public IKernel create(String type, int minSubsample, BufferedImage templateImage) throws InvalidArgumentException {
        switch (type) {
            case TEMPLATE_MATCHING:
                return new TemplateMatch(templateImage, minSubsample, false);
            case DOUBLE_TEMPLATE_MATCHING:
                return new TemplateMatch(templateImage, minSubsample, true);
            default:
                throw new InvalidArgumentException(new String[] {"Invalid kernel type - not implemented"});
        }
    }
}
