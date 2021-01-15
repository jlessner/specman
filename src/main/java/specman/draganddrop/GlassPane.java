package specman.draganddrop;

import specman.Specman;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class GlassPane extends JPanel {
	private JPanel panel;
	private final int menueHeight;
	private int windowsoffset;

	public GlassPane(int menueHeight) {
		setOpaque(false);
		setLayout(null);
		createPanel();
		this.menueHeight = menueHeight;
		add(panel);
		this.setVisible(true);
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")){
		    windowsoffset = 6;
		}
	  
	}

	private void createPanel() {
		panel = new JPanel();
		panel.setBackground(new Color(85,85,85, 95));
	}

	public void setInputRecBounds(int x, int y, int width, int height) {
		y= y-menueHeight;
		panel.setBounds(x-windowsoffset, y, width, height);
		Specman.instance().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		//Specman.instance().setCursor(Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon("src/main/resources/images/plus.png").getImage().getScaledInstance(15,15,Image.SCALE_SMOOTH),new Point(5,5),"custom cursor"));
	}
}