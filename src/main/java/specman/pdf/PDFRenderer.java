package specman.pdf;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import specman.view.AbstractSchrittView;

import java.awt.*;

import static specman.pdf.Shape.DEFAULT_LINE_COLOR;
import static specman.view.AbstractSchrittView.LINIENBREITE;

public class PDFRenderer {
  // This factor turned out to be the one, which causes the PDF-rendered
  // diagramm to get nearly the same size in Acrobat Reader as it appears
  // in the UI on the same monitor. For size comparison, make sure to
  // set the Acrobat Reader's zoom factor in a way, that the pages are actually
  // displayed with the size of an A4 sheet. Hold a sheet on the monitor for
  // zoom factor adaption. On my monitor the Acrobat Reader requires a zoom
  // factor of 85% rather than 100% to display the pages correctly.
  public static float SWING2PDF_SCALEFACTOR_100PERCENT = 0.77f;

  public static PageSize PAGESIZE = PageSize.A4;
  public static final int LEFT_RIGHT_PAGE_MARGIN = 10;
  String pdfFilename;
  PdfWriter writer;
  PdfDocument pdfDoc;
  Document document;
  PdfCanvas pdfCanvas;
  PdfFont labelFont;
  PdfFont textFont;
  float swing2pdfScaleFactor;

  public PDFRenderer(String pdfFilename) {
    try {
      this.pdfFilename = pdfFilename;
      writer = new PdfWriter(pdfFilename);
      pdfDoc = new PdfDocument(writer);
      pdfCanvas = new PdfCanvas(pdfDoc.addNewPage(PAGESIZE));
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
    initScaleFactor(shape);
    Point topLeftCorner = new Point(0, (int)(PAGESIZE.getHeight() / swing2pdfScaleFactor));
    render(shape, topLeftCorner);
  }

  private void initScaleFactor(Shape shape) {
    swing2pdfScaleFactor = SWING2PDF_SCALEFACTOR_100PERCENT;
    int shapeWidth = shape.getWidth();
    float availableWidth100Percent = PAGESIZE.getWidth() / SWING2PDF_SCALEFACTOR_100PERCENT;
    float requiredWidth = shapeWidth + 2 * LEFT_RIGHT_PAGE_MARGIN;
    if (requiredWidth > availableWidth100Percent) {
      swing2pdfScaleFactor *= availableWidth100Percent / requiredWidth;
    }
    pdfCanvas.setLineWidth(((float)LINIENBREITE) * swing2pdfScaleFactor);
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
      float scaledFontSize = text.getFontsize() * swing2pdfScaleFactor;
      pdfCanvas.setFillColor(text.getPDFColor());
      PdfFont font = text.getFont().getFamily().contains("Sans") ? labelFont : textFont;
      pdfCanvas.beginText().setFontAndSize(font, scaledFontSize)
        .moveText((renderOffset.x + text.getInsets().left -1) * swing2pdfScaleFactor, (renderOffset.y - text.getInsets().top +1) * swing2pdfScaleFactor - scaledFontSize)
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
    float x = (p.x + renderOffset.x)*swing2pdfScaleFactor;
    float y = (renderOffset.y - p.y)*swing2pdfScaleFactor;
    return pdfCanvas.moveTo(x, y);
  }

  private PdfCanvas lineTo(Point p, Point renderOffset) {
    float x = (p.x + renderOffset.x)*swing2pdfScaleFactor;
    float y = (renderOffset.y - p.y)*swing2pdfScaleFactor;
    return pdfCanvas.lineTo(x, y);
  }
}
