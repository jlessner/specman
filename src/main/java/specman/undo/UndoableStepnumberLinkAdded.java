package specman.undo;

import specman.EditException;
import specman.editarea.TextEditArea;
import specman.view.AbstractSchrittView;

public class UndoableStepnumberLinkAdded extends AbstractUndoableInteraction {
  private final AbstractSchrittView abstractSchrittView;
  private final TextEditArea textEditArea;

  public UndoableStepnumberLinkAdded(AbstractSchrittView abstractSchrittView, TextEditArea textEditArea) {
    if (abstractSchrittView == null || textEditArea == null) {
      throw new IllegalArgumentException(
              "A parameter is null and can not be used for undo/redo. Check if the caller got a valid value.");
    }
    this.abstractSchrittView = abstractSchrittView;
    this.textEditArea = textEditArea;
  }

  @Override
  protected void undoEdit() throws EditException {
    abstractSchrittView.unregisterStepnumberLink(textEditArea);
  }

  @Override
  protected void redoEdit() throws EditException {
    abstractSchrittView.registerStepnumberLink(textEditArea);
  }
}