package specman.undo;

import specman.EditException;
import specman.view.BreakSchrittView;
import specman.view.CatchSchrittSequenzView;
import specman.view.CatchUeberschrift;

import javax.swing.undo.UndoableEdit;

public class UndoableCoCatchRemoved extends AbstractUndoableInteraction {
  private CatchSchrittSequenzView catchSchrittSequenzView;
  private BreakSchrittView breakStep;
  private int deletionIndex;
  private CatchUeberschrift coCatchHeading;

  public UndoableCoCatchRemoved(CatchSchrittSequenzView catchSchrittSequenzView, BreakSchrittView breakStep, CatchUeberschrift catchHeading, int deletionIndex) {
    this.catchSchrittSequenzView = catchSchrittSequenzView;
    this.coCatchHeading = catchHeading;
    this.deletionIndex = deletionIndex;
    this.breakStep = breakStep;
  }

  @Override
  protected void undoEdit() throws EditException {
    catchSchrittSequenzView.addCoCatchForUndo(deletionIndex, coCatchHeading, breakStep);
  }

  @Override
  protected void redoEdit() throws EditException {
    catchSchrittSequenzView.removeUDBL(coCatchHeading);
  }
}
