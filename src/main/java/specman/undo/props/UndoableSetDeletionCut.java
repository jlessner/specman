package specman.undo.props;

import specman.editarea.stepnumberlabel.StepnumberLabel;

public class UndoableSetDeletionCut extends UndoableSetProperty<Integer> {

  public UndoableSetDeletionCut(StepnumberLabel label, Integer undoDeletionCut) {
    super(undoDeletionCut, label::setDeletionCut, label::getDeletionCut);
  }
}
