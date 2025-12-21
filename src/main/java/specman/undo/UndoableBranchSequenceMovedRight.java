package specman.undo;

import specman.EditException;
import specman.editarea.InteractiveStepFragment;
import specman.modelops.MoveBranchSequenceLeftOperation;
import specman.modelops.MoveBranchSequenceRightOperation;
import specman.view.AbstractSchrittView;

public class UndoableBranchSequenceMovedRight extends AbstractUndoableInteraction {
  private final AbstractSchrittView step;
  private final InteractiveStepFragment initiatingFragment;

  public UndoableBranchSequenceMovedRight(AbstractSchrittView step, InteractiveStepFragment initiatingFragment) {
    this.step = step;
    this.initiatingFragment = initiatingFragment;
  }

  @Override
  protected void undoEdit() throws EditException {
    new MoveBranchSequenceLeftOperation(step, initiatingFragment).execute();
  }

  @Override
  protected void redoEdit() throws EditException {
    new MoveBranchSequenceRightOperation(step, initiatingFragment).execute();
  }
}
