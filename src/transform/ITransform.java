package transform;

import java.awt.image.BufferedImage;

/**
 * Created by BaronVonBaerenstein on 3/23/2016.
 */
public interface ITransform {
    BufferedImage apply(BufferedImage input);
}
