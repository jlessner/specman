package specman.undo.props;

import specman.Aenderungsart;
import specman.editarea.EditArea;

public class UndoableSetAenderungsartEditArea extends UndoableSetProperty<Aenderungsart> {

  public UndoableSetAenderungsartEditArea(EditArea editArea, Aenderungsart undoAenderungsart) {
    super(undoAenderungsart, editArea::setAenderungsart, editArea::getAenderungsart);
  }
}
