package specman;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JPanel;

public class GlassPane extends JPanel {
	public JPanel panel;
	int menueHeight;
	
	
	public GlassPane(int menueHeight) {
       setOpaque(false);
		setLayout(null);
        panel = new JPanel();
        panel.setBackground(new Color(85,85,85));
        panel.setBounds(100,22, 120, 12);
        this.menueHeight = menueHeight;
        add(panel);
        this.setVisible(true);
        
    }
	
	public void setInputRecBounds(int x, int y, int width, int height) {
		y= y-menueHeight;
		panel.setBounds(x, y, width, height);	

			Specman.instance().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

	}
}