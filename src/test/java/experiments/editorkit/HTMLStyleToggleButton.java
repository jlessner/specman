package experiments.editorkit;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HTMLStyleToggleButton extends HTMLToggleButton {
  protected final MutableAttributeSet setStyle = new SimpleAttributeSet();
  protected final MutableAttributeSet notSetStyle = new SimpleAttributeSet();

  protected HTMLStyleToggleButton(String label, Object styleConstant, HTMLEditComponentsProvider provider) {
    super(label, provider);
    setStyle.addAttribute(styleConstant, true);
    notSetStyle.addAttribute(styleConstant, false);
  }

  @Override
  public void editorUpdated(TextSelection selection) {
    super.editorUpdated(selection);
    if (selection != null) {
      System.out.println("editorUpdated");
      StyledEditorKit sek = (StyledEditorKit)provider.getCurrentEditorPane().getEditorKit();
      System.out.println(sek.getInputAttributes());
      setSelected(selection.getStyle().containsAttributes(setStyle));
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    TextSelection selection = provider.getCurrentTextSelection();
    if (selection != null) {
      if (!selection.isEmpty()) {
        selection.applyStyle(isSelected() ? setStyle : notSetStyle);
        System.out.println(selection.editorPane.getText());
      }
      else {
        StyledEditorKit sek = (StyledEditorKit)provider.getCurrentEditorPane().getEditorKit();
        MutableAttributeSet inputAttributes = sek.getInputAttributes();
        inputAttributes.addAttributes(isSelected() ? setStyle : notSetStyle);
      }
    }
  }

}
