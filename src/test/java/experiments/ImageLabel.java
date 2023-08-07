package experiments;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;

public class ImageLabel extends JFrame {

  ImageLabel() {
    setSize(400, 300);
    setVisible(true);
    ImageIcon icon = new ImageIcon("Download.png");
    //JLabel image = new JLabel("image");
    System.out.println(icon.getIconHeight());
    ImageIcon scaledIcon = new ImageIcon(icon.getImage()
      .getScaledInstance(icon.getIconWidth() / 2, icon.getIconHeight() / 2, Image.SCALE_SMOOTH));

    JLabel image = new JLabel(scaledIcon);
    image.setBackground(Color.yellow);
    image.setOpaque(true);
    this.getContentPane().setLayout(new FormLayout("600px:grow", "fill:300px:grow"));
    this.getContentPane().add(image, CC.xy(1,1));
    this.pack();
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  public static void main(String[] args) {
    new ImageLabel();
  }

}
