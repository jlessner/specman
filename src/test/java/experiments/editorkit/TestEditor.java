package experiments.editorkit;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import specman.editarea.TextStyles;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;

public class TestEditor extends JFrame {
  private JEditorPane field;
  private Container contentPane;
  private HTMLEditComponentsProvider editComponents;

  TestEditor() throws Exception {
    setSize(300, 500);
    contentPane = this.getContentPane();
    contentPane.setLayout(new FormLayout("80px:grow", "30px,fill:pref:grow,30px,30px"));

    editComponents = new HTMLEditComponentsProvider();
    contentPane.add(editComponents.getFormatToolbar(), CC.xy(1, 1));

    field = new JEditorPane();
    field.setContentType("text/html");
    field.setFont(TextStyles.font.deriveFont(Fontsize.SWING_FONTSIZE));
    field.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    contentPane.add(field, CC.xy(1, 2));
    editComponents.instrumentWysEditor(field, "<html>Überschrift<br>blabla<br>Nummerierte Liste:<ul><li>eins<li>zwei, zwwei<li>Unterliste<ul><li>drei eins<li>drei zwei</ul></ul>drei<br><br><br>vier");

    this.setDefaultCloseOperation(EXIT_ON_CLOSE);

    setVisible(true);
  }

  public static void main(String[] args) throws Exception {
    new TestEditor();
  }

}
