package specman.pdf;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;

import java.awt.*;

import static specman.pdf.Shape.DEFAULT_LINE_COLOR;
import static specman.pdf.Shape.PDF_LINIENBREITE;

public class PDFRenderer {
  public static float SWING2PDF_SCALEFACTOR = 0.7f;
  public static PageSize PAGESIZE = PageSize.A4;
  String pdfFilename;
  PdfWriter writer;
  PdfDocument pdfDoc;
  Document document;
  PdfCanvas pdfCanvas;
  PdfFont labelFont;
  PdfFont textFont;

  public PDFRenderer(String pdfFilename) {
    try {
      this.pdfFilename = pdfFilename;
      writer = new PdfWriter(pdfFilename);
      pdfDoc = new PdfDocument(writer);
      pdfCanvas = new PdfCanvas(pdfDoc.addNewPage(PAGESIZE));
      pdfCanvas.setLineWidth(PDF_LINIENBREITE);
      pdfCanvas.setFillColor(Shape.DEFAULT_FILL_COLOR);
      pdfCanvas.setStrokeColor(Shape.DEFAULT_LINE_COLOR);
      document = new Document(pdfDoc);
      labelFont = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);
      textFont = PdfFontFactory.createFont(FontConstants.TIMES_ROMAN);
    }
    catch(Exception x) {
      x.printStackTrace();
    }
  }

  public void render(Shape shape) {
    Point topLeftPDFCorner = new Point(10, (int)(PAGESIZE.getHeight() / SWING2PDF_SCALEFACTOR));
    render(shape, topLeftPDFCorner);
  }

  private void render(Shape shape, Point renderOffset) {
    try {
      drawShape(shape, renderOffset);
      document.close();
      Desktop.getDesktop().open(new java.io.File(pdfFilename));
    }
    catch(Exception x) {
      x.printStackTrace();
    }
  }

  private void drawShape(Shape shape, Point renderOffset) {
    if (shape.hasForm()) {
        if (!shape.isLine()) {
          pdfCanvas.setFillColor(shape.getPDFBackgroundColor());
          runPath(shape, renderOffset);
          // This looks wrong and is probably caused by a lack of knowledge of the iText API:
          // If we run the shape's path *twice*, the gap between the shapes shows up with a
          // reasonable width. If we run the path only *once*, the gaps appear thinner than
          // expected when displaying the PDF in Acrobat Reader in 100% resolution. The
          // solution with the double-run was an accidential finding.
          runPath(shape, renderOffset);
          pdfCanvas.fill();
        }
        if (shape.withOutline()) {
          pdfCanvas.setStrokeColor(DEFAULT_LINE_COLOR);
          runPath(shape, renderOffset);
          pdfCanvas.stroke();
        }
    }
    renderOffset = shape.translate(renderOffset);
    writeShapeText(shape, renderOffset);
    for (Shape subshape: shape.getSubshapes()) {
      drawShape(subshape, renderOffset);
    }
  }

  private void writeShapeText(Shape shape, Point renderOffset) {
    ShapeText text = shape.getText();
    if (text != null) {
      float scaledFontSize = text.getFontsize() * SWING2PDF_SCALEFACTOR;
      pdfCanvas.setFillColor(text.getPDFColor());
      PdfFont font = text.getFont().getFamily().contains("Sans") ? labelFont : textFont;
      pdfCanvas.beginText().setFontAndSize(font, scaledFontSize)
        .moveText((renderOffset.x + text.getInsets().left -1) * SWING2PDF_SCALEFACTOR, (renderOffset.y - text.getInsets().top +1) * SWING2PDF_SCALEFACTOR - scaledFontSize)
        .showText(text.getContent())
        .endText();
    }
  }

  private void runPath(Shape shape, Point renderOffset) {
    moveTo(shape.start(), renderOffset);
    shape.allButStart().forEach(p -> lineTo(p, renderOffset));
    lineTo(shape.start(), renderOffset);
  }

  private PdfCanvas moveTo(Point p, Point renderOffset) {
    float x = (p.x + renderOffset.x)*SWING2PDF_SCALEFACTOR;
    float y = (renderOffset.y - p.y)*SWING2PDF_SCALEFACTOR;
    return pdfCanvas.moveTo(x, y);
  }

  private PdfCanvas lineTo(Point p, Point renderOffset) {
    float x = (p.x + renderOffset.x)*SWING2PDF_SCALEFACTOR;
    float y = (renderOffset.y - p.y)*SWING2PDF_SCALEFACTOR;
    return pdfCanvas.lineTo(x, y);
  }
}
