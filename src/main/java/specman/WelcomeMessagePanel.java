package specman;

import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.AbstractSchrittModel_V001;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class WelcomeMessagePanel extends JLabel {
  public WelcomeMessagePanel() {
    setText("<html>"
        + "<h2><i>Welcome to Specman " + Specman.SPECMAN_VERSION + "</i></h2><i>"
        + "This is the modelling area. Click a button from the steps palette and "
        + "drag the mouse to the position where you want to place the step. Alternatively "
        + "just click the button from the steps palette to add a step below the one "
        + "focused by the text cursor."
        + "</i></html>");
    setOpaque(true);
    setForeground(Color.gray);
    setBackground(new Color(230, 230, 240));
    setBorder(new CompoundBorder(
        new LineBorder(Color.BLACK, 2),
        new EmptyBorder(15, 15, 15, 15)));
  }
}
