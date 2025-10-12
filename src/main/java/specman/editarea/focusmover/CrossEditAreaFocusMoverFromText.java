package specman.editarea.focusmover;

import specman.editarea.TextEditArea;
import specman.editarea.document.WrappedPosition;

import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;

public class CrossEditAreaFocusMoverFromText extends AbstractCrossEditAreaFocusMover<TextEditArea> {
  protected final WrappedPosition caretPosition;

  public CrossEditAreaFocusMoverFromText(TextEditArea currentFocusArea) {
    super(currentFocusArea);
    this.caretPosition = currentFocusArea.getWrappedCaretPosition();
  }

  @Override
  protected boolean caretAtBottom() {
    try {
      int caretRowEnd = Utilities.getRowEnd(currentFocusArea, caretPosition.toModel());
      return caretRowEnd == currentFocusArea.getWrappedDocument().getLength();
    }
    catch(BadLocationException blx) {
      blx.printStackTrace();
      return false;
    }
  }

  @Override
  protected boolean caretAtTop() {
    try {
      int caretRowStart = Utilities.getRowStart(currentFocusArea, caretPosition.toModel());
      return caretRowStart == 0;
    }
    catch(BadLocationException blx) {
      blx.printStackTrace();
      return false;
    }
  }

}
