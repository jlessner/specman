package specman.undo;

import specman.SchrittSequenzView;
import specman.SchrittView;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoableSchrittHinzugefuegt extends AbstractUndoableInteraktion {

	private final SchrittView schritt;
	private final SchrittSequenzView sequenz;
	private int schrittIndex;
	
	public UndoableSchrittHinzugefuegt(SchrittView schritt, SchrittSequenzView sequenz) {
		this.schritt = schritt;
		this.sequenz = sequenz;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		schrittIndex = sequenz.schrittEntfernen(schritt);
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
