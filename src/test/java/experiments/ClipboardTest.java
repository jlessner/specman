package experiments;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class ClipboardTest {

  @Test
  void testReadString() throws Exception {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable contents = clipboard.getContents(null);
    boolean hasTransferableText = (contents != null) &&
        contents.isDataFlavorSupported(DataFlavor.stringFlavor);
    if (hasTransferableText) {
      String result = (String)contents.getTransferData(DataFlavor.stringFlavor);
      System.out.println(result);
    }
  }

  @Test
  void testReadImage() throws Exception {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable contents = clipboard.getContents(null);
    boolean hasTransferableText = (contents != null) &&
      contents.isDataFlavorSupported(DataFlavor.imageFlavor);
    if (hasTransferableText) {
      BufferedImage image = (BufferedImage) contents.getTransferData(DataFlavor.imageFlavor);
      System.out.println(image);
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ImageIO.write(image, "png", bytes);
      bytes.close();
      System.out.println(bytes.size());
    }
  }

}
