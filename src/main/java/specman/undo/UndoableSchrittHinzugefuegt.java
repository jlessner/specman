package specman.undo;

import specman.EditException;
import specman.Specman;
import specman.view.SchrittSequenzView;
import specman.view.AbstractSchrittView;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoableSchrittHinzugefuegt extends AbstractUndoableInteraktion {

	private final AbstractSchrittView schritt;
	private final SchrittSequenzView sequenz;
	private int schrittIndex;
	
	public UndoableSchrittHinzugefuegt(AbstractSchrittView schritt, SchrittSequenzView sequenz) {
		this.schritt = schritt;
		this.sequenz = sequenz;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		try {
			schrittIndex = sequenz.schrittEntfernen(schritt);
		}
		catch(EditException ex) {
			Specman.instance().showError(ex);
			throw new CannotUndoException();
		}
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
	public void redo() throws CannotRedoException {
		sequenz.schrittHinzufuegen(schritt, schrittIndex);
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
