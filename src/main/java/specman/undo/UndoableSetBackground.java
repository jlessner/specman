package specman.undo;

import java.awt.*;

public class UndoableSetBackground extends UndoableSetProperty<Color> {

  public UndoableSetBackground(Component component, Color undoColor) {
    super(undoColor, component::setBackground, component::getBackground);
  }
}
