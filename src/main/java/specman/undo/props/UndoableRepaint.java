package specman.undo.props;

import specman.EditException;
import specman.undo.AbstractUndoableInteraction;

import java.awt.*;

/** This class is required when an undoable composite operation requires a
 * repaint of a component. Undoing the operation usually requires a repaint
 * too, to update the UI correctly. */
public class UndoableRepaint extends AbstractUndoableInteraction {
  private final Component component;

  public UndoableRepaint(Component component) {
    this.component = component;
  }

  @Override
  protected void undoEdit() throws EditException {
    component.repaint();
  }

  @Override
  protected void redoEdit() throws EditException {
    component.repaint();
  }
}
