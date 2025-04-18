package bvb.gui;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class TabPanelInfo extends JPanel
{
	public TabPanelInfo()
	{
		super(new GridLayout());
		this.setBorder(new PanelTitle(" Shortcuts "));
		String shortCutInfo ="<html><center><b>View/Navigation</b></center><br>"
					+"&nbsp;<b>P</b> - show sources panel<br><br>"
					+"&nbsp;<b>S</b> - brightness/color dialog<br><br>"
					+"&nbsp;<b>C</b> - center the view (zoom out)<br><br>"
					+"&nbsp;<b>O</b> - toggle render method<br><br>"
					+"&nbsp;<b>Shift + X/Y/Z</b> - rotate to major plane<br><br>"					
					+"&nbsp;<b>M</b>/<b>N</b> - timepoint +/- <br><br></html>";
		JLabel jlInfo = new JLabel(shortCutInfo);
		jlInfo.setVerticalAlignment(SwingConstants.CENTER);
		jlInfo.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(jlInfo);

	}
}
