package specman.undo;

import specman.view.CaseSchrittView;
import specman.EditorI;
import specman.view.ZweigSchrittSequenzView;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoableZweigHinzugefuegt extends AbstractUndoableInteraktion {
	final ZweigSchrittSequenzView zweig;
	final CaseSchrittView caseSchritt;
	final EditorI editor;
	int zweigIndex;

	public UndoableZweigHinzugefuegt
		(EditorI editor, ZweigSchrittSequenzView zweig, CaseSchrittView caseSchritt) {
		this.editor = editor;
		this.caseSchritt = caseSchritt;
		this.zweig = zweig;
	}

	@Override
	public void undoEdit() throws CannotUndoException {
		zweigIndex = caseSchritt.zweigEntfernen(editor, zweig);
	}

	@Override
	public void redoEdit() throws CannotRedoException {
		caseSchritt.zweigHinzufuegen(editor, zweig, zweigIndex);
	}

}
