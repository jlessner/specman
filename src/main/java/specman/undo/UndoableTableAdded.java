package specman.undo;

import specman.EditException;
import specman.Specman;
import specman.editarea.EditContainer;
import specman.editarea.ImageEditArea;
import specman.editarea.TableEditArea;
import specman.editarea.TextEditArea;

public class UndoableTableAdded extends AbstractUndoableInteraction {
  private final EditContainer editContainer;
  private final TableEditArea tableArea;
  private final TextEditArea initiatingTextArea, cutOffTextArea;

  public UndoableTableAdded(EditContainer editContainer, TextEditArea initiatingTextArea, TableEditArea tableArea, TextEditArea cutOffTextArea) {
    this.editContainer = editContainer;
    this.tableArea = tableArea;
    this.initiatingTextArea = initiatingTextArea;
    this.cutOffTextArea = cutOffTextArea;
  }

  @Override
  protected void undoEdit() throws EditException {
    editContainer.removeTableByUndoRedo(tableArea, cutOffTextArea);
    Specman.instance().diagrammAktualisieren(null);
  }

  @Override
  protected void redoEdit() throws EditException {
    editContainer.addTableByUndoRedo(initiatingTextArea, tableArea, cutOffTextArea);
    Specman.instance().diagrammAktualisieren(null);
  }
}
