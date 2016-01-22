package histogram;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Created by BaronVonBaerenstein on 1/22/2016.
 */
public interface IHistogram {

    int getNumberOfLevels();

    int getTotalPixels();

    IHistogramBucket getLevelValues(int level) throws InvalidArgumentException;

    IHistogram equalize(IHistogram out) throws Exception;

    IHistogram specify(IHistogram target, IHistogram out) throws Exception;

    void reset();
}
