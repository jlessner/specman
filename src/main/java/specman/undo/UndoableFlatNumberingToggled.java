package specman.undo;

import specman.EditException;
import specman.view.AbstractSchrittView;

import javax.swing.undo.UndoableEdit;

public class UndoableFlatNumberingToggled extends AbstractUndoableInteraction {
  private final AbstractSchrittView step;
  private boolean flatNumbering;

  public UndoableFlatNumberingToggled(AbstractSchrittView step, boolean flatNumbering) {
    this.step = step;
    this.flatNumbering = flatNumbering;
  }

  @Override
  protected void undoEdit() throws EditException {
    flatNumbering = !flatNumbering;
    step.toggleFlatNumbering(flatNumbering);
  }

  @Override
  protected void redoEdit() throws EditException {
    flatNumbering = !flatNumbering;
    step.toggleFlatNumbering(flatNumbering);
  }
}
