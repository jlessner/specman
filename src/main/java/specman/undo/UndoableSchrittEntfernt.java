package specman.undo;

import specman.EditException;
import specman.view.SchrittSequenzView;
import specman.view.AbstractSchrittView;

import javax.swing.undo.CannotUndoException;

public class UndoableSchrittEntfernt extends AbstractUndoableInteraction {

	private final AbstractSchrittView schritt;
	private final SchrittSequenzView sequenz;
	private int schrittIndex;
	
	public UndoableSchrittEntfernt(AbstractSchrittView schritt, SchrittSequenzView sequenz, int schrittIndex) {
		this.schritt = schritt;
		this.sequenz = sequenz;
		this.schrittIndex = schrittIndex;
	}
	
	@Override
	public void undoEdit() throws CannotUndoException {
		sequenz.schrittHinzufuegen(schritt, schrittIndex);
	}

	@Override
	public void redoEdit() throws EditException {
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
