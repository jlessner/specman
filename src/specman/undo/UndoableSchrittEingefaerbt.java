package specman.undo;

import specman.SchrittView;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;

public class UndoableSchrittEingefaerbt extends AbstractUndoableInteraktion {
	private final SchrittView schritt;
	private final Color alteHintergrundfarbe;
	private final Color neueHintergrundfarbe;
	
	public UndoableSchrittEingefaerbt(SchrittView schritt, Color alteHintergrundfarbe, Color neueHintergrundfarbe) {
		this.schritt = schritt;
		this.alteHintergrundfarbe = alteHintergrundfarbe;
		this.neueHintergrundfarbe = neueHintergrundfarbe;
	}

	@Override
	public void undo() throws CannotUndoException {
		schritt.setBackground(alteHintergrundfarbe);
	}

	@Override
	public void redo() throws CannotRedoException {
		schritt.setBackground(neueHintergrundfarbe);
	}

	
}
