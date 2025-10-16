package specman.editarea.keylistener;

import specman.editarea.TextEditArea;
import specman.editarea.document.WrappedDocument;
import specman.editarea.document.WrappedElement;
import specman.editarea.document.WrappedPosition;
import specman.editarea.markups.MarkedChar;
import specman.editarea.markups.MarkedCharSequence;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyledEditorKit;

abstract class AbstractKeyEventHandler {
  protected final TextEditArea textArea;
  
  protected AbstractKeyEventHandler(TextEditArea textArea) {
    this.textArea = textArea;
  }

  protected void markRangeAsDeleted(WrappedPosition deleteStart, int deleteLength, MutableAttributeSet deleteStyle) {
    getWrappedDocument().setCharacterAttributes(deleteStart, deleteLength, deleteStyle, false);
  }

  protected boolean shouldPreventActionInsideStepnumberLink() {
    if (stepnumberLinkNormalOrChangedStyleSet(getWrappedSelectionStart()) || stepnumberLinkNormalOrChangedStyleSet(getWrappedSelectionEnd())) {
      if (isCaretInsideSelection()) {
        return true;
      }

      for (WrappedPosition i = getWrappedSelectionStart(); i.less(getWrappedSelectionEnd()); i.inc()) {
        if (stepnumberLinkNormalOrChangedStyleSet(i)) {
          if (getStartOffsetFromPosition(i).less(getWrappedSelectionStart()) ||
            getEndOffsetFromPosition(i).greater(getWrappedSelectionEnd())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  protected boolean isCaretInsideSelection() {
    WrappedPosition linkStyleStart = getStartOffsetFromPosition(getWrappedSelectionEnd());
    WrappedPosition linkStyleEnd = getEndOffsetFromPosition(getWrappedSelectionEnd());
    return getWrappedSelectionStart().equals(getWrappedSelectionEnd()) &&
      getWrappedSelectionEnd().less(linkStyleEnd) &&
      getWrappedSelectionStart().greater(linkStyleStart);
  }

  protected boolean skipToStepnumberLinkEnd() {
    WrappedPosition selectionEnd = getWrappedSelectionEnd();
    if (stepnumberLinkNormalOrChangedStyleSet(selectionEnd)) {
      setCaretPosition(getEndOffsetFromPosition(selectionEnd).unwrap());
      return true;
    }
    return false;
  }

  protected MarkedCharSequence findMarkups() {
    MarkedCharSequence seq = new MarkedCharSequence();
    WrappedDocument doc = getWrappedDocument();
    for (WrappedPosition p = doc.fromModel(0); p.exists(); p = p.inc()) {
      MarkedChar c = new MarkedChar(doc, p);
      seq.add(c);
    }
    return seq;
  }

  protected boolean aenderungsStilGesetzt() { return textArea.aenderungsStilGesetzt(); }
  protected boolean elementHatAenderungshintergrund(WrappedElement element) { return textArea.elementHatAenderungshintergrund(element); }
  protected boolean elementHatDurchgestrichenenText(WrappedElement element) { return textArea.elementHatDurchgestrichenenText(element); }
  protected boolean stepnumberLinkChangedStyleSet(WrappedElement element) { return textArea.stepnumberLinkChangedStyleSet(element); }
  protected String getStepnumberLinkIDFromElement(WrappedPosition start, WrappedPosition end) { return textArea.getStepnumberLinkIDFromElement(start, end); }
  protected void setCaretPosition(int position) { textArea.setCaretPosition(position); }
  protected boolean stepnumberLinkNormalOrChangedStyleSet(WrappedPosition i) { return textArea.stepnumberLinkNormalOrChangedStyleSet(i); }
  protected boolean stepnumberLinkNormalOrChangedStyleSet(WrappedElement e) { return textArea.stepnumberLinkNormalOrChangedStyleSet(e); }
  protected WrappedPosition getWrappedSelectionStart() { return textArea.getWrappedSelectionStart(); }
  protected WrappedPosition getEndOffsetFromPosition(WrappedPosition position) { return textArea.getEndOffsetFromPosition(position); }
  protected WrappedPosition getStartOffsetFromPosition(WrappedPosition position) { return textArea.getStartOffsetFromPosition(position); }
  protected WrappedPosition getWrappedSelectionEnd() { return textArea.getWrappedSelectionEnd(); }
  protected WrappedPosition getWrappedCaretPosition() { return textArea.getWrappedCaretPosition(); }
  protected boolean isTrackingChanges() { return textArea.isTrackingChanges(); }
  protected WrappedDocument getWrappedDocument() { return textArea.getWrappedDocument(); }
  protected boolean stepnumberLinkNormalStyleSet(WrappedPosition position) { return textArea.stepnumberLinkNormalOrChangedStyleSet(position); }
  protected int getSelectionEnd() { return textArea.getSelectionEnd(); }
  protected int getSelectionStart() { return textArea.getSelectionStart(); }
  protected void setSelectionStart(int position) { textArea.setSelectionStart(position); }
  protected boolean isEditable() { return textArea.isEditable(); }
  protected StyledEditorKit getEditorKit() { return (StyledEditorKit) textArea.getEditorKit(); }

}
