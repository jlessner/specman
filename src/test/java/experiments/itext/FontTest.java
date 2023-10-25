package experiments.itext;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.TrueTypeFont;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.font.FontProvider;
import org.junit.jupiter.api.Test;
import specman.textfield.TextStyles;

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

  @Test
  void testBoldHTMLHeadingWithCustomFont() throws Exception {
    ConverterProperties properties = new ConverterProperties();
    //FontProvider fontProvider = new DefaultFontProvider(false, false, false);
    FontProvider fontProvider = new DefaultFontProvider(false, false, false);

//    fontProvider.addFont(FontProgramFactory.createFont("C:/Windows/Fonts/times.ttf"));
//    fontProvider.addFont(FontProgramFactory.createFont("C:/Windows/Fonts/timesi.ttf"));
//    fontProvider.addFont(FontProgramFactory.createFont("C:/Windows/Fonts/timesbd.ttf"));
//    fontProvider.addFont(FontProgramFactory.createFont("C:/Windows/Fonts/timesbi.ttf"));

    //fontProvider.addFont(FontProgramFactory.createFont(TextStyles.SERIF_FONT));
    fontProvider.addFont(FontProgramFactory.createFont(TextStyles.SERIF_FONTCOLLECTION_REGULAR, TextStyles.FONT_INDEX, false));
    fontProvider.addFont(FontProgramFactory.createFont("src/main/resources/fonts/SitkaB.ttc", TextStyles.FONT_INDEX, false));
    fontProvider.addFont(FontProgramFactory.createFont("src/main/resources/fonts/SitkaI.ttc", TextStyles.FONT_INDEX, false));
    fontProvider.addFont(FontProgramFactory.createFont("src/main/resources/fonts/SitkaZ.ttc", TextStyles.FONT_INDEX, false));

    // Folgendes geht nicht wegen eines Lizenzproblems, das dann beim Hinzufügen des ersten Paragrafen auftritt
    //fontProvider.addFont(FontProgramFactory.createFont("C:/Users/jlessner/AppData/Local/Microsoft/Windows/Fonts/TimesNewRomanPSMT.ttf"));

    properties.setFontProvider(fontProvider);
    java.util.List<IElement> elements = HtmlConverter.convertToElements("<H1 style='font-family:serif'>Serif Headwinjg 1</H1>" +
      "<H1 style='font-family:serif'>Italic Serif H<i>ead</i>linjg 1</H1>" +
      "Plain te<i>ww</i>xtj<p><b>Bold textj</b>", properties);

    PdfDocument pdf = new PdfDocument(new PdfWriter("sample.pdf"));
    Document document = new Document(pdf);
    for (IElement element : elements) {
      document.add(new Paragraph().add((IBlockElement)element));
    }
    document.close();
    Desktop desktop = Desktop.getDesktop();
    desktop.open(new java.io.File("sample.pdf"));
  }
}
