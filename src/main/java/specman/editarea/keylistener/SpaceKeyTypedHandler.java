package specman.editarea.keylistener;

import specman.editarea.TextEditArea;
import specman.editarea.document.WrappedPosition;

import java.awt.event.KeyEvent;

import static specman.editarea.markups.CharType.ParagraphBoundary;
import static specman.editarea.markups.CharType.Whitespace;

public class SpaceKeyTypedHandler extends AbstractKeyEventHandler {
  public SpaceKeyTypedHandler(TextEditArea textArea, KeyEvent e) {
    super(textArea, e);
  }

  @Override
  /** If there is a whitespace directly in front or behind the caret position,
   * we do not want to insert another whitespace there. The same is true if the
   * caret is at the very beginning of the document or at the beginning of a paragraph. */
  void handle() {
    WrappedPosition caret = getWrappedCaretPosition();
    if (caret.isZero() ||
      Whitespace.at(caret) ||
      Whitespace.at(caret.dec()) ||
      ParagraphBoundary.at(caret.dec())) {
      event.consume();
    }
  }
}
