package specman.pdf;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.properties.LineHeight;
import com.itextpdf.layout.properties.Property;
import org.apache.commons.io.FileUtils;
import specman.textfield.HTMLTags;
import specman.textfield.TextEditArea;
import specman.textfield.TextStyles;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattedShapeText extends AbstractShapeText {
  private static final String HTML2PDF_STYLESHEET = "src/main/resources/stylesheets/specman-pdf.css";
  private static final Pattern FONTSIZE_PATTERN = Pattern.compile("(.*font-size:[\\s]*)([\\d\\.]+)(.+)");

  private static ConverterProperties properties;
  private static String htmlStyles;

  private TextEditArea content;

  public FormattedShapeText(TextEditArea content) {
    super(content.getInsets(), content.getForeground(), content.getFont());
    this.content = content;
  }

  public void writeToPDF(Point renderOffset, PdfCanvas pdfCanvas, com.itextpdf.layout.Document document, float swing2pdfScaleFactor) {
    // Is not really clear why we have to slightly reduce the font size
    float scaledFontSize = getFontsize() * swing2pdfScaleFactor * 0.98f;
    pdfCanvas.setFillColor(getPDFColor());

    document.setFontSize(scaledFontSize);
    String htmlContent = injectStylesheet(content.getText());

    java.util.List<IElement> elements = HtmlConverter.convertToElements(htmlContent, properties);

    Paragraph superp = new Paragraph()
      .setFixedPosition(
        (renderOffset.x + insets.left) * swing2pdfScaleFactor,
        (renderOffset.y - content.getHeight() + getInsets().bottom) * swing2pdfScaleFactor,
        (content.getWidth() - getInsets().left - getInsets().right - 7) * swing2pdfScaleFactor)
      .setMargin(0)
      .setMultipliedLeading(1.0f)
      .setFontSize(scaledFontSize);
    for (IElement element : elements) {
      Paragraph paragraph = new Paragraph()
        .setMargin(0)
        .setMultipliedLeading(1.0f)
        .setCharacterSpacing(0.0f)
        .setFontSize(scaledFontSize);
      paragraph.setProperty(Property.LINE_HEIGHT, LineHeight.createMultipliedValue(1.37f));
      paragraph.add((IBlockElement)element);
      superp.add(paragraph);
      superp.add("\n");
    }
    document.add(superp);
  }

  private String injectStylesheet(String rawHTML) {
    int headEnd = rawHTML.indexOf(HTMLTags.HEAD_OUTRO);
    String frontHTML = rawHTML.substring(0, headEnd-1);
    String tailHTML = rawHTML.substring(headEnd);
    return frontHTML +
      //"<link rel=\"stylesheet\" type=\"text/css\" href=\"" + HTML2PDF_STYLESHEET + "\">" +
      "<style>" + htmlStyles + "</style>" +
      tailHTML;
  }

  @Override
  protected PdfFont getPDFFont() { return null; }

  public static void initFont(int zoompercent) {
    try {
      properties = new ConverterProperties();
      FontProvider fontProvider = new DefaultFontProvider(false, false, false);
      FontProgram fontProgram = FontProgramFactory.createFont(TextStyles.SERIF_FONT);
      fontProvider.addFont(fontProgram);
////      for (int i = 0; i < 6; i++) {
////        FontProgram fontProgram = FontProgramFactory.createFont("src/main/resources/fonts/sitka-small-599.ttf", i, true);
////        fontProvider.addFont(fontProgram);
////      }
      properties.setFontProvider(fontProvider);
      initHTMLStyles(zoompercent);
    }
    catch(IOException iox) {
      iox.printStackTrace();
    }
  }

  /** The method reads Specman's style sheet for PDF rendering and resizes any
   * fonts according to the current zoomfactor. If the zoomfactor is 100%, the
   * method simply returns the stylesheet content as is. */
  private static void initHTMLStyles(int zoompercent) throws IOException {
    float zoomfactor = (float)zoompercent / 100.0f;
    StringBuilder sb = new StringBuilder();
    java.util.List<String> rawStylesheet = FileUtils.readLines(new File(HTML2PDF_STYLESHEET), StandardCharsets.US_ASCII);
    for (String line: rawStylesheet) {
      if (zoompercent != 100) {
        Matcher matcher = FONTSIZE_PATTERN.matcher(line);
        if (matcher.matches()) {
          float rawFontsize = Float.parseFloat(matcher.group(2));
          float zoomedFontsize = rawFontsize * zoomfactor;
          sb.append(matcher.group(1));
          sb.append(zoomedFontsize);
          sb.append(matcher.group(3));
          sb.append("\n");
          continue;
        }
      }
      sb.append(line);
      sb.append("\n");
    }
    htmlStyles = sb.toString();
  }

}
