package experiments.itext;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.TrueTypeFont;
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
import specman.editarea.TextStyles;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    FontProvider fontProvider = new DefaultFontProvider(false, false, false);

    assertEquals(4, fontProvider.addDirectory("src/main/resources/fonts/courierprime"));
    assertEquals(4, fontProvider.addDirectory("src/main/resources/fonts/sitka"));
    assertEquals(4, fontProvider.addDirectory("src/main/resources/fonts/roboto"));
    fontProvider.addFont(FontProgramFactory.createFont());

    properties.setFontProvider(fontProvider);

    PdfDocument pdf = new PdfDocument(new PdfWriter("sample.pdf"));
    Document document = new Document(pdf);

    String html = "" +
        //"<html><head><style> body { font-family: sitka; } </style></head>\n" +
        "<H1 style='font-family:serif'>Serif Headwinjg 1</H1>" +
        "<H1 style=\"font-family:roboto\">Roboto</H1>" +
        "<H1>Standard Style</H1>" +
        "<H1 style=\"font-family:sitka\">Sitka</H1>" +
        "<H1 style=\"font-family:'Courier Prime'\">Courier Prime 123</H1>" +
        "<span style=\"font-family: 'Courier Prime'\">Normal-sized Courier Prime 123</span><br>" +
        "<font face=\"'Courier Prime'\">Courier Prime by font element</font><br>" +
        "<font face=\"roboto\">Helvetica Roboto by font element 123</font><br>" +
        "<font face=\"helvetica\">Helvetica by font element 123</font><br>" +
        "Plain 153te<i>ww</i>xtj<p><b>Bold textj</b>";

    HtmlConverter.convertToPdf(html, pdf, properties);

//    java.util.List<IElement> elements = HtmlConverter.convertToElements(html, properties);
//    for (IElement element : elements) {
//      document.add(new Paragraph().add((IBlockElement)element));
//    }

    document.close();
    Desktop desktop = Desktop.getDesktop();
    desktop.open(new java.io.File("sample.pdf"));
  }

  @Test
  void testITextFontTutorial_StandardType1Fonts() throws Exception {
    HtmlConverter.convertToPdf(new File("src/test/java/experiments/itext/fonts_standardtype1.html"), new File("sample.pdf"));
    Desktop desktop = Desktop.getDesktop();
    desktop.open(new java.io.File("sample.pdf"));
  }

  @Test
  void testITextFontTutorial_FontsShippedWithIText() throws Exception {
    HtmlConverter.convertToPdf(new File("src/test/java/experiments/itext/fonts_shipped.html"), new File("sample.pdf"));
    Desktop desktop = Desktop.getDesktop();
    desktop.open(new java.io.File("sample.pdf"));
  }

  @Test
  void testITextFontTutorial_SystemFonts() throws Exception {
    ConverterProperties properties = new ConverterProperties();
    properties.setFontProvider(new DefaultFontProvider(true, true, true));
    HtmlConverter.convertToPdf(new File("src/test/java/experiments/itext/fonts_system.html"), new File("sample.pdf"), properties);
    Desktop desktop = Desktop.getDesktop();
    desktop.open(new java.io.File("sample.pdf"));
  }

  @Test
  void testITextFontTutorial_AddingSelectedFonts() throws Exception {
    //String FONT = "src/main/resources/fonts/cardo/Cardo-Regular.ttf";
    String FONT = "src/main/resources/fonts/courier-prime/Courier-Prime.ttf";
    ConverterProperties properties = new ConverterProperties();
    FontProvider fontProvider = new DefaultFontProvider();
    FontProgram fontProgram = FontProgramFactory.createFont(FONT);
    fontProvider.addFont(fontProgram);
    properties.setFontProvider(fontProvider);
    HtmlConverter.convertToPdf(new File("src/test/java/experiments/itext/fonts_extra.html"), new File("sample.pdf"), properties);
    Desktop desktop = Desktop.getDesktop();
    desktop.open(new java.io.File("sample.pdf"));
  }

}
