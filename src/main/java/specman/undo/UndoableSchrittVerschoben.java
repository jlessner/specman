package specman.undo;

import specman.EditException;
import specman.Specman;
import specman.view.AbstractSchrittView;
import specman.view.SchrittSequenzView;

import static specman.view.StepRemovalPurpose.Move;

public class UndoableSchrittVerschoben extends AbstractUndoableInteraction {
  protected final AbstractSchrittView step;
  protected SchrittSequenzView originalParent;
  protected int originalIndex;

  public UndoableSchrittVerschoben(AbstractSchrittView step, SchrittSequenzView originalParent, int originalIndex) {
    this.step = step;
    this.originalParent = originalParent;
    this.originalIndex = originalIndex;
  }

  @Override public void undoEdit() throws EditException {
    togglePosition();
    Specman.instance().resyncStepnumberStyle();
  }

  @Override public void redoEdit() throws EditException {
    togglePosition();
    Specman.instance().resyncStepnumberStyle();
  }

  protected void togglePosition() throws EditException {
    SchrittSequenzView toggledOriginalParent = step.getParent();
    int toggledOriginalIndex = toggledOriginalParent.schrittEntfernen(step, Move);
    step.setParent(originalParent);
    originalParent.schrittHinzufuegen(step, originalIndex);
    originalParent.renummerieren();
    originalParent = toggledOriginalParent;
    originalIndex = toggledOriginalIndex;
  }
}
