package specman.textfield;

import org.junit.jupiter.api.Test;

public class ImageScalerTest {

  @Test
  void test() {
    String html = "intro "
        + "<img height=\"300\" vspace=\"1\" border=\"1\" hspace=\"1\" width=\"300\" alt=\"alt text\" src=\"max.png\" \"align=\"top\">"
        + " middle "
        + " <img src=\"min.png\">"
        + " outro";
    new ImageScaler(0, 0).scaleImages(html);
  }
}
