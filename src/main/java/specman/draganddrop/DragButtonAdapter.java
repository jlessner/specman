package specman.draganddrop;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import specman.Specman;
import specman.textfield.InsetPanel;
import specman.view.AbstractSchrittView;

import static specman.draganddrop.InsertDecision.Insert;
import static specman.draganddrop.InsertDecision.NoInsert;


public class DragButtonAdapter extends MouseAdapter {
	private final Specman spec;
	private	JTextField dummy;
	private final DraggingLogic draggingLogic;

	public DragButtonAdapter(Specman spec) {
		this.spec = spec;
		this.draggingLogic = new DraggingLogic(spec);
	}

	public void mousePressed(MouseEvent e) {
		setDummy(e);
		dummy.setBounds(new Rectangle(150, 15));
	}

	public void mouseDragged(MouseEvent e) {
		if(checkEinzigerSchritt(e)){
			draggingLogic.showInvalidCursor();
			return;
		}
		Point pt = e.getPoint();
		JComponent parent = (JComponent) e.getComponent();
		//-2 da performanter
		Point ptCon = SwingUtilities.convertPoint((Component)e.getSource(),(int) e.getPoint().getX(),(int)e.getPoint().getY()-2, spec);
		spec.getGlassPane().setVisible(false);
		spec.window.add(dummy);
		spec.window.pack();
		updateWindowLocation(pt, parent);
		spec.window.setVisible(true);
		draggingLogic.dragGlassPanePos(ptCon, spec.hauptSequenz.schritte,NoInsert,e);

	}

	//Updates the Window Location
	private void updateWindowLocation(Point pt, JComponent parent){
		Point p = new Point(pt.x  +3, pt.y +3);
		SwingUtilities.convertPointToScreen(p,parent);
		spec.window.setLocation(p);
	}

	public void mouseReleased(MouseEvent e) {
		if(checkEinzigerSchritt(e)){
			spec.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			return;
		}
		spec.window.setVisible(false);
		Point ptCon = SwingUtilities.convertPoint((Component)e.getSource(),(int) e.getPoint().getX(),(int)e.getPoint().getY()-2, spec);
		draggingLogic.dragGlassPanePos(ptCon, spec.hauptSequenz.schritte,Insert,e);
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
				draggingLogic.showInvalidCursor();
			}
		}
	}

	public void mouseExited(MouseEvent e) {
		spec.setCursor(Cursor.getDefaultCursor());
	}

	//Letzter Schritt darf nicht verschoben werden
	private boolean checkEinzigerSchritt(MouseEvent e) {
		if(e.getSource() instanceof JLabel){
			AbstractSchrittView step = labelToStep( (JLabel) e.getSource());
			return step.getParent().schritte.size() <= 1;
		}
		return false;
	}

	private AbstractSchrittView labelToStep(JLabel label) {
		InsetPanel ip = (InsetPanel) label.getParent().getParent();
		return spec.hauptSequenz.findeSchritt(ip.getTextfeld().getTextComponent());
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		spec.getGlassPane().setVisible(false);
	}

	//sets Dummy depending on new Step oder dragged Step
	private void setDummy(MouseEvent e) {
		if(e.getSource() instanceof JLabel){
			AbstractSchrittView step = labelToStep((JLabel)e.getSource());
			dummy = new JTextField("Schritt "+step.getId().toString() );
		}else {
			dummy = new JTextField("Neuer Schritt");
		}
	}
}
