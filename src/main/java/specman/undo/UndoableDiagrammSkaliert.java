package specman.undo;

import specman.SpaltenResizer;
import specman.Specman;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * Eigentlich fragwürdig, das Umstellen des Zoomfaktors in die Undo-Liste mit aufzunehmen. Es löst aber auf einfache Weise
 * ein Problem: wenn man über den {@link SpaltenResizer} Spaltenbreiten ändert, dann werden die Spaltengrößen mit den
 * Zoomfaktoren verrechnet, um die endgültige Spaltenbreite zu erhalten. Nur die Spaltenbreitenänderungen rückgängig
 * machen, die Zoomveränderungen aber nicht, führt beim Undo zu Verzerrungen der Spaltenverhältnisse.
 * 
 * @author LESS02
 */
public class UndoableDiagrammSkaliert extends AbstractUndoableInteraction {
	private final Specman editor;
	private int prozent;
	
	public UndoableDiagrammSkaliert(Specman editor, int prozentAlt) {
		this.editor = editor;
		this.prozent = prozentAlt;
	}

	@Override
	public void undoEdit() throws CannotUndoException {
		prozent = editor.skalieren(prozent);
	}

	@Override
	public void redoEdit() throws CannotRedoException {
		prozent = editor.skalieren(prozent);
	}
}
