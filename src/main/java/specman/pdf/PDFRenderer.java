package specman.pdf;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;

import java.awt.*;

import static specman.pdf.Shape.DEFAULT_LINE_COLOR;
import static specman.pdf.Shape.PDF_LINIENBREITE;

public class PDFRenderer {
  //public static float SWING2PDF_SCALEFACTOR = 0.65f;
  public static float SWING2PDF_SCALEFACTOR = 0.7f;
  String pdfFilename;
  PdfWriter writer;
  PdfDocument pdfDoc;
  Document document;
  PdfCanvas pdfCanvas;
  PdfFont labelFont;


  public PDFRenderer(String pdfFilename) {
    try {
      this.pdfFilename = pdfFilename;
      writer = new PdfWriter(pdfFilename);
      pdfDoc = new PdfDocument(writer);
      pdfCanvas = new PdfCanvas(pdfDoc.addNewPage());
      pdfCanvas.setLineWidth(PDF_LINIENBREITE);
      pdfCanvas.setFillColor(Shape.DEFAULT_FILL_COLOR);
      pdfCanvas.setStrokeColor(Shape.DEFAULT_LINE_COLOR);
      document = new Document(pdfDoc);
      labelFont = PdfFontFactory.createFont(FontConstants.HELVETICA);
    }
    catch(Exception x) {
      x.printStackTrace();
    }
  }

  public void render(Shape shape) {
    Point topLeftPDFCorner = new Point(10, 1200);
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
      float scaledFontSize = text.getFontsize() * (SWING2PDF_SCALEFACTOR + 0.05f);
      pdfCanvas.setFillColor(Color.WHITE);
      pdfCanvas.beginText().setFontAndSize(labelFont, scaledFontSize)
        .moveText((renderOffset.x + text.getLeftMargin() -1) * SWING2PDF_SCALEFACTOR, (renderOffset.y +1) * SWING2PDF_SCALEFACTOR - scaledFontSize)
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
