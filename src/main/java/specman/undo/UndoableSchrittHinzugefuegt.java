package specman.undo;

import specman.EditException;
import specman.Specman;
import specman.view.SchrittSequenzView;
import specman.view.AbstractSchrittView;

import javax.swing.undo.CannotRedoException;

import static specman.view.StepRemovalPurpose.Discard;

public class UndoableSchrittHinzugefuegt extends AbstractUndoableInteraction {

	private final AbstractSchrittView schritt;
	private final SchrittSequenzView sequenz;
	private int schrittIndex;
	
	public UndoableSchrittHinzugefuegt(AbstractSchrittView schritt, SchrittSequenzView sequenz) {
		this.schritt = schritt;
		this.sequenz = sequenz;
	}
	
	@Override
	public void undoEdit() throws EditException {
		schrittIndex = sequenz.schrittEntfernen(schritt, Discard);
    Specman.instance().resyncStepnumberStyle();
	}

	@Override public boolean canUndo() {
		try {
			sequenz.checkSchrittEntfernen(schritt);
			return true;
		}
		catch(EditException ex) {
			return false;
		}
	}

	@Override
	public void redoEdit() throws CannotRedoException {
		sequenz.schrittHinzufuegen(schritt, schrittIndex);
    Specman.instance().resyncStepnumberStyle();
	}

	@Override
	public String getPresentationName() {
		return "Neuer Schritt...";
	}

	@Override
	public String getUndoPresentationName() {
		return "Neuen Schritt entfernen";
	}

	@Override
	public String getRedoPresentationName() {
		return "Neuen Schritt wieder hinzuf�gen";
	}

}
