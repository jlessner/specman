package specman.draganddrop;

import specman.Specman;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.JPanel;

public class GlassPane extends JPanel {
	private JPanel insertPosIndicator;
	private final int menueHeight;
	private int windowsoffset;

	public GlassPane(int menueHeight) {
		setOpaque(false);
		setLayout(null);
		createInsertPosIndicator();
		this.menueHeight = menueHeight;
		add(insertPosIndicator);
		this.setVisible(true);
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")){
		    windowsoffset = 6;
		}
	  
	}

	private void createInsertPosIndicator() {
		insertPosIndicator = new JPanel();
		insertPosIndicator.setBackground(new Color(85,85,85, 95));
	}

	public void setInputRecBounds(int x, int y, int width, int height) {
		y -= menueHeight;
		insertPosIndicator.setBounds(x - windowsoffset, y, width, height);
		Specman.instance().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}
}
