package specman;

import java.awt.Color;

import javax.swing.JPanel;

public class GlassPane extends JPanel {
	public JPanel panel;
	int menueHeight =22;
	
	public GlassPane() {
       setOpaque(false);
		setLayout(null);
        panel = new JPanel();
        panel.setBackground(new Color(85,85,85));
        panel.setBounds(100,22, 120, 12);
        add(panel);
        this.setVisible(true);
    }
	
	public void setInputRecBounds(int x, int y, int width, int height) {
		y= y-menueHeight;
		panel.setBounds(x, y, width, height);		
	}
}