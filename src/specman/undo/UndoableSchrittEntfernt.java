package specman.undo;

import specman.SchrittSequenzView;
import specman.SchrittView;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoableSchrittEntfernt extends AbstractUndoableInteraktion {

	private final SchrittView schritt;
	private final SchrittSequenzView sequenz;
	private int schrittIndex;
	
	public UndoableSchrittEntfernt(SchrittView schritt, SchrittSequenzView sequenz, int schrittIndex) {
		this.schritt = schritt;
		this.sequenz = sequenz;
		this.schrittIndex = schrittIndex;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		sequenz.schrittHinzufuegen(schritt, schrittIndex);
	}

	@Override
	public void redo() throws CannotRedoException {
		schrittIndex = sequenz.schrittEntfernen(schritt);
	}

	@Override
	public String getPresentationName() {
		return "Schritt entfernt...";
	}

	@Override
	public String getUndoPresentationName() {
		return "Enfernten Schritt wieder hinzuf�gen";
	}

	@Override
	public String getRedoPresentationName() {
		return "Schritt erneut entfernen";
	}

}
