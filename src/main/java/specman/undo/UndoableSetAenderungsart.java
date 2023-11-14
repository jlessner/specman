package specman.undo;

import specman.Aenderungsart;
import specman.view.AbstractSchrittView;

public class UndoableSetAenderungsart extends UndoableSetProperty<Aenderungsart> {

  public UndoableSetAenderungsart(AbstractSchrittView schrittView, Aenderungsart undoAenderungsart) {
    super(undoAenderungsart, schrittView::setAenderungsart, schrittView::getAenderungsart);
  }
}
