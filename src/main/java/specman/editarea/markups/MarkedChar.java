package specman.editarea.markups;

import specman.editarea.document.WrappedDocument;
import specman.editarea.document.WrappedElement;
import specman.editarea.document.WrappedPosition;

public class MarkedChar {
  public final char c;
  public final MarkupType markupType;

  public MarkedChar(WrappedDocument doc, WrappedPosition p) {
    c = doc.getChar(p);
    WrappedElement element = doc.getCharacterElement(p);
    markupType = MarkupType.fromBackground(element);
  }

  public MarkedChar(char c, MarkupType markupType) {
    this.c = c;
    this.markupType = markupType;
  }

}
