package experiments.editorkit;

import javax.swing.text.StyleConstants;
import java.awt.event.ActionListener;

public class ItalicToggleButton extends HTMLStyleToggleButton implements ActionListener {

  public ItalicToggleButton(HTMLEditComponentsProvider provider) {
    super("<html><i>I</i></html>", StyleConstants.Italic, provider);
  }

}
