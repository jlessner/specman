package specman.undo;

import specman.EditException;
import specman.EditorI;
import specman.view.CaseSchrittView;
import specman.view.ZweigSchrittSequenzView;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoableZweigEntferntMarkiert extends AbstractUndoableInteraktion {
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
    try {
      editor.pauseUndoRecording();
      zweig.aenderungenVerwerfen(editor);
    }
    finally {
      editor.resumeUndoRecording();
    }
  }

  @Override public void redoEdit() throws CannotRedoException {
    try {
      editor.pauseUndoRecording();
      zweig.alsGeloeschtMarkieren(editor);
    }
    finally {
      editor.resumeUndoRecording();
    }
  }
}
