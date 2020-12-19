package specman;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import specman.textfield.InsetPanel;
import specman.view.AbstractSchrittView;




public class DragButtonAdapter extends MouseAdapter {
	private Specman spec;
	private JTextField dummy;

	public DragButtonAdapter(Specman spec) {
		this.spec = spec;
	}

	public void mousePressed(MouseEvent e) {
		if(e.getSource() instanceof JLabel){
			AbstractSchrittView step = labelToStep((JLabel)e.getSource());
			dummy = new JTextField("Schritt "+step.getId().toString() );
		}else {
			dummy = new JTextField("Neuer Schritt");
		}
		dummy.setBounds(new Rectangle(150, 15));
	}

	public void mouseDragged(MouseEvent e) {
		if(checkEinzigerSchritt(e)){
			spec.showInvalidCursor();
			return;
		}
		Point pt = e.getPoint();
		JComponent parent = (JComponent) e.getComponent();
		Point dp = pt;
		Point dragOffset = new Point(pt.x - dp.x, pt.y - dp.y);
		//-2 da performanter
		Point ptCon = SwingUtilities.convertPoint((Component)e.getSource(),(int) e.getPoint().getX(),(int)e.getPoint().getY()-2, spec);
		spec.getGlassPane().setVisible(false);
		spec.window.add(dummy);
		spec.window.pack();
		updateWindowLocation(pt,dragOffset, parent);
		spec.window.setVisible(true);
		spec.dragGlassPanePos(ptCon, spec.hauptSequenz.schritte,false,e);

	}

	private void updateWindowLocation(Point pt,Point dragOffset, JComponent parent){
		Point p = new Point(pt.x - dragOffset.x +3, pt.y - dragOffset.y +3);
		SwingUtilities.convertPointToScreen(p,parent);
		spec.window.setLocation(p);
	}

	public void mouseReleased(MouseEvent e) {
		if(checkEinzigerSchritt(e)){
			return;
		}
		spec.window.setVisible(false);
		Point ptCon = SwingUtilities.convertPoint((Component)e.getSource(),(int) e.getPoint().getX(),(int)e.getPoint().getY()-2, spec);
		spec.dragGlassPanePos(ptCon, spec.hauptSequenz.schritte,true,e);
		//System.out.println(e.getSource());
		spec.getGlassPane().setVisible(false);
		spec.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		spec.window.remove(dummy);
	}

	//Detection funktioniert aber der Cursor wird nicht gesetzt
	public void mouseEntered(MouseEvent e) {
		if(e.getSource() instanceof JLabel){
			JLabel label = (JLabel) e.getSource();
			InsetPanel ip = (InsetPanel) label.getParent().getParent();
			AbstractSchrittView step =   spec.hauptSequenz.findeSchritt(ip.getTextfeld().getTextComponent());
			//Abfrage ob man sich auf sich selbst befindet
			if(step.getParent().schritte.size() <= 1) {
				spec.showInvalidCursor();
			}
		}
	}
	public void mouseExited(MouseEvent e) {
		spec.setCursor(Cursor.getDefaultCursor());
	}
	//Letzer Schritt darf nicht verschoben werden
	private boolean checkEinzigerSchritt(MouseEvent e) {
		if(e.getSource() instanceof JLabel){
			AbstractSchrittView step = labelToStep( (JLabel) e.getSource());
			if(step.getParent().schritte.size()<=1) {
				return true;
			}
		}
		return false;
	}

	private AbstractSchrittView labelToStep(JLabel label) {
		InsetPanel ip = (InsetPanel) label.getParent().getParent();
		return spec.hauptSequenz.findeSchritt(ip.getTextfeld().getTextComponent());
	}
}
