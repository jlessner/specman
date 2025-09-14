package specman.editarea;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;

public class WrappedElement {
  private final Element element;
  private final WrappedDocument document;

  public WrappedElement(Element element, WrappedDocument wrappedDocument) {
    this.element = element;
    this.document = wrappedDocument;
  }

  public WrappedPosition getStartOffset() {
    return new WrappedPosition(element.getStartOffset(), document);
  }

  public WrappedPosition getEndOffset() {
    return new WrappedPosition(element.getEndOffset(), document);
  }

  public int getElementCount() {
    return element.getElementCount();
  }

  public WrappedElement getElement(int i) {
    return new WrappedElement(element.getElement(i), document);
  }

  public AttributeSet getAttributes() {
    return element.getAttributes();
  }

  public WrappedDocument getDocument() {
    return document;
  }
}
