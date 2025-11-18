package specman.pdf;

import specman.editarea.TextEditArea;
import specman.editarea.TextStyles;
import specman.editarea.document.WrappedDocument;
import specman.editarea.document.WrappedPosition;
import specman.editarea.markups.MarkedCharSequence;
import specman.editarea.markups.MarkupType;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.geom.Rectangle2D;

public class TextlineDimension {
  WrappedPosition docIndexFrom, docIndexTo;
  Rectangle2D space;

  public TextlineDimension(WrappedPosition docIndexFrom, WrappedPosition docIndexTo, Rectangle2D space) {
    this.docIndexFrom = docIndexFrom;
    this.docIndexTo = docIndexTo;
    this.space = space;
  }

  public WrappedPosition getDocIndexFrom() { return docIndexFrom; }
  public WrappedPosition getDocIndexTo() { return docIndexTo; }
  public float getHeight() { return (float)space.getHeight(); }
  public float getY() { return (float)space.getY(); }
  public float getX() { return (float)space.getX(); }

  public String extractLineHtml(TextEditArea field, MarkedCharSequence fieldmarks) throws BadLocationException {
    JEditorPane subdocCarrier = new JEditorPane();
    subdocCarrier.setContentType("text/html");
    subdocCarrier.setText(field.getText());
    WrappedDocument subdoc = new WrappedDocument((StyledDocument) subdocCarrier.getDocument());
    if (!docIndexTo.isLast()) {
      subdoc.removeFrom(docIndexTo.inc());
    }
    subdoc.remove(getDocIndexFrom().toModel());

    MarkedCharSequence lineMarks = fieldmarks.subsequence(docIndexFrom.toModel(), docIndexTo.toModel());
    for (int i = 0; i < lineMarks.size(); i++) {
      MarkupType markupType = lineMarks.type(i);
      if (markupType != null && markupType.toPDFBackground() != null) {
        subdoc.setCharacterAttributes(i+1, 1, markupType.toPDFBackground(),false);
      }
    }

    return subdocCarrier.getText();
  }

}
