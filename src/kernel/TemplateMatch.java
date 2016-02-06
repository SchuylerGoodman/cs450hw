package kernel;

import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.util.Pair;
import sun.awt.image.ToolkitImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by BaronVonBaerenstein on 2/3/2016.
 */
public class TemplateMatch extends AlterRGB {

    // Finals
    private final BufferedImage baseTemplateImage;
    private final int minSubsample;
    private final boolean doubleTemplate;

    // Template variables
    private double[] reds;
    private double[] greens;
    private double[] blues;
    double redAverage;
    double greenAverage;
    double blueAverage;
    private int kernelRadiusX;
    private int kernelRadiusY;

    /**
     * Constructor for a template matching kernel.
     *
     * @param templateImage = the image representing the template.
     * @param minSubsample = the minimum dimension (in x or y) to subsample the template to for faster processing.
     * @param doubleTemplate = whether the template can match 2 shapes instead of just one.
     * @throws InvalidArgumentException
     */
    public TemplateMatch(BufferedImage templateImage, int minSubsample, boolean doubleTemplate) throws InvalidArgumentException {

        this.baseTemplateImage = templateImage;
        this.minSubsample = minSubsample;
        this.doubleTemplate = doubleTemplate;

        int width = templateImage.getWidth();
        int height = templateImage.getHeight();

        // Template must have odd dimensions because we calculate correlation at center pixel.
        if (width % 2 == 0 || height % 2 == 0) {
            throw new InvalidArgumentException(new String[] {"Template image dimensions must be odd."});
        }
    }

