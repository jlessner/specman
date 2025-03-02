package experiments.editorkit;

import javax.swing.text.StyleConstants;
import java.awt.event.ActionListener;

public class BoldToggleButton extends HTMLStyleToggleButton implements ActionListener {

  public BoldToggleButton(HTMLEditComponentsProvider provider) {
    super("<html><b>B</b></html>", StyleConstants.Bold, provider);
  }

}
