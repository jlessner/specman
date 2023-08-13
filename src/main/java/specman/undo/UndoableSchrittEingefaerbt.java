package specman.undo;

import specman.view.AbstractSchrittView;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;

public class UndoableSchrittEingefaerbt extends AbstractUndoableInteraction {
	private final AbstractSchrittView schritt;
	private final Color alteHintergrundfarbe;
	private final Color neueHintergrundfarbe;
	
	public UndoableSchrittEingefaerbt(AbstractSchrittView schritt, Color alteHintergrundfarbe, Color neueHintergrundfarbe) {
		this.schritt = schritt;
		this.alteHintergrundfarbe = alteHintergrundfarbe;
		this.neueHintergrundfarbe = neueHintergrundfarbe;
	}

	@Override
	public void undoEdit() throws CannotUndoException {
		schritt.setBackground(alteHintergrundfarbe);
	}

	@Override
	public void redoEdit() throws CannotRedoException {
		schritt.setBackground(neueHintergrundfarbe);
	}

	
}
