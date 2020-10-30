package specman.textfield;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageScalerTest {

  @Test
  void testScalingUpMultipleImagesByFactor_1dot2() {
    String html = "intro "
        + "<img height=\"300\" vspace=\"1\" border=\"1\" hspace=\"1\" width=\"500\" alt=\"alt text\" src=\"max.png\" \"align=\"top\">"
        + " middle "
        + "<img src=\"min.png\">"
        + " outro";

    String scaledHtml = new ImageScaler(120, 100).scaleImages(html);
    System.out.println(scaledHtml);
    assertTrue(scaledHtml.contains("360"));
    assertTrue(scaledHtml.contains("600"));
    assertEquals(html.length(), scaledHtml.length());
  }

}
