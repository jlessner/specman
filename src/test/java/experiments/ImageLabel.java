package experiments;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;

public class ImageLabel extends JFrame {

  ImageLabel() throws Exception {
    setSize(400, 300);
    setVisible(true);
    File imageFile = new File("testimage.jpg");
    BufferedImage bufferedImage = ImageIO.read(imageFile);
    String imageType = FilenameUtils.getExtension(imageFile.getName());
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

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, imageType, bytes);
    String resultantimage = Base64.getEncoder().encodeToString(bytes.toByteArray());
    System.out.println(resultantimage.length());
    System.out.println(resultantimage);
  }

  public static void main(String[] args) throws Exception {
    new ImageLabel();
  }

}
