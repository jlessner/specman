package specman.undo;

import specman.EditException;
import specman.editarea.EditContainer;
import specman.editarea.TableEditArea;

import java.util.List;

public class UndoableTableRowRemoved extends AbstractUndoableInteraction {
  private final TableEditArea tableEditArea;
  private final int rowIndex;
  private final List<EditContainer> row;

  public UndoableTableRowRemoved(TableEditArea tableEditArea, int rowIndex, List<EditContainer> row) {
    this.tableEditArea = tableEditArea;
    this.rowIndex = rowIndex;
    this.row = row;
  }

  @Override
  protected void undoEdit() throws EditException {
    tableEditArea.addRow(rowIndex, row);
  }

  @Override
  protected void redoEdit() throws EditException {
    tableEditArea.removeRow(rowIndex);
  }
}
