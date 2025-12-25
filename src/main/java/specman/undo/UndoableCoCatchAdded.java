package specman.undo;

import specman.EditException;
import specman.view.BreakSchrittView;
import specman.view.CatchSchrittSequenzView;
import specman.view.CatchUeberschrift;

public class UndoableCoCatchAdded extends AbstractUndoableInteraction {

  private CatchSchrittSequenzView catchSchrittSequenzView;
  private BreakSchrittView breakStep;
  private int insertionIndex;
  private CatchUeberschrift coCatchHeading;

  public UndoableCoCatchAdded(CatchSchrittSequenzView catchSchrittSequenzView, BreakSchrittView breakStep, int insertionIndex, CatchUeberschrift coCatchHeading) {
    this.catchSchrittSequenzView = catchSchrittSequenzView;
    this.breakStep = breakStep;
    this.insertionIndex = insertionIndex;
    this.coCatchHeading = coCatchHeading;
  }

  @Override
  protected void undoEdit() throws EditException {
    catchSchrittSequenzView.removeUDBL(coCatchHeading);
  }

  @Override
  protected void redoEdit() throws EditException {
    catchSchrittSequenzView.addCoCatchUDBL(insertionIndex, coCatchHeading, breakStep);
  }
}
