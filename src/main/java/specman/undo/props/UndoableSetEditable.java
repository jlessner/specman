package specman.undo.props;

import specman.Aenderungsart;
import specman.view.AbstractSchrittView;

import javax.swing.text.JTextComponent;

public class UndoableSetEditable extends UndoableSetProperty<Boolean> {

  public UndoableSetEditable(JTextComponent component, boolean undoEditable) {
    super(undoEditable, component::setEditable, component::isEditable);
  }
}
