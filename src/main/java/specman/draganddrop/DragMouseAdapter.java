package specman.draganddrop;

import specman.Aenderungsart;
import specman.EditException;
import specman.Specman;
import specman.editarea.InteractiveStepFragment;
import specman.editarea.StepnumberLabel;
import specman.view.AbstractSchrittView;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import static specman.draganddrop.InsertDecision.Insert;
import static specman.draganddrop.InsertDecision.NoInsert;


public class DragMouseAdapter extends MouseAdapter {
	private final Specman specman;
	private	JTextField dummy;
	private final DraggingLogic draggingLogic;

	public DragMouseAdapter(Specman specman) {
		this.specman = specman;
		this.draggingLogic = new DraggingLogic(specman);
	}

	public void mousePressed(MouseEvent e) {
		setDummy(e);
		dummy.setBounds(new Rectangle(150, 15));
	}

	public void mouseDragged(MouseEvent e) {
		try {
			if(checkEinzigerSchritt(e)){
				draggingLogic.showInvalidCursor();
				return;
			}
			if(checkGeloeschterSchritt(e) || checkQuellSchritt(e)){
				draggingLogic.showInvalidCursor();
				return;
			}
			Point pt = e.getPoint();
			JComponent parent = (JComponent) e.getComponent();
			Point ptCon = SwingUtilities.convertPoint((Component)e.getSource(),(int) e.getPoint().getX(),(int)e.getPoint().getY(), specman);
			if(!specman.getGlassPane().isVisible() ) {
				specman.setCursor(Cursor.getDefaultCursor());
			}
			specman.getGlassPane().setVisible(false);
			specman.window.add(dummy);
			specman.window.pack();
			updateWindowLocation(pt, parent);
			specman.window.setVisible(true);
			draggingLogic.dragGlassPanePos(ptCon, specman.hauptSequenz.schritte,NoInsert,e);
		}
		catch(EditException ex) {
			specman.showError(ex);
		}
	}

	//Updates the Window Location
	private void updateWindowLocation(Point pt, JComponent parent){
		Point p = new Point(pt.x  +3, pt.y +3);
		SwingUtilities.convertPointToScreen(p,parent);
		specman.window.setLocation(p);
	}

	public void mouseReleased(MouseEvent e) {
		try {
			if(checkEinzigerSchritt(e)){
				specman.setCursor(Cursor.getDefaultCursor());
				return;
			}
			if(checkGeloeschterSchritt(e) || checkQuellSchritt(e)){
				specman.setCursor(Cursor.getDefaultCursor());
				return;
			}
			specman.window.setVisible(false);
			Point ptCon = SwingUtilities.convertPoint((Component)e.getSource(),(int) e.getPoint().getX(),(int)e.getPoint().getY()-2, specman);
			draggingLogic.dragGlassPanePos(ptCon, specman.hauptSequenz.schritte,Insert,e);
			specman.getGlassPane().setVisible(false);
			specman.setCursor(Cursor.getDefaultCursor());
			specman.window.remove(dummy);
		}
		catch(EditException ex) {
			specman.showError(ex);
		}
	}


	public void mouseEntered(MouseEvent e) {
		if(e.getSource() instanceof StepnumberLabel) {
			if(checkEinzigerSchritt(e)) {
				draggingLogic.showInvalidCursor();
			}
			if(checkGeloeschterSchritt(e) || checkQuellSchritt(e)){
				draggingLogic.showInvalidCursor();
			}
		}

	}

	public void mouseExited(MouseEvent e) {
		specman.setCursor(Cursor.getDefaultCursor());
	}

	//Letzter Schritt darf nicht verschoben werden
	private boolean checkEinzigerSchritt(MouseEvent e) {
		if(e.getSource() instanceof InteractiveStepFragment){
			int counter = 0;
			AbstractSchrittView step = labelToStep( (InteractiveStepFragment) e.getSource());
			for(AbstractSchrittView Schritt: step.getParent().schritte) {
				if(!(Schritt.getAenderungsart() == Aenderungsart.Geloescht || Schritt.getAenderungsart() == Aenderungsart.Quellschritt)) {
					counter++;
				}
			}
			return counter <= 1;
		}
		return false;
	}

	//gelöschter Schritt darf nicht verschoben werden
	private boolean checkGeloeschterSchritt(MouseEvent e){
		if(e.getSource() instanceof InteractiveStepFragment){
			AbstractSchrittView step = labelToStep( (InteractiveStepFragment) e.getSource());
			return step.getAenderungsart()== Aenderungsart.Geloescht;
		}
		return false;
	}

	private boolean checkQuellSchritt(MouseEvent e){
		if(e.getSource() instanceof InteractiveStepFragment){
			AbstractSchrittView step = labelToStep( (InteractiveStepFragment) e.getSource());
			return step.getAenderungsart() == Aenderungsart.Quellschritt;
		}
		return false;
	}


	private AbstractSchrittView labelToStep(InteractiveStepFragment label) {
		return specman.hauptSequenz.findeSchritt(label);
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		specman.getGlassPane().setVisible(false);
	}

	//sets Dummy depending on new Step oder dragged Step
	private void setDummy(MouseEvent e) {
		if(e.getSource() instanceof InteractiveStepFragment){
			AbstractSchrittView step = labelToStep((InteractiveStepFragment)e.getSource());
			dummy = new JTextField("Schritt "+step.getId().toString() );
		}else {
			dummy = new JTextField("Neuer Schritt");
		}
	}
}