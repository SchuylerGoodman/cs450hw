package kernel;

import java.awt.image.BufferedImage;

/**
 * Created by BaronVonBaerenstein on 12/3/2015.
 */
public interface IKernel {

    /**
     * Apply the kernel to an image, altering it.
     * @param image = the image to alter.
     * @param borderPolicy = a border policy for getting values outside range of image.
     */
    void apply(BufferedImage image, IBorderPolicy borderPolicy);
}