    private void initTemplate(BufferedImage templateImage) {

        int width = templateImage.getWidth();
        int height = templateImage.getHeight();
        int pixelCount = width * height;

        // Initialize template information holders
        redAverage = 0.0;
        greenAverage = 0.0;
        blueAverage = 0.0;
        reds = new double[pixelCount];
        greens = new double[pixelCount];
        blues = new double[pixelCount];

        // Get color information out of template image.
        int p = 0;
        //Raster raster = templateImage.getRaster();
        int[] rgb = new int[3];
        double colorMax = 255;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                //raster.getPixel(x, y, rgb);
                int rgbHash = templateImage.getRGB(x, y);
                rgb[0] = (rgbHash >> 16) & 0xFF;
                rgb[1] = (rgbHash >> 8) & 0xFF;
                rgb[2] = rgbHash & 0xFF;
                reds[p] = rgb[0] / colorMax;
                greens[p] = rgb[1] / colorMax;
                blues[p] = rgb[2] / colorMax;

                redAverage += reds[p];
                greenAverage += greens[p];
                blueAverage += blues[p];
                ++p;
            }
        }

        redAverage /= pixelCount;
        greenAverage /= pixelCount;
        blueAverage /= pixelCount;

        // Subtract average from values to get 0 mean template.
        for (int i = 0; i < pixelCount; ++i) {
            reds[i] -= redAverage;
            greens[i] -= greenAverage;
            blues[i] -= blueAverage;
        }

        kernelRadiusX = width / 2;
        kernelRadiusY = height / 2;
    }

    @Override
    protected int getKernelRadiusX() {
        return kernelRadiusX;
    }

    @Override
    protected int getKernelRadiusY() {
        return kernelRadiusY;
    }

    @Override
    protected void alterRGB(BufferedImage image, IBorderPolicy borderPolicy, int kernelRadiusX, int kernelRadiusY,
                            int windowStartX, int windowStartY, int width, int height)
    {
        int baseTemplateWidth = this.baseTemplateImage.getWidth();
        int baseTemplateHeight = this.baseTemplateImage.getHeight();
        int minTemplateSize = Math.min(baseTemplateWidth, baseTemplateHeight);

        // Build pyramid of subsampled templates and images.
        int shrinkFactor = 2;
        Stack<BufferedImage> templatePyramid = new Stack<>();
        Stack<BufferedImage> imagePyramid = new Stack<>();

        templatePyramid.add(this.baseTemplateImage);
        imagePyramid.add(image);

        BufferedImage smallerTemplate = this.baseTemplateImage;
        BufferedImage smallerImage = image;

        while (minTemplateSize / shrinkFactor > this.minSubsample) {

            minTemplateSize /= shrinkFactor;

            // Template cannot have even dimensions.
            int templateWidth = smallerTemplate.getWidth() / shrinkFactor;
            int templateHeight = smallerTemplate.getHeight() / shrinkFactor;
            int imageWidth = smallerImage.getWidth() / shrinkFactor;
            int imageHeight = smallerImage.getHeight() / shrinkFactor;

            if (templateWidth % 2 == 0) {
                templateWidth += 1;
                imageWidth += 1;
            }

            if (templateHeight % 2 == 0) {
                templateHeight += 1;
                imageHeight += 1;
            }

            smallerTemplate = this.shrinkImage(smallerTemplate, templateWidth, templateHeight);
            smallerImage = this.shrinkImage(
                    smallerImage,
                    imageWidth,
                    imageHeight
            );

            try {
                File file = new File("img/smallerTemplate.png");
                ImageIO.write(smallerTemplate, "png", file);

                file = new File("img/smallerImage.png");
                ImageIO.write(smallerImage, "png", file);
            }
            catch (IOException e) {
                System.err.println(e.getMessage());
            }

            templatePyramid.add(smallerTemplate);
            imagePyramid.add(smallerImage);
        }

        // Acknowledged, this is a totally hacky loop.
        // I could probably fix it with some kind of extraction refactoring,
        // but I don't really feel like it.
        int nextX = 0;
        int nextY = 0;
        BufferedImage peakImage = imagePyramid.peek();
        int windowWidth = peakImage.getWidth();
        int windowHeight = peakImage.getHeight();
        boolean first = true;
        while (!templatePyramid.empty() && !imagePyramid.empty()) {

            // Double pixel location to go down a level in pyramid.
            nextX *= 2;
            nextY *= 2;

            BufferedImage topTemplate = templatePyramid.pop();
            BufferedImage topImage = imagePyramid.pop();

            // Bound the coordinates so we get a valid window.
            nextX = nextX - 1 < 0 ? 0 : nextX - 1;
            nextY = nextY - 1 < 0 ? 0 : nextY - 1;
            if (nextX + windowWidth >= topImage.getWidth()) {
                windowWidth -= (nextX + windowWidth) - topImage.getWidth();
            }

            if (nextY + windowHeight >= topImage.getHeight()) {
                windowHeight -= (nextY + windowHeight) - topImage.getHeight();
            }

            /*topImage = topImage.getSubimage(
                    nextX - 1,
                    nextY - 1,
                    windowWidth,
                    windowHeight
            );*/

            // Initialize template variables based on the top template in the stack.
            this.initTemplate(topTemplate);

            // Change image colors based on correlation.
            super.alterRGB(
                    topImage,
                    borderPolicy,
                    this.kernelRadiusX,
                    this.kernelRadiusY,
                    nextX,
                    nextY,
                    windowWidth,
                    windowHeight
            );

            if (first) {
                try {
                    File file = new File("img/match_image.png");
                    ImageIO.write(topImage, "png", file);

                    file = new File("img/match_template.png");
                    ImageIO.write(topTemplate, "png", file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Find the point with highest intensity.
            Point2D maxPoint = this.getBrightestPixel(
                    topImage,
                    nextX,
                    nextY,
                    windowWidth,
                    windowHeight
            );

            nextX = (int) maxPoint.getX();
            nextY = (int) maxPoint.getY();

            // After the first run this is always 3.
            windowWidth = 3;
            windowHeight = 3;

            first = false;
        }

        // Set new colors in image with bright spot only at nextPoint.
        System.out.printf("(x, y): (%d, %d)\n", nextX, nextY);
    }

    private BufferedImage shrinkImage(BufferedImage image, int targetWidth, int targetHeight) {

        BufferedImage newImage = new BufferedImage(targetWidth, targetHeight, image.getType());
        Graphics2D g2 = newImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        //g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();

        return newImage;
    }

    private Point2D getBrightestPixel(BufferedImage image, int windowStartX, int windowStartY, int windowWidth, int windowHeight) {

        int red, green, blue;
        float[] hsb = new float[3];
        float maxBrightness = Float.MIN_VALUE;
        Point2D maxPoint = new Point2D.Double(windowStartX, windowStartY);
        for (int x = windowStartX; x < windowStartX + windowWidth; ++x) {
            for (int y = windowStartY; y < windowStartY + windowHeight; ++y) {
                int rgbHash = image.getRGB(x, y);
                red = (rgbHash >> 16) & 0xFF;
                green = (rgbHash >> 8) & 0xFF;
                blue = rgbHash & 0xFF;
                Color.RGBtoHSB(red, green, blue, hsb);

                if (hsb[2] > maxBrightness) {
                    maxBrightness = hsb[2];
                    maxPoint.setLocation(x, y);
                }
            }
        }

        //System.out.printf("Max brightness is %f", maxBrightness);

        return maxPoint;
    }

    @Override
    protected int[] alterPixelRGB(int[] reds, int[] greens, int[] blues, int[] data) throws InvalidArgumentException {

        double correlation = this.calculateCorrelation(reds, greens, blues, data);

        // Threshold new color based on correlation.
        int color = 0;
        if (!this.doubleTemplate && correlation > 0.0) {
            color = (int) (correlation * 255);
        }
        else if (this.doubleTemplate) {
            color = (int) ((correlation + 1) * 127.5);
        }

        data[0] = data[1] = data[2] = color;

        return data;
    }

    protected double calculateCorrelation(int[] reds, int[] greens, int[] blues, int[] data) throws InvalidArgumentException {

        assert data != null;

        double correlation = 0.0;

        correlation += this.crossCorrelate(this.reds, reds);
        correlation += this.crossCorrelate(this.greens, greens);
        correlation += this.crossCorrelate(this.blues, blues);
        correlation /= 3;

        return correlation;
    }

    /**
     * Run 0 mean cross-correlation on two dimension-matched arrays.
     *
     * @param template = the template array to match.
     * @param window = the window array to match.
     * @return the similarity measure for the cross-correlation.
     */
    private double crossCorrelate(double[] template, int[] window) throws InvalidArgumentException {

        if (template.length != window.length) {
            throw new InvalidArgumentException(new String[] {"Cross correlation array lengths must match."});
        }

        int length = template.length;

        double windowAverage = 0.0;
        double colorMax = 255;
        double[] windowDouble = new double[length];
        for (int i = 0; i < length; ++i) {
            windowDouble[i] = window[i] / colorMax;
            windowAverage += windowDouble[i];
        }
        windowAverage /= length;

        // Calculate 0 mean normalized cross-correlation
        // See https://siddhantahuja.files.wordpress.com/2009/05/zncc2.png
        double templateWeight;
        double windowWeight;
        double numerator = 0.0;
        double templateDenominator = 0.0;
        double windowDenominator = 0.0;
        for (int i = 0; i < length; ++i) {
            templateWeight = template[i]; // Template is already 0 mean.
            windowWeight = windowDouble[i] - windowAverage;
            numerator += templateWeight * windowWeight;
            templateDenominator += Math.pow(templateWeight, 2);
            windowDenominator += Math.pow(windowWeight, 2);
        }
        double denominator = Math.sqrt(templateDenominator * windowDenominator);

        double correlation = numerator / denominator;

        return correlation;
    }
}
