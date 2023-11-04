package specman.pdf;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.font.FontProvider;
import org.apache.commons.io.FileUtils;
import specman.textfield.HTMLTags;
import specman.textfield.litrack.LITrackingListView;
import specman.textfield.TextEditArea;
import specman.textfield.TextStyles;

import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static specman.pdf.ListItemPromptFactory.LISTITEM_IDENTION;
import static specman.pdf.PDFRenderer.SWING2PDF_SCALEFACTOR_100PERCENT;

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

  public void writeToPDF(Point renderOffset, float swing2pdfScaleFactor, PdfCanvas pdfCanvas, Document document) {
    float scaledFontSize = getFontsize() * swing2pdfScaleFactor;
    pdfCanvas.setFillColor(getPDFColor());
    document.setFontSize(scaledFontSize);

    try {
      java.util.List<TextlineDimension> lines = scanLineDimensions();
      float paragraphWidth = (content.getWidth() - getInsets().left - getInsets().right) * swing2pdfScaleFactor;
      ListItemPromptFactory listItemPromptFactory = new ListItemPromptFactory();

      for (TextlineDimension line: lines) {
        System.out.println("Zeile bis " + line.getDocIndexTo() + ", Höhe " + line.getHeight() + ", y = " + line.getY());

        String lineHtml = line.extractLineHtml(content);
        Integer liIndex = LITrackingListView.isLILine(content.getDocument(), line.getY());
        String lineItemPrompt = listItemPromptFactory.createPrompt(lineHtml, liIndex);
        lineHtml = removeLinebreakingElementsFromHtmlLine(lineHtml);
        lineHtml = stylifyTextAlignment(lineHtml);
        lineHtml = injectStylesheet(lineHtml);
        java.util.List<IElement> elements = HtmlConverter.convertToElements(lineHtml, properties);

        Paragraph p = new Paragraph()
          .setMargin(0)
          .setMultipliedLeading(0.0f)
          .setCharacterSpacing(-0.1f)
          .setFontSize(scaledFontSize)
          .setFixedPosition(
          (renderOffset.x + line.getX()) * swing2pdfScaleFactor,
          (renderOffset.y - line.getY() - line.getHeight()) * swing2pdfScaleFactor,
          paragraphWidth);
        p.add((IBlockElement)elements.get(0));

        document.add(p);

        if (lineItemPrompt != null) {
          p = new Paragraph()
            .setMargin(0)
            .setMultipliedLeading(0.0f)
            .setCharacterSpacing(-0.1f)
            .setFontSize(scaledFontSize)
            .setFixedPosition(
              (renderOffset.x + line.getX() - LISTITEM_IDENTION) * swing2pdfScaleFactor,
              (renderOffset.y - line.getY() - line.getHeight()) * swing2pdfScaleFactor,
              (LISTITEM_IDENTION - 5) * swing2pdfScaleFactor);
          elements = HtmlConverter.convertToElements(lineItemPrompt, properties);
          p.add((IBlockElement)elements.get(0));
          document.add(p);
        }
      }

    }
    catch(Exception x) {
      x.printStackTrace();
    }


//    String htmlContent = injectStylesheet(stylifyTextAlignment(content.getText()));
//
//    java.util.List<IElement> elements = HtmlConverter.convertToElements(htmlContent, properties);
//
//    float paragraphWidth = (content.getWidth() - getInsets().left - getInsets().right - 7) * swing2pdfScaleFactor;
//    Paragraph superp = new Paragraph()
//      .setFixedPosition(
//        (renderOffset.x + insets.left) * swing2pdfScaleFactor,
//        (renderOffset.y - content.getHeight() + getInsets().bottom) * swing2pdfScaleFactor,
//        paragraphWidth)
//      .setMargin(0)
//      .setMultipliedLeading(-0.0f)
//      .setFontSize(scaledFontSize);
//    for (IElement element : elements) {
//      Paragraph paragraph = new Paragraph()
//        .setMargin(0)
//        .setMultipliedLeading(0.0f)
//        .setCharacterSpacing(-0.1f)
//        .setFontSize(scaledFontSize)
//        .setWidth(paragraphWidth); // Setting width for sub paragraph is important for text alignments right, center, ...
//      paragraph.setProperty(Property.LINE_HEIGHT, LineHeight.createMultipliedValue(1.37f));
//      paragraph.add((IBlockElement)element);
//      superp.add(paragraph);
//      superp.add("\n");
//    }
//    document.add(superp);
  }

  private String removeLinebreakingElementsFromHtmlLine(String subHtml) {
    return subHtml
      .replace("<li>", "")
      .replace("</li>", "")
      .replace("<ol>", "")
      .replace("</ol>", "")
      .replace("<ul>", "")
      .replace("</ul>", "")
      .replace("<br>", "");
  }

  private List<TextlineDimension> scanLineDimensions() throws BadLocationException {
    List<TextlineDimension> lines = new ArrayList<>();
    javax.swing.text.Document document = content.getDocument();
    for (int rowStart = 1; rowStart < document.getLength(); rowStart++) {
      Rectangle2D lineSpace = content.modelToView2D(rowStart);
      int rowEnd = Utilities.getRowEnd(content, rowStart);
      lines.add(new TextlineDimension(rowStart, rowEnd, lineSpace));
      rowStart = rowEnd;
    }
    return lines;
  }

  /** JEditorPane expresses text alignment by plain HTML form in div elements like that:
   * <div align="right">text goes here</div>
   * This is not suitable für pdf2html which only supports text alignment to be expressed by styling like that:
   * <div style="text-align: right">text goes here</div>
   * This must be morphed before rendering. Fortunately there occurs no other styling of divs in JEditorPane, so
   * we can simply work with an ordinary regexp replacement without merging. */
  private String stylifyTextAlignment(String rawHTML) {
    return rawHTML.replaceAll("align=\"([a-z]+)\"", "style=\"text-align:$1\"");
  }

  private String injectStylesheet(String rawHTML) {
    int headEnd = rawHTML.indexOf(HTMLTags.HEAD_OUTRO);
    String frontHTML = rawHTML.substring(0, headEnd-1);
    String tailHTML = rawHTML.substring(headEnd);
    return frontHTML + "<style>" + htmlStyles + "</style>" + tailHTML;
  }

  @Override
  protected PdfFont getPDFFont() { return null; }

  public static void initFont(int uizoomfactor, float swing2pdfScaleFactor) {
    try {
      properties = new ConverterProperties();
      FontProvider fontProvider = new DefaultFontProvider(false, false, false);
      fontProvider.addFont(FontProgramFactory.createFont(TextStyles.SERIF_FONTCOLLECTION_REGULAR, TextStyles.FONT_INDEX, false));
      fontProvider.addFont(FontProgramFactory.createFont("src/main/resources/fonts/SitkaB.ttc", TextStyles.FONT_INDEX, false));
      fontProvider.addFont(FontProgramFactory.createFont("src/main/resources/fonts/SitkaI.ttc", TextStyles.FONT_INDEX, false));
      fontProvider.addFont(FontProgramFactory.createFont("src/main/resources/fonts/SitkaZ.ttc", TextStyles.FONT_INDEX, false));
      properties.setFontProvider(fontProvider);
      initHTMLStyles(uizoomfactor, swing2pdfScaleFactor);
    }
    catch(IOException iox) {
      iox.printStackTrace();
    }
  }

  /** The method reads Specman's style sheet for PDF rendering and resizes any
   * fonts according to the UI zoom factor and the scaling factor required to
   * fit the diagramm into the maximum PDF page width. If the factors have the
   * default of 100% UI and {@link PDFRenderer#SWING2PDF_SCALEFACTOR_100PERCENT},
   * the method simply returns the stylesheet content as is. */
  private static void initHTMLStyles(int uiZoomfactor, float swing2pdfScaleFactor) throws IOException {
    float fontShrinkingFactor = swing2pdfScaleFactor / SWING2PDF_SCALEFACTOR_100PERCENT;
    float zoomAndScalefactor = fontShrinkingFactor * uiZoomfactor / 100.0f;
    //float zoomAndScalefactor = uiZoomfactor / 100.0f;
    StringBuilder sb = new StringBuilder();
    java.util.List<String> rawStylesheet = FileUtils.readLines(new File(HTML2PDF_STYLESHEET), StandardCharsets.US_ASCII);
    for (String line: rawStylesheet) {
      if (uiZoomfactor != 100 || swing2pdfScaleFactor != SWING2PDF_SCALEFACTOR_100PERCENT) {
        Matcher matcher = FONTSIZE_PATTERN.matcher(line);
        if (matcher.matches()) {
          float rawFontsize = Float.parseFloat(matcher.group(2));
          float zoomedFontsize = rawFontsize * zoomAndScalefactor;
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
