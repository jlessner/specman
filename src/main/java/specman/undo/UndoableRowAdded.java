package specman.undo;

import specman.EditException;
import specman.editarea.TableEditArea;

public class UndoableRowAdded extends AbstractUndoableInteraction {
  private final TableEditArea tableEditArea;
  private final int rowIndex;

  public UndoableRowAdded(TableEditArea tableEditArea, int rowIndex) {
    this.tableEditArea = tableEditArea;
    this.rowIndex = rowIndex;
  }

  @Override
  protected void undoEdit() throws EditException {
    tableEditArea.removeRowByUndoRedo(rowIndex);
  }

  @Override
  protected void redoEdit() throws EditException {
    tableEditArea.addEmptyRowWithoutUndoRecording(rowIndex);
  }
}
