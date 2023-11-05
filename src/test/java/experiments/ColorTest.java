package experiments;

import org.junit.jupiter.api.Test;
import specman.textfield.TextStyles;

import java.awt.*;

public class ColorTest {

  @Test
  void testToHTMLColor_transparent() {
    Color transparentWhite = new Color(255, 255, 255, 0);
    System.out.println(TextStyles.toHTMLColor(transparentWhite));
  }

  @Test
  void testToHTMLColor_opaque() {
    Color white = new Color(16, 32, 128);
    System.out.println(TextStyles.toHTMLColor(white));
  }

}
