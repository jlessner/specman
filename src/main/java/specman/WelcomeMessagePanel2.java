package specman;

import specman.view.AbstractSchrittView;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;

public class WelcomeMessagePanel2 extends JPanel {
  public WelcomeMessagePanel2() {
    try {
      Image myPicture = ImageIO.read(new File("examples/BALI UC-113-Image-02.png"));
      myPicture = myPicture.getScaledInstance(600, 300, Image.SCALE_SMOOTH);
      Icon icon = new ImageIcon(myPicture);
      JLabel label = new JLabel(icon);
      this.add(label);
      setOpaque(true);
      setForeground(Color.gray);
      setBackground(new Color(230, 230, 240));
      setBorder(new CompoundBorder(
          new LineBorder(Color.BLACK, AbstractSchrittView.LINIENBREITE),
          new EmptyBorder(15, 15, 15, 15)));

    }
    catch(Exception x) {
      x.printStackTrace();
    }
  }
}
