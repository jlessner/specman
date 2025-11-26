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
   * caret is at the very beginning of the document or at the beginning of a paragraph.
   * <p>
   * However, if there is a whitespace ahead, we increase the caret position so that
   * the caret is placed <i>after</i> it. Otherwise, continuous typing of text whould
   * permanently refuse adding whitespaces. */
  void handle() {
    //textArea.paragraphType();
    WrappedPosition caret = getWrappedCaretPosition();
    if (caret.isZero() ||
      Whitespace.at(caret) ||
      Whitespace.at(caret.dec()) ||
      ParagraphBoundary.at(caret.dec())) {
      event.consume();
      hopOverWhitespaceAhead(caret);
    }
  }

  private void hopOverWhitespaceAhead(WrappedPosition caret) {
    if (Whitespace.at(caret)) {
      setCaretPosition(caret.unwrap() + 1);
    }
  }
}
