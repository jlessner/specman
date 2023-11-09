package specman.undo;

import specman.EditException;
import specman.editarea.TableEditArea;

import java.util.List;

public class UndoableTableColumnAdded extends AbstractUndoableInteraction {
  private final TableEditArea tableEditArea;
  private final int colIndex;
  private final List<Integer> originalColumnsWidthPercent;

  public UndoableTableColumnAdded(TableEditArea tableEditArea, int colIndex, List<Integer> originalColumnsWidthPercent) {
    this.tableEditArea = tableEditArea;
    this.colIndex = colIndex;
    this.originalColumnsWidthPercent = originalColumnsWidthPercent;
  }

  @Override
  protected void undoEdit() throws EditException {
    tableEditArea.removeColumnWithoutUndoRecording(colIndex, originalColumnsWidthPercent);
  }

  @Override
  protected void redoEdit() throws EditException {
    tableEditArea.addEmptyColumnWithoutUndoRecording(colIndex);
  }
}
