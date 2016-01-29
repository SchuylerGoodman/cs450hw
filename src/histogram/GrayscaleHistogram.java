package histogram;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BaronVonBaerenstein on 1/22/2016.
 */
public class GrayscaleHistogram implements IHistogram {

    private List<IHistogramBucket> buckets;

    public GrayscaleHistogram(int levelCount) {
        this.buckets = new ArrayList<>(levelCount);

        for (int i = 0; i < levelCount; ++i) {
            this.buckets.add(new HistogramBucket());
        }
    }

    public GrayscaleHistogram(BufferedImage image) {

        int levelCount = 0;

        try {
            levelCount = this.parseImageType(image);
        }
        catch (Exception e) {
        }

        this.buckets = new ArrayList<>(levelCount);
        for (int i = 0; i < levelCount; ++i) {
            this.buckets.add(new HistogramBucket());
        }

        // Add pixels to histogram.
        float[] hsb = new float[3];
        int[] rgb = new int[3];
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                int rgbHash = image.getRGB(x, y);
                this.parseRGBHash(rgbHash, rgb);
                Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsb);

                int intensity = Math.round(hsb[2] * ( this.getNumberOfLevels() - 1 ));
                this.buckets.get(intensity).add(new Pixel(x, y, rgbHash));
            }
        }
    }

    @Override
    public int getNumberOfLevels() {
        return this.buckets.size();
    }

    @Override
    public int getTotalPixels() {

        int pixelCount = 0;
        for (IHistogramBucket bucket : this.buckets) {
            pixelCount += bucket.size();
        }

        return pixelCount;
    }

    @Override
    public IHistogramBucket getLevelValues(int level) throws InvalidArgumentException {
        if (0 > level || level >= this.getNumberOfLevels()) {
            throw new InvalidArgumentException(new String[] {"Invalid level index"});
        }
        return this.buckets.get(level);
    }

    @Override
    public IHistogram equalize(IHistogram out) throws Exception {

        // Get mapping from input image space to equalized space.
        List<Integer> map = GrayscaleHistogram.getHistogramEqualizationMap(this);

        System.out.println("Index,MapsTo");
        for (int i = 0; i < map.size(); ++i) {
            System.out.println(String.format("%d,%d",i,map.get(i)));
        }

        // Map histogram to equalized space.
        out = this.mapHistogram(map, out);

        return out;
    }

    @Override
    public IHistogram specify(IHistogram target, IHistogram out) throws Exception {

        List<Integer> inputMap = GrayscaleHistogram.getHistogramEqualizationMap(this);
        List<Integer> targetMap = GrayscaleHistogram.getHistogramEqualizationMap(target);

        // Combine the input equalized map and the inverse output equalized map.
        List<Integer> combinedMap = new ArrayList<>(targetMap);
        int index = 0;
        for (int i = 0; i < inputMap.size(); ++i) {
            while (targetMap.get(index) < inputMap.get(i)) {
                ++index;
            }
            combinedMap.set(i, index);
        }

        // Map histogram to specified space.
        out = this.mapHistogram(combinedMap, out);

        return out;
    }

    @Override
    public void reset() {
        for (IHistogramBucket bucket : this.buckets) {
            bucket.clear();
        }
    }

    @Override
    public int getThresholdValue() {

        // Use Otsu's Method to determine threshold that minimizes spread of high and low intensity values.
        int total = this.getTotalPixels();

        float sum = 0;
        for (int t = 0; t < this.getNumberOfLevels(); ++t) {
            sum += t * this.buckets.get(t).size();
        }

        float sumBackground = 0;
        int weightBackground = 0;
        int weightForeground = 0;

        float maxVariance = 0;
        int threshold = 0;

        for (int t = 0; t < this.getNumberOfLevels(); ++t) {

            IHistogramBucket bucket = this.buckets.get(t);

            weightBackground += bucket.size();
            if (weightBackground == 0) continue;

            weightForeground = total - weightBackground;
            if (weightForeground == 0) break;

            sumBackground += (float) (t * bucket.size());

            float meanBackground = sumBackground / weightBackground;
            float meanForeground = (sum - sumBackground) / weightForeground;

            float varianceBetween = (float) weightBackground
                    * (float) weightForeground
                    * (meanBackground - meanForeground)
                    * (meanBackground - meanForeground);

            if (varianceBetween > maxVariance) {
                maxVariance = varianceBetween;
                threshold = t;
            }
        }

        return threshold;
    }

    private IHistogram mapHistogram(List<Integer> map, IHistogram out) throws Exception {

        // Initialize out histogram if necessary.
        if (out == null || out.getNumberOfLevels() != this.getNumberOfLevels()) {
            out = new GrayscaleHistogram(this.getNumberOfLevels());
        }

        // Reset all buckets in out
        out.reset();

        // Map histogram to equalized space.
        for (int i = 0; i < this.getNumberOfLevels(); ++i) {

            // Get map to index for i-th level pixels.
            int toIndex = map.get(i);

            // If there is an exception, something is wrong with the mapping.
            try {
                // Add all pixels at i-th level to map to index.
                out.getLevelValues(toIndex).addAll(this.getLevelValues(i));
            }
            catch (InvalidArgumentException e) {
                throw new Exception(
                        String.format("Histogram mapping is incorrect, mapping {0} to {1}...", i, toIndex)
                );
            }
        }

        return out;
    }

    private static int parseImageType(BufferedImage image) throws InvalidArgumentException {

        int levels = 0;
        switch (image.getType()) {
            case BufferedImage.TYPE_USHORT_GRAY:
            case BufferedImage.TYPE_BYTE_GRAY:
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_INT_RGB:
                levels = 256;
                break;
            default:
                throw new InvalidArgumentException(new String[] {"Image type not supported"});
        }

        return levels;
    }

    private static List<Integer> getHistogramEqualizationMap(IHistogram histogram) throws InvalidArgumentException {
        List<Integer> map = new ArrayList<>(histogram.getNumberOfLevels());

        // Initialize map (let's see if default to 0 is broken)
        for (int i = 0; i < histogram.getNumberOfLevels(); ++i) {
            map.add(0);
        }

        double constant = (histogram.getNumberOfLevels() - 1) / (double) histogram.getTotalPixels();
        int leastCumulativeValue = 0;
        for (int i = 0; i < map.size(); ++i) {
            IHistogramBucket bucket = histogram.getLevelValues(i);
            leastCumulativeValue += bucket.size();

            int newValue = (int) Math.round(constant * leastCumulativeValue);
            map.set(i, newValue);
        }

        return map;
    }

    private static int[] parseRGBHash(int rgbHash, int[] rgb) {

        if (rgb == null || rgb.length != 3) {
            rgb = new int[3];
        }

        rgb[0] = (rgbHash >> 16) & 0xFF;
        rgb[1] = (rgbHash >> 8) & 0xFF;
        rgb[2] = rgbHash & 0xFF;

        return rgb;
    }

}
