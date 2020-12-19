package specman.draganddrop;

import specman.Specman;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.JPanel;

public class GlassPane extends JPanel {
	public JPanel panel;
	int menueHeight;


	public GlassPane(int menueHeight) {
		setOpaque(false);
		setLayout(null);
		createPanel();
		this.menueHeight = menueHeight;
		add(panel);
		this.setVisible(true);
	}

	private void createPanel() {
		panel = new JPanel();
		panel.setBackground(new Color(85,85,85, 95));
		panel.setBounds(100,22, 120, 12);
	}

	public void setInputRecBounds(int x, int y, int width, int height) {
		y= y-menueHeight;
		panel.setBounds(x, y, width, height);

		Specman.instance().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}
}