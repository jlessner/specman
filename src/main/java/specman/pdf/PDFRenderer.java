package specman.pdf;

import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;

import java.awt.*;

import static com.itextpdf.kernel.color.Color.WHITE;
import static specman.pdf.Shape.PDF_LINIENBREITE;

public class PDFRenderer {
  public static float SWING2PDF_SCALEFACTOR = 0.65f;
  String pdfFilename;
  PdfWriter writer;
  PdfDocument pdfDoc;
  Document document;
  PdfCanvas pdfCanvas;

  public PDFRenderer(String pdfFilename) {
    try {
      this.pdfFilename = pdfFilename;
      writer = new PdfWriter(pdfFilename);
      pdfDoc = new PdfDocument(writer);
      pdfCanvas = new PdfCanvas(pdfDoc.addNewPage());
      pdfCanvas.setLineWidth(PDF_LINIENBREITE);
      document = new Document(pdfDoc);
    }
    catch(Exception x) {
      x.printStackTrace();
    }
  }

  public void render(Shape shape) {
    Point topLeftPDFCorner = new Point(10, 1300);
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
    renderOffset = shape.translate(renderOffset);
    if (shape.start() != null) {
      if (!shape.isLine()) {
        pdfCanvas.setFillColor(shape.getPDFColor());
        runPath(shape, renderOffset);
        pdfCanvas.fill();
      }
      runPath(shape, renderOffset);
      pdfCanvas.stroke();
      renderOffset = new Point(renderOffset);
      renderOffset.translate(shape.start().x + PDF_LINIENBREITE, -shape.start().y - PDF_LINIENBREITE);
    }
    for (Shape subshape: shape.getSubshapes()) {
      drawShape(subshape, renderOffset);
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
