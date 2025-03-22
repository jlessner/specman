package experiments.editorkit;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyledDocument;

public class TextSelection {
  final JEditorPane editorPane;
  final int start, end;

  public TextSelection(JEditorPane editorPane) {
    this.editorPane = editorPane;
    this.start = editorPane.getSelectionStart();
    this.end = editorPane.getSelectionEnd();
  }

  public void applyStyle(MutableAttributeSet style) {
    StyledDocument document = (StyledDocument)editorPane.getDocument();
    document.setCharacterAttributes(start, end-start, style, false);
  }

  public AttributeSet getStyle() {
    StyledDocument document = (StyledDocument)editorPane.getDocument();
    return document.getCharacterElement(start).getAttributes();
  }

  public boolean isEmpty() { return start == end; }
}
