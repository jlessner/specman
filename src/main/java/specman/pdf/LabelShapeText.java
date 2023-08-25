package specman.pdf;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.awt.*;

public class LabelShapeText extends AbstractShapeText {

  private static PdfFont LABEL_FONT;

  private String content;

  public LabelShapeText(String content, Insets insets, Color color, Font font) {
    super(insets, color, font);
    this.content = content;
  }

  public static void initFont() {
    LABEL_FONT = createFont("Helvetica");
  }

  public void writeToPDF(Point renderOffset, PdfCanvas pdfCanvas, float swing2pdfScaleFactor) {
    float scaledFontSize = getFontsize() * swing2pdfScaleFactor;
    pdfCanvas.setFillColor(getPDFColor());
    pdfCanvas.beginText().setFontAndSize(getPDFFont(), scaledFontSize)
      .moveText(
        (renderOffset.x + insets.left -1) * swing2pdfScaleFactor,
        (renderOffset.y - insets.top +1) * swing2pdfScaleFactor - scaledFontSize)
      .showText(getContent())
      .endText();
  }

  public String getContent() { return content; }

  @Override
  protected PdfFont getPDFFont() { return LABEL_FONT; }
}
