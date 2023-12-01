package specman.undo;

import specman.EditException;
import specman.view.CatchSchrittSequenzView;

public class UndoableCatchSequenceRemoved extends AbstractUndoableInteraction {
  private final CatchSchrittSequenzView catchSequence;
  private final int catchIndex;

  public UndoableCatchSequenceRemoved(CatchSchrittSequenzView catchSequence, int catchIndex) {
    this.catchSequence = catchSequence;
    this.catchIndex = catchIndex;
  }

  @Override
  protected void undoEdit() throws EditException {
    catchSequence.getParent().addCatchSequence(catchSequence, catchIndex);
  }

  @Override
  protected void redoEdit() throws EditException {
    catchSequence.removeOrMarkAsDeletedUDBL();
  }
}
