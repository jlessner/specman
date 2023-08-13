package specman.undo;

import specman.EditException;
import specman.Specman;
import specman.textfield.ImageEditArea;
import specman.textfield.TextfieldShef;

public class UndoableImageAdded extends AbstractUndoableInteraction {
  private final TextfieldShef editContainer;
  private final ImageEditArea imageArea;
  private final int initiatingSplitPosition;

  public UndoableImageAdded(TextfieldShef editContainer, ImageEditArea imageArea, int initiatingSplitPosition) {
    this.editContainer = editContainer;
    this.imageArea = imageArea;
    this.initiatingSplitPosition = initiatingSplitPosition;
  }

  @Override
  protected void undoEdit() throws EditException {
    editContainer.removeImage(imageArea);
    Specman.instance().diagrammAktualisieren(null);
  }

  @Override
  protected void redoEdit() throws EditException {

  }
}
