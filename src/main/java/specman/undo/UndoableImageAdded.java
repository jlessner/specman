package specman.undo;

import specman.EditException;
import specman.Specman;
import specman.textfield.ImageEditArea;
import specman.textfield.TextEditArea;
import specman.textfield.TextfieldShef;

public class UndoableImageAdded extends AbstractUndoableInteraction {
  private final TextfieldShef editContainer;
  private final ImageEditArea imageArea;
  private final TextEditArea initiatingTextArea, cutOffTextArea;

  public UndoableImageAdded(TextfieldShef editContainer, TextEditArea initiatingTextArea, ImageEditArea imageArea, TextEditArea cutOffTextArea) {
    this.editContainer = editContainer;
    this.imageArea = imageArea;
    this.initiatingTextArea = initiatingTextArea;
    this.cutOffTextArea = cutOffTextArea;
  }

  @Override
  protected void undoEdit() throws EditException {
    editContainer.removeImageByUndoRedo(imageArea, cutOffTextArea);
  }

  @Override
  protected void redoEdit() throws EditException {
    editContainer.addImageByUndoRedo(initiatingTextArea, imageArea, cutOffTextArea);
    Specman.instance().diagrammAktualisieren(null);
  }
}
