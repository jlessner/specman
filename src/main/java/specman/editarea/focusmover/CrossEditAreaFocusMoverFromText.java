package specman.editarea.focusmover;

import specman.editarea.TextEditArea;

import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;

public class CrossEditAreaFocusMoverFromText extends AbstractCrossEditAreaFocusMover<TextEditArea> {
  protected final int caretPosition;

  public CrossEditAreaFocusMoverFromText(TextEditArea currentFocusArea) {
    super(currentFocusArea);
    this.caretPosition = currentFocusArea.getCaretPosition();
  }

  @Override
  protected boolean caretAtBottom() {
    try {
      int caretRowEnd = Utilities.getRowEnd(currentFocusArea, caretPosition);
      return caretRowEnd == currentFocusArea.getDocument().getLength();
    }
    catch(BadLocationException blx) {
      blx.printStackTrace();
      return false;
    }
  }

  @Override
  protected boolean caretAtTop() {
    try {
      int caretRowStart = Utilities.getRowStart(currentFocusArea, caretPosition);
      if (currentFocusArea.newlineAt(0)) {
        caretRowStart--;
      }
      return caretRowStart == 0;
    }
    catch(BadLocationException blx) {
      blx.printStackTrace();
      return false;
    }
  }

}
