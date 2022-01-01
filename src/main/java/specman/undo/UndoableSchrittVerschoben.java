package specman.undo;

import specman.EditException;
import specman.Specman;
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
    try {
      togglePosition();
    }
    catch(EditException ex) {
      Specman.instance().showError(ex);
      throw new CannotUndoException();
    }
  }

  @Override public void redo() throws CannotRedoException {
    try {
      togglePosition();
    }
    catch(EditException ex) {
      Specman.instance().showError(ex);
      throw new CannotRedoException();
    }
  }

  protected void togglePosition() throws EditException {
    SchrittSequenzView toggledOriginalParent = step.getParent();
    int toggledOriginalIndex = toggledOriginalParent.schrittEntfernen(step);
    step.setParent(originalParent);
    originalParent.schrittHinzufuegen(step, originalIndex);
    originalParent.renummerieren();
    originalParent = toggledOriginalParent;
    originalIndex = toggledOriginalIndex;
  }
}
