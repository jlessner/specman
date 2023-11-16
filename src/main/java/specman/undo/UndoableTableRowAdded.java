package specman.undo;

import specman.EditException;
import specman.editarea.EditContainer;
import specman.editarea.TableEditArea;

import java.util.List;

public class UndoableTableRowAdded extends AbstractUndoableInteraction {
  private final TableEditArea tableEditArea;
  private final int rowIndex;
  private final List<EditContainer> row;

  public UndoableTableRowAdded(TableEditArea tableEditArea, int rowIndex, List<EditContainer> row) {
    this.tableEditArea = tableEditArea;
    this.rowIndex = rowIndex;
    this.row = row;
  }

  @Override
  protected void undoEdit() throws EditException {
    tableEditArea.removeRow(rowIndex);
  }

  @Override
  protected void redoEdit() throws EditException {
    tableEditArea.addRow(rowIndex, row);
  }
}
