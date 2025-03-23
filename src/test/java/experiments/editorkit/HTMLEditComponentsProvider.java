package experiments.editorkit;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.undo.UndoManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

public class HTMLEditComponentsProvider implements FocusListener, CaretListener {
  private UndoManager undoManager;
  private JToolBar formatToolbar;
  private JEditorPane currentEditorPane;
  private List<EditorPaneListener> editorListeners = new ArrayList<>();

  public void instrumentWysEditor(JEditorPane ed, String initialText) {
    HTMLEditorKit kit = new HTMLEditorKit();
    StyleSheet styleSheet = kit.getStyleSheet();
    styleSheet.addRule("body { font-size: " + Fontsize.SWING_FONTSIZE + "; }");
    ed.setEditorKit(kit);
    ed.setContentType("text/html");

    ed.setText(initialText);

    ed.addFocusListener(this);
    ed.addCaretListener(this);

    HTMLDocument document = (HTMLDocument)ed.getDocument();
    if (undoManager != null) {
      document.addUndoableEditListener(undoManager);
    }
  }

  public HTMLEditComponentsProvider() {
    createFormatToolBar();
    informEditorListeners();
  }

  private void createFormatToolBar() {
    formatToolbar = new JToolBar();
    formatToolbar.setFloatable(false);
    formatToolbar.setFocusable(false);

    formatToolbar.add(new FontsizeBox(this));
    formatToolbar.add(new BoldToggleButton(this));
    formatToolbar.add(new ItalicToggleButton(this));
    formatToolbar.add(new UnderlineToggleButton(this));
    formatToolbar.add(new YellowToggleButton(this));
    formatToolbar.add(new SmallButton(this));

  }

  public JToolBar getFormatToolbar() {
    return formatToolbar;
  }

  @Override
  public void focusGained(FocusEvent e) {
    currentEditorPane = (JEditorPane)e.getComponent();
    informEditorListeners();
  }

  private void informEditorListeners() {
    TextSelection selection = currentEditorPane != null ? new TextSelection(currentEditorPane) : null;
    editorListeners.forEach(l -> l.editorUpdated(selection));
  }

  @Override
  public void focusLost(FocusEvent e) {
    currentEditorPane = null;
  }

  public JEditorPane getCurrentEditorPane() {
    return currentEditorPane;
  }

  public TextSelection getCurrentTextSelection() {
    return currentEditorPane != null
      ? new TextSelection(currentEditorPane)
      : null;
  }

  @Override
  public void caretUpdate(CaretEvent e) {
    informEditorListeners();
  }

  public void addEditorPaneListener(EditorPaneListener listener) {
    this.editorListeners.add(listener);
  }
}
