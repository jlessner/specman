package experiments;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageLabel extends JFrame {

  ImageLabel() throws Exception {
    setSize(400, 300);
    setVisible(true);
    BufferedImage bufferedImage = ImageIO.read(new File("Download.png"));
    System.out.println(bufferedImage.getWidth());
    ImageIcon scaledIcon = new ImageIcon(bufferedImage
      .getScaledInstance(bufferedImage.getWidth() / 2, bufferedImage.getHeight() / 2, Image.SCALE_SMOOTH));

    JLabel image = new JLabel(scaledIcon);
    image.setBackground(Color.yellow);
    image.setOpaque(true);
    this.getContentPane().setLayout(new FormLayout("600px:grow", "fill:300px:grow"));
    this.getContentPane().add(image, CC.xy(1,1));
    this.pack();
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  public static void main(String[] args) throws Exception {
    new ImageLabel();
  }

}
