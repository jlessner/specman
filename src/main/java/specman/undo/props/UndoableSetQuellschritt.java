package specman.undo.props;

import specman.view.AbstractSchrittView;
import specman.view.QuellSchrittView;

public class UndoableSetQuellschritt extends UndoableSetProperty<QuellSchrittView> {
  public UndoableSetQuellschritt(AbstractSchrittView abstractSchrittView, QuellSchrittView undoQuellschritt) {
    super(undoQuellschritt, abstractSchrittView::setQuellschritt, abstractSchrittView::getQuellschritt);
  }
}
