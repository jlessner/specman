package specman.undo;

import specman.CaseSchrittView;
import specman.EditorI;
import specman.ZweigSchrittSequenzView;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoableZweigEntfernt extends AbstractUndoableInteraktion {
	final ZweigSchrittSequenzView zweig;
	final CaseSchrittView caseSchritt;
	final int zweigIndex;
	final EditorI editor;
	
	public UndoableZweigEntfernt(EditorI editor, ZweigSchrittSequenzView zweig, CaseSchrittView caseSchritt, int zweigIndex) {
		this.zweig = zweig;
		this.caseSchritt = caseSchritt;
		this.zweigIndex = zweigIndex;
		this.editor = editor;
	}

	@Override
	public void undo() throws CannotUndoException {
		caseSchritt.zweigHinzufuegen(editor, zweig, zweigIndex);
	}

	@Override
	public void redo() throws CannotRedoException {
		caseSchritt.zweigEntfernen(editor, zweig);
	}

	
}
