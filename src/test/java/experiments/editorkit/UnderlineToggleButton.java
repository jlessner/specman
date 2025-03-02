package experiments.editorkit;

import javax.swing.text.StyleConstants;
import java.awt.event.ActionListener;

public class UnderlineToggleButton extends HTMLStyleToggleButton implements ActionListener {

  public UnderlineToggleButton(HTMLEditComponentsProvider provider) {
    super("<html><u>U</u></html>", StyleConstants.Underline, provider);
  }

}
