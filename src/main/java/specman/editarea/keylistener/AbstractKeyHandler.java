package specman.editarea.keylistener;

import specman.editarea.TextEditArea;
import specman.editarea.TextEditAreaAccessMixin;
import specman.editarea.document.WrappedPosition;

import javax.swing.text.MutableAttributeSet;

public class AbstractKeyHandler implements TextEditAreaAccessMixin {
  protected final TextEditArea textArea;

  protected AbstractKeyHandler(TextEditArea textArea) {
    this.textArea = textArea;
  }

  @Override
  public TextEditArea textArea() { return textArea; }

  protected boolean shouldPreventActionInsideStepnumberLink() {
    if (stepnumberLinkStyleSet(getWrappedSelectionStart()) || stepnumberLinkStyleSet(getWrappedSelectionEnd())) {
      if (isCaretInsideSelection()) {
        return true;
      }

      for (WrappedPosition i = getWrappedSelectionStart(); i.less(getWrappedSelectionEnd()); i = i.inc()) {
        if (stepnumberLinkStyleSet(i)) {
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
    if (stepnumberLinkStyleSet(selectionEnd)) {
      setCaretPosition(getEndOffsetFromPosition(selectionEnd).unwrap());
      return true;
    }
    return false;
  }

  protected boolean skipToStepnumberLinkStart() {
    WrappedPosition selectionStart = getWrappedSelectionStart();
    if (stepnumberLinkStyleSet(selectionStart)) {
      setCaretPosition(getStartOffsetFromPosition(selectionStart).unwrap());
      return true;
    }
    return false;
  }

  protected void markRangeAsDeleted(WrappedPosition deleteStart, int deleteLength, MutableAttributeSet deleteStyle) {
    getWrappedDocument().setCharacterAttributes(deleteStart, deleteLength, deleteStyle, false);
  }

}
