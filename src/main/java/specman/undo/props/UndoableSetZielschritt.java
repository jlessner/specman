package specman.undo.props;

import specman.undo.AbstractUndoableInteraction;
import specman.view.AbstractSchrittView;
import specman.view.QuellSchrittView;

public class UndoableSetZielschritt extends UndoableSetProperty<AbstractSchrittView> {
  public UndoableSetZielschritt(QuellSchrittView quellSchrittView, AbstractSchrittView undoZielschritt) {
    super(undoZielschritt, quellSchrittView::setZielschritt, quellSchrittView::getZielschritt);
  }
}
