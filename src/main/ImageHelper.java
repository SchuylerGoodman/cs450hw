package main;

import com.sun.javaws.exceptions.InvalidArgumentException;
import histogram.GrayscaleHistogram;
import histogram.IHistogram;
import histogram.IHistogramBucket;
import histogram.Pixel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.*;
import java.util.List;

/**
 * Created by BaronVonBaerenstein on 1/29/2016.
 */
public final class ImageHelper {
    private ImageHelper() {}

    public static int[] parseRGBHash(int rgbHash, int[] rgb) {

		if (rgb == null || rgb.length != 3) {
			rgb = new int[3];
		}

		rgb[0] = (rgbHash >> 16) & 0xFF;
		rgb[1] = (rgbHash >> 8) & 0xFF;
		rgb[2] = rgbHash & 0xFF;

		return rgb;
	}

	public static void histogramToImage(IHistogram histogram, BufferedImage out) throws InvalidArgumentException {

		if (out == null) {
			throw new InvalidArgumentException(new String[] {"out image cannot be null."});
		}

		// For every bucket in the histogram
		float[] hsb = new float[3];
		int[] rgb = new int[3];
		for (int i = 0; i < histogram.getNumberOfLevels(); ++i) {

			IHistogramBucket bucket = histogram.getLevelValues(i);

			// For every pixel in the bucket, set correct intensity in output image
			for (Pixel pixel : bucket) {

				// Get the HSB of the pixel to preserve hue and saturation
				int rgbHash = pixel.getRGB();
				ImageHelper.parseRGBHash(rgbHash, rgb);
				Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsb);

				// Set the intensity of the pixel in the output image to normalized index
				hsb[2] = i / 255.0f;
				rgbHash = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
				out.setRGB(pixel.getX(), pixel.getY(), rgbHash);
			}
		}
	}

	public static BufferedImage difference(BufferedImage first, BufferedImage second, BufferedImage output) {

		int width = first.getWidth();
		int height = second.getHeight();

		if (output == null
				|| output.getWidth() != width
				|| output.getHeight() != height
				|| output.getType() != first.getType())
		{
			output = new BufferedImage(width, height, first.getType());
		}

		Raster firstRaster = first.getRaster();
		Raster secondRaster = second.getRaster();
		WritableRaster outRaster = output.getRaster();

		int[] firstColor = new int[3];
		int[] secondColor = new int[3];
		int[] outColor = new int[3];
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				firstRaster.getPixel(x, y, firstColor);
				secondRaster.getPixel(x, y, secondColor);
				outColor[0] = Math.abs(firstColor[0] - secondColor[0]);
				outColor[1] = Math.abs(firstColor[1] - secondColor[1]);
				outColor[2] = Math.abs(firstColor[2] - secondColor[2]);

				outRaster.setPixel(x, y, outColor);
			}
		}

		return output;
	}

	public static BufferedImage threshold(BufferedImage inputImage, BufferedImage outputImage) {

		GrayscaleHistogram inputHistogram = new GrayscaleHistogram(inputImage);
		int threshold = inputHistogram.getThresholdValue();

		return ImageHelper.threshold(inputImage, outputImage, threshold);
	}

	public static BufferedImage threshold(BufferedImage inputImage, BufferedImage outputImage, double threshold) {

		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		if (outputImage == null
				|| outputImage.getWidth() != width
				|| outputImage.getHeight() != height
				|| outputImage.getType() != BufferedImage.TYPE_BYTE_GRAY)
		{
            outputImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}

		WritableRaster in = inputImage.getRaster();
		WritableRaster out = outputImage.getRaster();

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int val = in.getSample(x, y, 0);

				if (val < threshold)
				{
					out.setSample(x, y, 0, 0); // black
				}
				else
				{
					out.setSample(x, y, 0, 255); // white
				}
			}
		}

		return outputImage;
	}

	public static BufferedImage bufferedImageDeepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	public static Pixel getWhiteCenterOfBalance(BufferedImage bi) throws InvalidArgumentException {

		IHistogram histogram = new GrayscaleHistogram(bi);

        IHistogramBucket whiteBucket = histogram.getLevelValues(histogram.getNumberOfLevels() - 1);

		if (whiteBucket.isEmpty()) {
			throw new InvalidArgumentException(new String[] {"Input image has no white pixels."});
		}

        int aveX = 0;
        int aveY = 0;
        for (int i = 0; i < whiteBucket.size(); ++i) {
            aveX += whiteBucket.get(i).getX();
            aveY += whiteBucket.get(i).getY();
        }
        aveX /= whiteBucket.size();
        aveY /= whiteBucket.size();

        return new Pixel(aveX, aveY, bi.getRGB(aveX, aveY));
	}

	public static BufferedImage averageImages(Collection<BufferedImage> images) throws InvalidArgumentException {

		if (images.isEmpty()) {
			throw new InvalidArgumentException(new String[] {"Image collection cannot be empty."});
		}

		int width = 0;
		int height = 0;

		BufferedImage lastImage = null;
		List<Raster> rasterList = new ArrayList<>();
		for (BufferedImage image : images) {
            width = image.getWidth();
            height = image.getHeight();
			if (width <= 0 || height <= 0 ||
					(lastImage != null && (width != lastImage.getWidth() || height != lastImage.getHeight()))) {
				throw new InvalidArgumentException(new String[] {"One or more images has invalid dimensions."});
			}
			rasterList.add(image.getRaster());
			lastImage = image;
		}

		BufferedImage output = new BufferedImage(width, height, lastImage.getType());
		WritableRaster outputRaster = output.getRaster();
		int bandCount = 4;
		int[] argb = new int[bandCount];
		int[] argbAve = new int[bandCount];
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {

				// Empty average array
				Arrays.fill(argbAve, 0);

                // Get argb values for pixel and add to average value.
				for (Raster raster : rasterList) {
					raster.getPixel(x, y, argb);
					for (int i = 0; i < bandCount; ++i) {
						argbAve[i] += argb[i];
					}
				}

				// Average argb values.
				for (int i = 0; i < bandCount; ++i) {
					argbAve[i] /= rasterList.size();
				}

				outputRaster.setPixel(x, y, argbAve);
			}
		}

		return output;
	}
}
