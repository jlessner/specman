package specman.undo;

import specman.EditorI;
import specman.view.AbstractSchrittView;
import specman.view.SchrittSequenzView;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoableToggleStepBorder extends AbstractUndoableInteraktion {
  private final EditorI editor;
  private final AbstractSchrittView schritt;
  private final SchrittSequenzView sequenz;

  public UndoableToggleStepBorder(EditorI editor, AbstractSchrittView schritt, SchrittSequenzView sequenz) {
    this.editor = editor;
    this.schritt = schritt;
    this.sequenz = sequenz;
  }

  @Override
  public void undoEdit() throws CannotUndoException {
    sequenz.toggleBorderType(schritt);
    editor.diagrammAktualisieren(schritt);
  }

  @Override
  public void redoEdit() throws CannotRedoException {
    sequenz.toggleBorderType(schritt);
    editor.diagrammAktualisieren(schritt);
  }

  @Override
  public String getPresentationName() {
    return "Schrittrahmen umgestellt...";
  }

  @Override
  public String getUndoPresentationName() {
    return "Schrittrahmen zurückstellen";
  }

  @Override
  public String getRedoPresentationName() {
    return "Schrittrahmen wieder herstellen";
  }
}
