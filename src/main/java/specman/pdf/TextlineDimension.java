package specman.pdf;

import specman.editarea.TextStyles;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.geom.Rectangle2D;
import java.util.List;

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

  public String extractLineHtml(JEditorPane field) throws BadLocationException {
    List<TextRange> changes = findChanges(field);
    JEditorPane subdocCarrier = new JEditorPane();
    subdocCarrier.setContentType("text/html");
    subdocCarrier.setText(field.getText());
    StyledDocument subdoc = (StyledDocument) subdocCarrier.getDocument();
    if (field.getDocument().getLength() > getDocIndexTo() + 1) {
      subdoc.remove(getDocIndexTo(), field.getDocument().getLength() - getDocIndexTo());
    }
    subdoc.remove(0, getDocIndexFrom());
    for (TextRange change : changes) {
      subdoc.setCharacterAttributes(change.from - getDocIndexFrom(), change.length, TextStyles.geaendertTextBackgroundSpan, false);
    }
    return subdocCarrier.getText();
  }

  private List<TextRange> findChanges(JEditorPane field) throws BadLocationException {
    List<TextRange> changes = new java.util.ArrayList<>();
    TextRange change = null;
    StyledDocument doc = (StyledDocument) field.getDocument();
    String start = doc.getText(0, 1);
    int textStart = start.equals("\n") ? 1 : 0;
    for (int i = textStart; i < doc.getLength(); i++) {
      AttributeSet attrs = doc.getCharacterElement(i).getAttributes();
      boolean isChange = StyleConstants.getBackground(attrs).equals(TextStyles.AENDERUNGSMARKIERUNG_FARBE);
      if (isChange) {
        if (change == null) {
          change = new TextRange(i - textStart);
        }
        else {
          change.increase();
        }
      }
      else {
        if (change != null) {
          changes.add(change);
          change = null;
        }
      }
    }
    if (change != null) {
      changes.add(change);
    }
    return changes;
  }

  private static class TextRange {
    final int from;
    int length;
    public TextRange(int from) { this.from = from; this.length = 1; }
    public void increase() { this.length++; };
  }
}
