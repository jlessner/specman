package experiments.editorkit;

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class HTMLToggleButton extends JToggleButton implements EditorPaneListener, ActionListener {
  protected final HTMLEditComponentsProvider provider;

  protected HTMLToggleButton(String label, HTMLEditComponentsProvider provider) {
    super(label);
    setFocusable(false);
    addActionListener(this);
    provider.addEditorPaneListener(this);
    this.provider = provider;
    setMaximumSize(new Dimension(30, 30));
  }

  @Override
  public void editorUpdated(TextSelection selection) {
    setEnabled(selection != null);
  }

}
