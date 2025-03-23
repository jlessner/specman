package experiments.editorkit;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Attribute;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class YellowToggleButton extends HTMLToggleButton implements ActionListener {
  protected final MutableAttributeSet style = new SimpleAttributeSet();

  protected YellowToggleButton(HTMLEditComponentsProvider provider) {
    super("<html>Yo</html>", provider);
    SimpleAttributeSet htmlStyleYellow = new SimpleAttributeSet();
    htmlStyleYellow.addAttribute(HTML.Attribute.STYLE, "background-color:yellow");
    style.addAttribute(HTML.Tag.SPAN, htmlStyleYellow);
    StyleConstants.setBackground(style, Color.yellow);
  }

  @Override
  public void editorUpdated(TextSelection selection) {
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    TextSelection selection = provider.getCurrentTextSelection();
    if (selection != null) {
      if (!selection.isEmpty()) {
        selection.applyStyle(style);
        System.out.println(selection.editorPane.getText());
      }
      else {
        StyledEditorKit sek = (StyledEditorKit) provider.getCurrentEditorPane().getEditorKit();
        MutableAttributeSet inputAttributes = sek.getInputAttributes();
        inputAttributes.addAttributes(style);
      }
    }
  }

}
