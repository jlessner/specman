package experiments.itext;

import com.itextpdf.io.font.TrueTypeFont;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Arrays;

public class FontTest {

  @Test
  void test() throws Exception {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    String []fontFamilies = ge.getAvailableFontFamilyNames();
    System.out.println(Arrays.asList(fontFamilies));

    PdfFont font = PdfFontFactory.createFont(new TrueTypeFont("C:/Windows/Fonts/BAUHS93.TTF"));

  }
}
