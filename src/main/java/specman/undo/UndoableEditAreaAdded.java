package specman.undo;

import specman.EditException;
import specman.Specman;
import specman.editarea.EditArea;
import specman.editarea.EditContainer;
import specman.editarea.TableEditArea;
import specman.editarea.TextEditArea;

public class UndoableEditAreaAdded extends AbstractUndoableInteraction {
  private final EditContainer editContainer;
  private final EditArea editArea;
  private final TextEditArea initiatingTextArea, cutOffTextArea;

  public UndoableEditAreaAdded(EditContainer editContainer, TextEditArea initiatingTextArea, EditArea editArea, TextEditArea cutOffTextArea) {
    this.editContainer = editContainer;
    this.editArea = editArea;
    this.initiatingTextArea = initiatingTextArea;
    this.cutOffTextArea = cutOffTextArea;
  }

  @Override
  protected void undoEdit() throws EditException {
    editContainer.removeEditAreaByUndoRedo(editArea, cutOffTextArea);
    Specman.instance().diagrammAktualisieren(null);
  }

  @Override
  protected void redoEdit() throws EditException {
    editContainer.addEditAreaByUndoRedo(initiatingTextArea, editArea, cutOffTextArea);
    Specman.instance().diagrammAktualisieren(null);
  }
}
