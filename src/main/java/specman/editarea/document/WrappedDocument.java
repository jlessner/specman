package specman.editarea.document;

import specman.editarea.changemarks.CharType;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import java.util.Arrays;
import java.util.List;

import static specman.editarea.changemarks.CharType.ParagraphBoundary;

public class WrappedDocument implements WrappedDocumentI {
  private final StyledDocument document;
  private final int visibleTextStart;

  public WrappedDocument(StyledDocument document) {
    try {
      this.document = document;
      String firstChar = document.getText(0, 1);
      visibleTextStart = firstChar.equals("\n") ? 1 : 0;
    }
    catch(BadLocationException blx) {
      throw new WrappedBadLocationException(blx);
    }
  }

  public int getVisibleTextStart() { return visibleTextStart; }

  public int getLength() {
    return document.getLength();
  }

  public void setCharacterAttributes(int offset, int length, AttributeSet style, boolean replace) {
    setCharacterAttributes(fromUI(offset), length, style, replace);
  }

  public void setCharacterAttributes(WrappedPosition offset, int length, AttributeSet style, boolean replace) {
    document.setCharacterAttributes(offset.unwrap(), length, style, replace);
  }

  public List<WrappedElement> getRootElements() {
    return Arrays
      .stream(document.getRootElements())
      .map(e -> new WrappedElement(e, this))
      .toList();
  }

  public WrappedElement getCharacterElement(int position) {
    return getCharacterElement(fromUI(position));
  }

  public WrappedElement getCharacterElement(WrappedPosition position) {
    Element element = document.getCharacterElement(position.unwrap());
    return new WrappedElement(element, this );
  }

  public void remove(WrappedPosition offs, int len) {
    try {
      // I hope this won't cause us trouble in the future:
      // A leading newline at the beginning of the document does increase the
      // document's length (see method getLength). So if the passed offset and length
      // came out of a calculation, the sum of both might reach to the very end of this
      // WrappedDocument and exceed the underlaying document's length by 1. If this is
      // the case, we silently decrease the passed length by 1.
      if (offs.unwrap() + len == document.getLength() + 1) {
        len--;
      }
      document.remove(offs.unwrap(), len);
    }
    catch(BadLocationException blx) {
      throw new WrappedBadLocationException(blx);
    }
  }

  public void insertString(WrappedPosition offset, String str, AttributeSet a) {
    try {
      document.insertString(offset.unwrap(), str, a);
    }
    catch(BadLocationException blx) {
      throw new WrappedBadLocationException(blx);
    }
  }

  public Document getCore() { return document; }

  public String getText(WrappedPosition offset, int length) {
    try {
      return document.getText(offset.unwrap(), length);
    }
    catch(BadLocationException blx) {
      throw new WrappedBadLocationException(blx);
    }
  }

  public WrappedElement getParagraphElement(WrappedPosition pos) {
    return getParagraphElement(pos.toModel());
  }

  public WrappedElement getParagraphElement(int pos) {
    Element element = document.getParagraphElement(pos + visibleTextStart);
    return new WrappedElement(element, this);
  }

  public WrappedPosition fromUI(int position) {
    return new WrappedPosition(position - getVisibleTextStart(), this);
  }

  public WrappedPosition fromModel(int position) {
    return new WrappedPosition(position, this);
  }

  public WrappedPosition start() { return fromModel(0); }
  public WrappedPosition end() { return fromModel(getLength()-1); }

  public boolean hasContent() {
    switch(getLength()) {
      case 0: return false;
      case 1: return !ParagraphBoundary.at(start());
      default: return true;
    }
  }

}
