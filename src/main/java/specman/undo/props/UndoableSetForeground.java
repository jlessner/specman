package specman.undo.props;

import java.awt.*;

public class UndoableSetForeground extends UndoableSetProperty<Color> {

  public UndoableSetForeground(Component component, Color undoColor) {
    super(undoColor, component::setForeground, component::getForeground);
  }
}
