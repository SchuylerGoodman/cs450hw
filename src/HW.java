import io.ImageInputStream;

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

	public void doSave()
	{
		BufferedImage img = CS450.getImageB();

		CS450.saveImage(img);
	}
}

