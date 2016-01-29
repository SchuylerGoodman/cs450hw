import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;

import com.sun.javaws.exceptions.InvalidArgumentException;
import io.ImageInputStream;
import io.ImageOutputStream;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

/**
 * Created by BaronVonBaerenstein on 1/7/2016.
 */
public class CS450
{
	static Object hw;
	static JFrame window = new JFrame();
	static JViewport viewA, viewB;
	static ButtonHandler buttonHandler = new ButtonHandler();
	static JFileChooser fileChooser = new JFileChooser(".");
	static BufferedImage[][] history = new BufferedImage[32][2];
	static int historyLength, historyIndex;

	public static void run(Object hw)
	{
		CS450.hw = hw;

		history[0][0] = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		history[0][1] = history[0][0];

		setupSwingComponents();
	}

	public static BufferedImage getImageA()
	{
		return history[historyIndex][0];
	}

	public static BufferedImage getImageB()
	{
		return history[historyIndex][1];
	}

	private static void update()
	{
		JLabel label = (JLabel) viewA.getComponent(0);
		ImageIcon icon = (ImageIcon) label.getIcon();
		icon.setImage(history[historyIndex][0]);
		viewA.remove(0);
		viewA.add(label);

		label = (JLabel) viewB.getComponent(0);
		icon = (ImageIcon) label.getIcon();
		icon.setImage(history[historyIndex][1]);
		viewB.remove(0);
		viewB.add(label);

		window.repaint();
	}

	public static void setImageA(BufferedImage img)
	{
		int prevIndex = historyIndex;
		historyIndex++;
		historyIndex %= history.length;
		history[historyIndex][0] = img;
		history[historyIndex][1] = history[prevIndex][1];
		historyLength++;

		if (historyLength > history.length)
		{
			historyLength = history.length;
		}

		update();
	}

	public static void setImageB(BufferedImage img)
	{
		int prevIndex = historyIndex;
		historyIndex++;
		historyIndex %= history.length;
		history[historyIndex][0] = history[prevIndex][0];
		history[historyIndex][1] = img;
		historyLength++;

		if (historyLength > history.length)
		{
			historyLength = history.length;
		}

		update();
	}

	static void swapImages()
	{
		BufferedImage img = history[historyIndex][0];
		history[historyIndex][0] = history[historyIndex][1];
		history[historyIndex][1] = img;

		update();
	}

	static void undo()
	{
		if (historyLength > 0)
		{
			historyLength--;
			historyIndex--;
			historyIndex += history.length;
			historyIndex %= history.length;

			update();
		}
	}

	public static BufferedImage openImage()
	{
		try
		{
			int val = fileChooser.showOpenDialog(window);

			if (val == JFileChooser.APPROVE_OPTION)
			{
				File file = fileChooser.getSelectedFile();

				BufferedImage img = ImageIO.read(file);
                //ImageInputStream iis = new ImageInputStream(file);
                //BufferedImage img = iis.read();

				return img;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static void saveImage(BufferedImage img)
	{
		try
		{
			int val = fileChooser.showSaveDialog(window);

			if (val == JFileChooser.APPROVE_OPTION)
			{
				File file = fileChooser.getSelectedFile();
				int dot = file.getName().lastIndexOf('.');
				String format = file.getName().substring(dot + 1);

				if (!ImageIO.write(img, format, file)) {
					throw new InvalidArgumentException(new String[] {"Invalid file format"});
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void saveChart(JFreeChart chart, int width, int height)
	{
		try
		{
			int val = fileChooser.showSaveDialog(window);

			if (val == JFileChooser.APPROVE_OPTION)
			{
				File file = fileChooser.getSelectedFile();
				int dot = file.getName().lastIndexOf('.');
				String suffix = file.getName().substring(dot + 1);

				String newTitle = chart.getTitle().getText() + " for \"" + file.getName().substring(0, dot) + "\"";
				chart.setTitle(newTitle);
				if (suffix.equals("jpg") || suffix.equals("jpeg"))
				{
					ChartUtilities.saveChartAsJPEG(file, chart, width, height);
				}
				else if (suffix.equals("png"))
				{
					ChartUtilities.saveChartAsPNG(file, chart, width, height);
				}
				else {
					throw new InvalidArgumentException(new String[] {"Invalid file type for chart output"});
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String prompt(String message)
	{
		return prompt(message, null, null);
	}

	public static String prompt(String message, String defaultValue)
	{
		return prompt(message, null, defaultValue);
	}

	public static String prompt(String message, String[] choices)
	{
		return prompt(message, choices, null);
	}

	public static String prompt(String message, String[] choices, String defaultValue)
	{
		Object answer = JOptionPane.showInputDialog(
			window,
			message,
			null,
			JOptionPane.PLAIN_MESSAGE,
			null, // no icon
			choices,
			defaultValue
		);

		return (String) answer;
	}

	public static void message(String message) {
		JOptionPane.showMessageDialog(
				window,
				message
		);
	}

	private static void setupSwingComponents()
	{
		ImageIcon iconA = new ImageIcon(history[0][0]);
		ImageIcon iconB = new ImageIcon(history[0][1]);

		JScrollPane paneA = new JScrollPane(new JLabel(iconA));
		paneA.setBorder(new TitledBorder("Image A"));
		viewA = paneA.getViewport();
		viewA.setPreferredSize(new Dimension(512, 512));

		JScrollPane paneB = new JScrollPane(new JLabel(iconB));
		paneB.setBorder(new TitledBorder("Image B"));
		viewB = paneB.getViewport();
		viewB.setPreferredSize(new Dimension(512, 512));

		JButton buttonSwap = new JButton("Swap");
		buttonSwap.setPreferredSize(new Dimension(70, 25));
		buttonSwap.addActionListener(buttonHandler);

		JButton buttonUndo = new JButton("Undo");
		buttonUndo.setPreferredSize(new Dimension(70, 25));
		buttonUndo.addActionListener(buttonHandler);

		JPanel controlPanel = new JPanel();
		controlPanel.add(buttonUndo);
		controlPanel.add(buttonSwap);
		controlPanel.setPreferredSize(new Dimension(70, 100));

		JPanel imagesPanel = new JPanel();
		imagesPanel.add(paneA);
		imagesPanel.add(controlPanel);
		imagesPanel.add(paneB);

		JPanel buttonPanel = reflectButtons();

		window.add(imagesPanel, BorderLayout.CENTER);
		window.add(buttonPanel, BorderLayout.SOUTH);
		window.pack();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}

	private static JPanel reflectButtons()
	{
		JPanel panel = new JPanel();

		for (Method method : hw.getClass().getMethods())
		{
			String name = method.getName().replace('_', ' ');

			if (name.startsWith("do"))
			{
				JButton button = new JButton(name.substring(2));
				button.addActionListener(buttonHandler);
				buttonHandler.map.put(name, method);
				panel.add(button);
			}
		}

		return panel;
	}

}

