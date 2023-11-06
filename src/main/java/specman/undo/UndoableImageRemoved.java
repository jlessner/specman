package specman.undo;

import specman.EditException;
import specman.Specman;
import specman.editarea.ImageEditArea;
import specman.editarea.TextEditArea;
import specman.editarea.EditContainer;

public class UndoableImageRemoved extends AbstractUndoableInteraction {
  private final EditContainer editContainer;
  private final ImageEditArea imageArea;
  private final TextEditArea leadingTextArea, trailingTextArea;

  public UndoableImageRemoved(EditContainer editContainer, TextEditArea leadingTextArea, ImageEditArea imageArea, TextEditArea trailingTextArea) {
    this.editContainer = editContainer;
    this.imageArea = imageArea;
    this.leadingTextArea = leadingTextArea;
    this.trailingTextArea = trailingTextArea;
  }

  @Override
  protected void undoEdit() throws EditException {
    editContainer.addImageByUndoRedo(leadingTextArea, imageArea, trailingTextArea);
    Specman.instance().diagrammAktualisieren(null);
  }

  @Override
  protected void redoEdit() throws EditException {
    editContainer.removeImageByUndoRedo(imageArea, trailingTextArea);
  }
}
