package specman.editarea;

import specman.model.v001.TextEditAreaModel_V001;

import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

/** This class splits a TextEdiArea into multiple areas based on the current caret position.
 * In the maximum case, the caret position is placed in a paragraph with leading and trailing
 * paragraphs within the same text area. As a result, the cutter produces
 * <ol>
 *   <li>a new TextEditArea containing only the text of the paragraph which the caret was in, and</li>
 *   <li>a new TextEditArea containing the text of all trailing paragraphs.</li>
 *   <li>a limitation of the initiating TextEditArea to the text from all leading paragraphs.</li>
 * </ol>
 * The two new TextEditAreas have to be added to the parent EditContainer of the initiating TextEditArea.
 * This is more complicated than it sounds, as there are different cases to consider (e.g. a completely
 * empty area as an extreme) and different representations of paragraphs separations in a StyledDocument.
 */
public class ParagraphCutter {
  private TextEditArea caretArea;
  private TextEditArea trailingArea;
  private TextEditArea initiatingArea;
  private int caretPosition;

  public ParagraphCutter cutAtCaret(TextEditArea initiatingArea) {
    this.initiatingArea = initiatingArea;
    this.caretPosition = initiatingArea.getCaretPosition();
    int leadingAreaStart = findLeadingAreaStart();
    int caretAreaStart = findCaretAreaStart();
    int caretAreaEnd = findCaretAreaEnd(leadingAreaStart);
    int leadingAreaEnd = findLeadingAreaEnd();
    int trailingAreaStart = findTrailingAreaStart(caretAreaEnd);

    caretArea = initiatingArea.copySection(caretAreaStart, caretAreaEnd);
    if (caretArea == null) {
      caretArea = new TextEditArea(new TextEditAreaModel_V001(""), initiatingArea.getFont());
    }
    trailingArea = initiatingArea.copySection(trailingAreaStart, initiatingArea.getLength() - 1);
    initiatingArea.shrink(leadingAreaStart, leadingAreaEnd);

    return this;
  }

  private boolean newlineAt(int pos) {
    return initiatingArea.getTextRX(pos, 1).startsWith("\n");
  }

  private int findLeadingAreaStart() {
    return newlineAt(0) ? 1 : 0;
  }

  private int findTrailingAreaStart(int caretAreaEnd) {
    return Math.min(caretAreaEnd + 2, initiatingArea.getLength());
  }

  private int findLeadingAreaEnd() {
    int pos = findCaretAreaStart() - 1;
    return newlineAt(pos) ? pos - 1 : pos;
  }

  private int findCaretAreaEnd(int leadingAreaStart) {
    int endPos = currentParagraphElement().getEndOffset() - leadingAreaStart;
    return newlineAt(endPos) ? endPos - 1 : endPos;
  }

  private int findCaretAreaStart() {
    return currentParagraphElement().getStartOffset();
  }

  private Element currentParagraphElement() {
    StyledDocument doc = (StyledDocument) initiatingArea.getDocument();
    return doc.getParagraphElement(caretPosition);
  }

  public TextEditArea caretTextArea() {
    return caretArea;
  }

  public EditArea trailingTextArea() {
    return trailingArea;
  }
}
