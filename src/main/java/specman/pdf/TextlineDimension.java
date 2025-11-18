package specman.pdf;

import org.apache.commons.lang.math.IntRange;
import specman.editarea.TextEditArea;
import specman.editarea.TextStyles;
import specman.editarea.document.WrappedDocument;
import specman.editarea.markups.MarkedCharSequence;
import specman.editarea.markups.MarkupBackgroundStyleInitializer;
import specman.editarea.markups.MarkupRecovery;
import specman.editarea.markups.MarkupType;
import specman.model.v001.Markup_V001;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;

public class TextlineDimension {
  int docIndexFrom, docIndexTo;
  Rectangle2D space;

  public TextlineDimension(int docIndexFrom, int docIndexTo, Rectangle2D space) {
    this.docIndexFrom = docIndexFrom;
    this.docIndexTo = docIndexTo;
    this.space = space;
  }

  public int getDocIndexFrom() { return docIndexFrom; }
  public int getDocIndexTo() { return docIndexTo; }
  public float getHeight() { return (float)space.getHeight(); }
  public float getY() { return (float)space.getY(); }
  public float getX() { return (float)space.getX(); }

  public String extractLineHtml(TextEditArea field, MarkedCharSequence fieldmarks) throws BadLocationException {
    JEditorPane subdocCarrier = new JEditorPane();
    subdocCarrier.setContentType("text/html");
    subdocCarrier.setText(field.getText());
    StyledDocument subdoc = (StyledDocument) subdocCarrier.getDocument();
    if (field.getDocument().getLength() > getDocIndexTo() + 1) {
      subdoc.remove(getDocIndexTo(), field.getDocument().getLength() - getDocIndexTo());
    }
    subdoc.remove(0, getDocIndexFrom());

    MarkedCharSequence lineMarks = fieldmarks.subsequence(docIndexFrom, docIndexTo);
    for (int i = 0; i < lineMarks.size(); i++) {
      MarkupType markupType = lineMarks.type(i);
      if (markupType != null) {
        subdoc.setCharacterAttributes(i+1, 1, TextStyles.geaendertTextBackgroundSpan,false);
      }
    }

    return subdocCarrier.getText();
  }

}
