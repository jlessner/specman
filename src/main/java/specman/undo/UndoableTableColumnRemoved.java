package specman.undo;

import specman.EditException;
import specman.editarea.EditContainer;
import specman.editarea.TableEditArea;

import java.util.List;

public class UndoableTableColumnRemoved extends AbstractUndoableInteraction {
  private final TableEditArea tableEditArea;
  private final int colIndex;
  private final List<EditContainer> column;
  private final List<Integer> originalColumnsWidthPercent;

  public UndoableTableColumnRemoved(TableEditArea tableEditArea, int colIndex, List<EditContainer> column, List<Integer> originalColumnsWidthPercent) {
    this.tableEditArea = tableEditArea;
    this.colIndex = colIndex;
    this.column = column;
    this.originalColumnsWidthPercent = originalColumnsWidthPercent;
  }

  @Override
  protected void undoEdit() throws EditException {
    tableEditArea.addColumn(colIndex, column, originalColumnsWidthPercent);
  }

  @Override
  protected void redoEdit() throws EditException {
    tableEditArea.removeColumn(colIndex);
  }
}
