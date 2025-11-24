package specman.undo;

import specman.EditException;
import specman.view.CatchSchrittSequenzView;

import java.util.List;

public class UndoableCatchSequenceRemoved extends AbstractUndoableInteraction {
  private final CatchSchrittSequenzView catchSequence;
  private final int catchIndex;
  private final List<Integer> backupSequencesWidthPercent;

  public UndoableCatchSequenceRemoved(CatchSchrittSequenzView catchSequence, int catchIndex, List<Integer> backupSequencesWidthPercent) {
    this.catchSequence = catchSequence;
    this.catchIndex = catchIndex;
    this.backupSequencesWidthPercent = backupSequencesWidthPercent;
  }

  @Override
  protected void undoEdit() throws EditException {
    catchSequence.getParent().addCatchSequence(catchSequence, catchIndex, backupSequencesWidthPercent);
    catchSequence.reconnectToBreakstep();
  }

  @Override
  protected void redoEdit() throws EditException {
    catchSequence.removeOrMarkAsDeletedUDBL();
  }
}
