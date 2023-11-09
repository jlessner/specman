package specman.undo;

import specman.EditException;
import specman.editarea.TableEditArea;

public class UndoableTableRowAdded extends AbstractUndoableInteraction {
  private final TableEditArea tableEditArea;
  private final int rowIndex;

  public UndoableTableRowAdded(TableEditArea tableEditArea, int rowIndex) {
    this.tableEditArea = tableEditArea;
    this.rowIndex = rowIndex;
  }

  @Override
  protected void undoEdit() throws EditException {
    tableEditArea.removeRowWithoutUndoRecording(rowIndex);
  }

  @Override
  protected void redoEdit() throws EditException {
    tableEditArea.addEmptyRowWithoutUndoRecording(rowIndex);
  }
}
