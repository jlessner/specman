package specman.undo.props;

import specman.editarea.SchrittNummerLabel;

public class UndoableSetDeletionCut extends UndoableSetProperty<Integer> {

  public UndoableSetDeletionCut(SchrittNummerLabel label, Integer undoDeletionCut) {
    super(undoDeletionCut, label::setDeletionCut, label::getDeletionCut);
  }
}
