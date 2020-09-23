package specman.undo;

import specman.SpaltenResizer;
import specman.Specman;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * Eigentlich fragw�rdig, das Umstellen des Zoomfaktors in die Undo-Liste mit aufzunehmen. Es l�st aber auf einfache Weise
 * ein Problem: wenn man �ber den {@link SpaltenResizer} Spaltenbreiten �ndert, dann werden die Spaltengr��en mit den
 * Zoomfaktoren verrechnet, um die endg�ltige Spaltenbreite zu erhalten. Nur die Spaltenbreiten�nderungen r�ckg�ngig
 * machen, die Zoomver�nderungen aber nicht, f�hrt beim Undo zu Verzerrungen der Spaltenverh�ltnisse.
 * 
 * @author LESS02
 */
public class UndoableDiagrammSkaliert extends AbstractUndoableInteraktion {
	private final Specman editor;
	private int prozent;
	
	public UndoableDiagrammSkaliert(Specman editor, int prozentAlt) {
		this.editor = editor;
		this.prozent = prozentAlt;
	}

	@Override
	public void undo() throws CannotUndoException {
		prozent = editor.skalieren(prozent);
	}

	@Override
	public void redo() throws CannotRedoException {
		prozent = editor.skalieren(prozent);
	}
	
	

}
