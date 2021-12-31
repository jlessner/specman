package specman.undo;

import specman.view.AbstractSchrittView;
import specman.view.SchrittSequenzView;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoableSchrittVerschoben extends AbstractUndoableInteraktion {
  protected final AbstractSchrittView step;
  protected SchrittSequenzView originalParent;
  protected int originalIndex;

  public UndoableSchrittVerschoben(AbstractSchrittView step, SchrittSequenzView originalParent, int originalIndex) {
    this.step = step;
    this.originalParent = originalParent;
    this.originalIndex = originalIndex;
  }

  @Override public void undo() throws CannotUndoException {
    togglePosition();
  }

  @Override public void redo() throws CannotRedoException {
    togglePosition();
  }

  protected void togglePosition() {
    SchrittSequenzView toggledOriginalParent = step.getParent();
    int toggledOriginalIndex = toggledOriginalParent.schrittEntfernen(step);
    step.setParent(originalParent);
    originalParent.schrittHinzufuegen(step, originalIndex);
    originalParent.renummerieren();
    originalParent = toggledOriginalParent;
    originalIndex = toggledOriginalIndex;
  }
}
