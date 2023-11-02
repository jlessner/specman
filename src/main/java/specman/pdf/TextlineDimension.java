package specman.pdf;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.geom.Rectangle2D;

public class TextlineDimension {
  int docIndexFrom, docIndexTo;
  Rectangle2D space;

  private static JEditorPane subdocCarrier = new JEditorPane();
  static { subdocCarrier.setContentType("text/html"); }

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
    subdocCarrier.setText(field.getText());
    Document subdoc = subdocCarrier.getDocument();
    subdoc.remove(getDocIndexTo(), field.getDocument().getLength() - getDocIndexTo());
    subdoc.remove(0, getDocIndexFrom());
    return subdocCarrier.getText();
  }
}
