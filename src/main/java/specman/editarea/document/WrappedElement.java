package specman.editarea.document;

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
    return document.fromUI(element.getStartOffset());
  }

  public WrappedPosition getEndOffset() {
    WrappedPosition result = document.fromUI(element.getEndOffset());
    // Documents derived from AbstractDocument have an implied newline at the end
    // See Javadoc of Element#getEndOffset(). Therefore the Method may return an
    // end offset acceeding the document length. We adjust this here.
    return result.exists() ? result : result.dec();
  }

  public int getElementCount() {
    return element.getElementCount();
  }

  public WrappedElement getElement(int i) {
    return new WrappedElement(element.getElement(i), document);
  }

  public WrappedDocument getDocument() {
    return document;
  }

  public AttributeSet getAttributes() { return element.getAttributes(); }
  public boolean isLeaf() { return element.isLeaf(); }
  public String getName() { return element.getName(); }
}
