package specman.pdf;

import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;

import java.awt.*;

public class PDFRenderer {
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
      pdfCanvas.setFillColor(Color.WHITE);
      pdfCanvas.setLineWidth(1);
      document = new Document(pdfDoc);
    }
    catch(Exception x) {
      x.printStackTrace();
    }
  }

  public void render(Shape shape) {
    try {
      drawShape(shape);
      document.close();
      Desktop.getDesktop().open(new java.io.File(pdfFilename));
    }
    catch(Exception x) {
      x.printStackTrace();
    }
  }

  private void drawShape(Shape shape) {
    moveTo(shape.start());
    shape.allButStart().forEach(p -> lineTo(p));
    lineTo(shape.start());
    pdfCanvas.fill();
    moveTo(shape.start());
    shape.allButStart().forEach(p -> lineTo(p));
    lineTo(shape.start());
    pdfCanvas.stroke();
  }

  private PdfCanvas moveTo(Point p) {
    return pdfCanvas.moveTo(p.x + 10, 800 - p.y);
  }

  private PdfCanvas lineTo(Point p) {
    return pdfCanvas.lineTo(p.x + 10, 800 - p.y);
  }
}
