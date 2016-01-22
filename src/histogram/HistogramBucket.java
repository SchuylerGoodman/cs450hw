package histogram;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by BaronVonBaerenstein on 1/22/2016.
 */
public class HistogramBucket extends ArrayList<Pixel> implements IHistogramBucket {

    public HistogramBucket() {
        super();
    }

    /**
     * Copy constructor for deep copies.
     *
     * @param other = the bucket to copy.
     */
    public HistogramBucket(IHistogramBucket other) {
        super();
        Iterator<Pixel> iterator = other.iterator();
        while (iterator.hasNext()) {
            Pixel newPixel = new Pixel(iterator.next());
            this.add(newPixel);
        }
    }
}
