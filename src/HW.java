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

		HistogramDataset dataset = new HistogramDataset();
		dataset.setType(HistogramType.FREQUENCY);
		double[] values = new double[width * height];

		float[] hsb = new float[3];
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				int rgb = outputImage.getRGB(x, y);
				int red = (rgb >> 16) & 0xFF;
				int green = (rgb >> 8) & 0xFF;
				int blue = rgb & 0xFF;
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

	public void doSave()
	{
		BufferedImage img = CS450.getImageB();

		CS450.saveImage(img);
	}
}

