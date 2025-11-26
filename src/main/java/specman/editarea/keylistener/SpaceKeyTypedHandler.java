package specman.editarea.keylistener;

import specman.editarea.TextEditArea;
import specman.editarea.document.WrappedElement;
import specman.editarea.document.WrappedPosition;

import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import java.awt.event.KeyEvent;

import static javax.swing.text.html.HTML.Tag.IMPLIED;
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
   * permanently refuse adding whitespaces.
   * <p>
   * This logic is only applied if the caret is not located in a preformatted paragraph. */
  void handle() {
    String paragraphType = paragraphTypeAtCaretPosition();
    if (paragraphType.equals(Tag.PRE.toString())) {
      return;
    }
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

  public String paragraphTypeAtCaretPosition() {
    WrappedElement paragraphElement = getWrappedDocument().getParagraphElement(getWrappedCaretPosition());
    String paragraphType = paragraphElement.getName();
    return paragraphType.equals(IMPLIED.toString())
      ? paragraphElement.getParentElement().getName()
      : paragraphType;
  }

}
