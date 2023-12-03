package specman.undo.props;

import specman.Aenderungsart;
import specman.editarea.EditArea;
import specman.view.SchrittSequenzView;

public class UndoableSetAenderungsartSchrittSequenzView extends UndoableSetProperty<Aenderungsart> {

  public UndoableSetAenderungsartSchrittSequenzView(SchrittSequenzView schrittSequenzView, Aenderungsart undoAenderungsart) {
    super(undoAenderungsart, schrittSequenzView::setAenderungsart, schrittSequenzView::getAenderungsart);
  }
}
