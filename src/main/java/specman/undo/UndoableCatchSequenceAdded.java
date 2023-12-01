package specman.undo;

import specman.EditException;
import specman.view.CatchSchrittSequenzView;

public class UndoableCatchSequenceAdded extends AbstractUndoableInteraction {
  private final CatchSchrittSequenzView catchSequence;

  public UndoableCatchSequenceAdded(CatchSchrittSequenzView catchSequence) {
    this.catchSequence = catchSequence;
  }

  @Override
  protected void undoEdit() throws EditException {
    catchSequence.removeOrMarkAsDeletedUDBL();
  }

  @Override
  protected void redoEdit() throws EditException {
    catchSequence.getParent().addCatchSequence(catchSequence, null);
  }
}
