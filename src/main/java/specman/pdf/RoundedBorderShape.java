package specman.pdf;

import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import specman.view.RoundedBorderDecorator;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class RoundedBorderShape extends Shape {
  RoundRectangle2D decoration;

  public RoundedBorderShape(RoundedBorderDecorator decorator, Shape undecoratedShape) {
    super(decorator);
    decoration = decorator.createInnerShape();
    getSubshapes().add(undecoratedShape);
  }

  private Shape decoratedShape() {
    return getSubshapes().get(0);
  }

  /** Any shape being added beside the one from the constructor is supposed
   * to actually be a sub shape of the decorated compinent rather than the
   * decorator itself. This allows to instanciate the decorator shape already
   * in base class methods and override the instanciation in derived types
   * and add additional child shapes. */
  public Shape add(Shape subshape) {
    decoratedShape().add(subshape);
    return this;
  }

  @Override
  public Shape add(List<? extends Shape> subshapes) {
    decoratedShape().add(subshapes);
    return this;
  }

  @Override
  public Shape withBackgroundColor(Color color) {
    decoratedShape().withBackgroundColor(color);
    return this;
  }

  @Override
  public void applyDecoration(Point renderOffset, float swing2pdfScaleFactor, PdfCanvas pdfCanvas) {
    float pdfWidth = (float) decoration.getWidth() * swing2pdfScaleFactor;
    float pdfHeight = (float) decoration.getHeight() * swing2pdfScaleFactor;
    float pdfX = toPdfX(decoration.getX(), renderOffset, swing2pdfScaleFactor);
    float pdfY = toPdfY(decoration.getY(), renderOffset, swing2pdfScaleFactor) - pdfHeight;
    float pdfArc = (float) decoration.getArcHeight() / 2 * swing2pdfScaleFactor;
    pdfCanvas.roundRectangle(pdfX, pdfY, pdfWidth, pdfHeight, pdfArc);
    pdfCanvas.stroke();
  }
}
