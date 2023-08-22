package specman.pdf;

import specman.view.AbstractSchrittView;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static specman.view.AbstractSchrittView.LINIENBREITE;

public class Shape {
  static final int PDF_LINIENBREITE = LINIENBREITE / 2;

  private List<Point> path = new ArrayList<>();
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

  public List<Shape> getSubshapes() { return subshapes; }

  public Point translate(Point renderOffset) {
    return renderOffset;
  }
}
