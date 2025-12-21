package specman.modelops;

import specman.EditException;
import specman.EditorI;
import specman.Specman;
import specman.editarea.InteractiveStepFragment;
import specman.undo.UndoableBranchSequenceMovedLeft;
import specman.view.AbstractSchrittView;
import specman.view.CatchBereich;

public class MoveBranchSequenceLeftOperation {
  private final AbstractSchrittView step;
  private final InteractiveStepFragment initiatingFragment;
  private final EditorI editor;

  public MoveBranchSequenceLeftOperation(AbstractSchrittView step, InteractiveStepFragment initiatingFragment) {
    this.step = step;
    this.initiatingFragment = initiatingFragment;
    this.editor = Specman.instance();
  }

  /** Moving catch sequences right or left is not considered as a change being reflected in change recording. */
  public void execute() throws EditException {
    if (step instanceof CatchBereich) {
      CatchBereich catchBereich = (CatchBereich) step;
      catchBereich.moveCatchSequenceLeft(initiatingFragment);
    }
    else {
      // Check if the initiatingFragment addresses a branch sequence inside some other multi-branch step
      // Move this sequence in there
    }
    editor.addEdit(new UndoableBranchSequenceMovedLeft(step, initiatingFragment));
  }

}
