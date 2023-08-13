package specman.undo;

import specman.EditException;
import specman.Specman;
import specman.textfield.ImageEditArea;
import specman.textfield.TextEditArea;
import specman.textfield.TextfieldShef;

public class UndoableImageRemoved extends AbstractUndoableInteraction {
  private final TextfieldShef editContainer;
  private final ImageEditArea imageArea;
  private final TextEditArea leadingTextArea, trailingTextArea;

  public UndoableImageRemoved(TextfieldShef editContainer, TextEditArea leadingTextArea, ImageEditArea imageArea, TextEditArea trailingTextArea) {
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
