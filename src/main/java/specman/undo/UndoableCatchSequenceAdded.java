package specman.undo;

import specman.EditException;
import specman.view.CatchSchrittSequenzView;

import java.util.List;

public class UndoableCatchSequenceAdded extends AbstractUndoableInteraction {
  private final CatchSchrittSequenzView catchSequence;
  private final List<Integer> originalSequencesWidthPercents;

  public UndoableCatchSequenceAdded(CatchSchrittSequenzView catchSequence, List<Integer> originalSequencesWidthPercents) {
    this.catchSequence = catchSequence;
    this.originalSequencesWidthPercents = originalSequencesWidthPercents;
  }

  @Override
  protected void undoEdit() throws EditException {
    catchSequence.removeUDBL();
  }

  @Override
  protected void redoEdit() throws EditException {
    catchSequence.getParent().addCatchSequence(catchSequence, null, null);
    catchSequence.reconnectToBreakstep();
  }
}
