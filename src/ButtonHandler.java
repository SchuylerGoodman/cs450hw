import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by BaronVonBaerenstein on 1/7/2016.
 */

class ButtonHandler implements ActionListener
{
	HashMap<String, Method> map = new HashMap<String, Method>();

	@Override
	public void actionPerformed(ActionEvent event)
	{
		JButton button = (JButton) event.getSource();
		String text = button.getText();

		if (text.equals("Swap"))
		{
			CS450.swapImages();
		}
		else if (text.equals("Undo"))
		{
			CS450.undo();
		}
		else try
		{
			map.get("do"+text).invoke(CS450.hw);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
