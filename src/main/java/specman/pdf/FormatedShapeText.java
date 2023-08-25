package specman.pdf;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import specman.EditException;
import specman.textfield.TextEditArea;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;
import java.awt.*;

public class FormatedShapeText extends AbstractShapeText {

  public static PdfFont TEXT_FONT;

  private TextEditArea content;

  public FormatedShapeText(TextEditArea content) {
    super(content.getInsets(), content.getForeground(), content.getFont());
    this.content = content;
  }

  public void writeToPDF(Point renderOffset, PdfCanvas pdfCanvas, float swing2pdfScaleFactor) {
    try {
      float scaledFontSize = getFontsize() * swing2pdfScaleFactor;
      pdfCanvas.setFillColor(getPDFColor()).setFontAndSize(getPDFFont(), scaledFontSize);

      for (int pos = 1; pos < content.getDocument().getLength(); pos++) {
        int lineEnd = Utilities.getRowEnd(content, pos);
        int lineHeight = (int)content.modelToView2D(pos).getHeight();
        String lineContent = content.getDocument().getText(pos, lineEnd - pos);
        System.out.println("Zeile bis " + pos + ", Höhe " + lineHeight + ", Inhalt '" + lineContent + "'");

        pdfCanvas.beginText().moveText(
            (renderOffset.x + insets.left) * swing2pdfScaleFactor,
            (renderOffset.y - insets.top) * swing2pdfScaleFactor - scaledFontSize)
          .showText(lineContent)
          .endText();

        renderOffset = new Point(renderOffset);
        renderOffset.translate(0, -lineHeight);
        pos = lineEnd;
      }

    }
    catch(BadLocationException blx) {
      throw new RuntimeException(blx);
    }
  }

  @Override
  protected PdfFont getPDFFont() { return TEXT_FONT; }

  public static void initFont() {
    TEXT_FONT = createFont(FontConstants.TIMES_ROMAN);
  }

}
