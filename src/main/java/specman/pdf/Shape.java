package specman.pdf;

import com.itextpdf.kernel.color.DeviceRgb;
import specman.view.AbstractSchrittView;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static specman.view.AbstractSchrittView.LINIENBREITE;

public class Shape {
  static final int PDF_LINIENBREITE = LINIENBREITE / 2;

  protected List<Point> path = new ArrayList<>();
  protected Color color;
  private List<Shape> subshapes = new ArrayList<>();
  private AbstractSchrittView source;

  public Shape(Component component) {
    this(component, null);
  }

  public Shape(Component component, AbstractSchrittView source) {
    this.source = source;
    Rectangle r = component.getBounds();
    add(r.x - PDF_LINIENBREITE, r.y - PDF_LINIENBREITE)
      .add(r.x + r.width + PDF_LINIENBREITE, r.y - PDF_LINIENBREITE)
      .add(r.x + r.width + PDF_LINIENBREITE, r.y + r.height + PDF_LINIENBREITE)
      .add(r.x - PDF_LINIENBREITE, r.y + r.height + PDF_LINIENBREITE);
    color = component.getBackground();
  }

  public Shape() {

  }

  public Shape add(int x, int y) {
    return add(new Point(x, y));
  }

  public Shape add(Point point) {
    path.add(point);
    return this;
  }

  public Point start() {
    return path.isEmpty() ? null : path.get(0);
  }

  public List<Point> allButStart() { return path.subList(1, path.size()); }

  public Shape add(Shape subshape) {
    subshapes.add(subshape);
    return this;
  }

  public Shape add(List<? extends Shape> subshapes) {
    this.subshapes.addAll(subshapes);
    return this;
  }

  public List<Shape> getSubshapes() { return subshapes; }

  public Point translate(Point renderOffset) {
    return renderOffset;
  }

  public boolean isLine() { return path.size() == 2; }

  public int[] xPositionsAsArray() {
    return path.stream().mapToInt(point -> point.x).toArray();
  }

  public int[] yPositionsAsArray() {
    return path.stream().mapToInt(point -> point.y).toArray();
  }

  public com.itextpdf.kernel.color.Color getPDFColor() {
    if (color == null) {
      return com.itextpdf.kernel.color.Color.WHITE;
    }
    return new DeviceRgb(color.getRed(), color.getGreen(), color.getBlue());
  }
}
