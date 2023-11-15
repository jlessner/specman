package specman.undo;

import specman.EditException;
import specman.EditorI;
import specman.view.CaseSchrittView;
import specman.view.ZweigSchrittSequenzView;

import javax.swing.undo.CannotRedoException;

public class UndoableZweigEntferntMarkiert extends AbstractUndoableInteraction {
  final ZweigSchrittSequenzView zweig;
  final CaseSchrittView caseSchritt;
  final EditorI editor;

  public UndoableZweigEntferntMarkiert
      (EditorI editor, ZweigSchrittSequenzView zweig, CaseSchrittView caseSchritt) {
    this.editor = editor;
    this.caseSchritt = caseSchritt;
    this.zweig = zweig;
  }

  @Override public void undoEdit() throws EditException {
    zweig.aenderungenVerwerfen(editor);
  }

  @Override public void redoEdit() throws CannotRedoException {
    zweig.alsGeloeschtMarkierenUDBL(editor);
  }
}
