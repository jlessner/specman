package specman.editarea;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import java.util.Arrays;
import java.util.List;

public class WrappedDocument {
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
    return document.getLength() - visibleTextStart;
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
}
