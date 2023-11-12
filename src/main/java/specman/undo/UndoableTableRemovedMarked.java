package specman.undo;

import specman.Aenderungsart;
import specman.EditException;
import specman.editarea.TableEditArea;

public class UndoableTableRemovedMarked extends AbstractUndoableInteraction {
  private final TableEditArea tableEditArea;

  public UndoableTableRemovedMarked(TableEditArea tableEditArea) {
    this.tableEditArea = tableEditArea;
  }

  @Override
  protected void undoEdit() throws EditException {
    tableEditArea.undoGeloeschtMarkiertStil();
  }

  @Override
  protected void redoEdit() throws EditException {
    tableEditArea.removeTableOrMarkAsDeleted();
  }
}
