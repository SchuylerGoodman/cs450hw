import com.sun.javaws.exceptions.InvalidArgumentException;
import histogram.GrayscaleHistogram;
import histogram.IHistogram;
import histogram.IHistogramBucket;
import histogram.Pixel;
import io.ImageInputStream;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.border.TitledBorder;


public class HW
{
	public static void main(String[] args)
	{
		HW hw = new HW();
		CS450.run(hw);

		for (String name : ImageIO.getReaderFormatNames())
		{
			System.out.println(name);
		}
	}

	public void doOpen()
	{
		BufferedImage img = CS450.openImage();

		if (img != null)
		{
			CS450.setImageA(img);
		}
	}

	public void doThreshold()
	{
		String threshold = CS450.prompt("threshold (0 - 255)", "128");
		if (threshold == null) return;
		int t = Integer.parseInt(threshold);

		BufferedImage inputImage = CS450.getImageA();
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

		WritableRaster in = inputImage.getRaster();
		WritableRaster out = outputImage.getRaster();

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int val = in.getSample(x, y, 0);

				if (val < t)
				{
					out.setSample(x, y, 0, 0); // black
				}
				else
				{
					out.setSample(x, y, 0, 255); // white
				}
			}
		}

		CS450.setImageB(outputImage);
	}

	public void doColor_Filter()
	{
		String[] choices = {"RED", "GREEN", "BLUE"};
		String colorChannel = CS450.prompt("color", choices, "GREEN");
		if (colorChannel == null) return;

		BufferedImage inputImage = CS450.getImageA();
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		WritableRaster in = inputImage.getRaster();
		WritableRaster out = outputImage.getRaster();

		int channel = 0; // defaults to RED filter

		if (colorChannel.equals("GREEN"))
		{
			channel = 1;
		}
		else if (colorChannel.equals("BLUE"))
		{
			channel = 2;
		}

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int val = in.getSample(x, y, channel);

				out.setSample(x, y, channel, val);
			}
		}

		CS450.setImageB(outputImage);
	}

	public void doGrayscale() {

		BufferedImage inputImage = CS450.getImageA();
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				int rgb = inputImage.getRGB(x, y);
				Color color = new Color(rgb);
				float value = 0.299f * color.getRed() +
						0.587f * color.getGreen() +
						0.114f * color.getBlue();
				value /= 255;

				int newRGB = Color.HSBtoRGB(0f, 0f, value);
				outputImage.setRGB(x, y, newRGB);
			}
		}

		CS450.setImageB(outputImage);
	}

	public void doGenerateGrayscaleHistogram() {

		BufferedImage outputImage = CS450.getImageB();
		int width = outputImage.getWidth();
		int height = outputImage.getHeight();
		int pixelCount = width * height;

		HistogramDataset dataset = new HistogramDataset();
		dataset.setType(HistogramType.FREQUENCY);
		double[] values = new double[pixelCount];

		float[] hsb = new float[3];
		int[] rgb = new int[3];
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				int rgbHash = outputImage.getRGB(x, y);
				this.parseRGBHash(rgbHash, rgb);
				int red = rgb[0];
				int green = rgb[1];
				int blue = rgb[2];
				Color.RGBtoHSB(red, green, blue, hsb);

				float brightness = hsb[2];
				values[height * x + y] = Math.round(brightness * 255);
			}
		}

		dataset.addSeries("H1", values, 255, 0, 255);

		String plotTitle = "Grayscale Histogram";
		String xAxis = "Value";
		String yAxis = "Frequency";
		PlotOrientation orientation = PlotOrientation.VERTICAL;

		JFreeChart chart = ChartFactory.createHistogram(plotTitle, xAxis, yAxis, dataset, orientation, false, false, false);

		CS450.saveChart(chart, 800, 450);
	}

	public void doHistogramEqualization() {
		BufferedImage inputImage = CS450.getImageA();
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();

		IHistogram histogram = new GrayscaleHistogram(inputImage);

		BufferedImage outputImage = new BufferedImage(width, height, inputImage.getType());

		try {
			IHistogram equalizedHistogram = histogram.equalize(null);

			this.histogramToImage(equalizedHistogram, outputImage);

			CS450.setImageB(outputImage);
		}
		catch (Exception e) {
		}
	}

	public void doHistogramSpecification() {

		BufferedImage inputImage = CS450.getImageA();
		IHistogram inputHistogram = new GrayscaleHistogram(inputImage);

		BufferedImage outputImage = CS450.getImageB();
		IHistogram outputHistogram = new GrayscaleHistogram(outputImage);

		BufferedImage newOutputImage = new BufferedImage(
				inputImage.getWidth(),
				inputImage.getHeight(),
				inputImage.getType()
		);

		try {
			IHistogram specifiedHistogram = inputHistogram.specify(outputHistogram, null);

			this.histogramToImage(specifiedHistogram, newOutputImage);

			CS450.setImageA(newOutputImage);
		}
		catch (Exception e) {
		}
	}

	public void doSave()
	{
		BufferedImage img = CS450.getImageB();

		CS450.saveImage(img);
	}

	private int[] parseRGBHash(int rgbHash, int[] rgb) {

		if (rgb == null || rgb.length != 3) {
			rgb = new int[3];
		}

		rgb[0] = (rgbHash >> 16) & 0xFF;
		rgb[1] = (rgbHash >> 8) & 0xFF;
		rgb[2] = rgbHash & 0xFF;

		return rgb;
	}

	private void histogramToImage(IHistogram histogram, BufferedImage out) throws InvalidArgumentException {

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
				this.parseRGBHash(rgbHash, rgb);
				Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsb);

				// Set the intensity of the pixel in the output image to normalized index
				hsb[2] = i / 255.0f;
				rgbHash = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
				out.setRGB(pixel.getX(), pixel.getY(), rgbHash);
			}
		}
	}
}

