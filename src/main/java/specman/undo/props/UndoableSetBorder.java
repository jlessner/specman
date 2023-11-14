package specman.undo.props;

import javax.swing.*;
import javax.swing.border.Border;

public class UndoableSetBorder extends UndoableSetProperty<Border> {

  public UndoableSetBorder(JComponent component, Border undoBorder) {
    super(undoBorder, component::setBorder, component::getBorder);
  }
}
