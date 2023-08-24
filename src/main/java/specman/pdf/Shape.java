package specman.pdf;

import com.itextpdf.kernel.color.DeviceRgb;
import specman.view.AbstractSchrittView;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static specman.view.AbstractSchrittView.LINIENBREITE;

public class Shape {
  public static final com.itextpdf.kernel.color.Color DEFAULT_FILL_COLOR = com.itextpdf.kernel.color.Color.WHITE;
  public static final com.itextpdf.kernel.color.Color DEFAULT_LINE_COLOR = com.itextpdf.kernel.color.Color.BLACK;
  public static final Color GAP_COLOR = Color.BLACK;
  static final int PDF_LINIENBREITE = LINIENBREITE / 2;

  protected List<Point> path = new ArrayList<>();
  protected Color backgroundColor;
  private List<Shape> subshapes = new ArrayList<>();
  private Object source;
  private boolean withOutline;
  private ShapeText text;

  public Shape() {}

  public Shape(Component component) {
    this(component, null);
  }

  public Shape(Component component, Object source) {
    this.source = source;
    Rectangle r = component.getBounds();
    add(r.x, r.y)
      .add(r.x + r.width, r.y)
      .add(r.x + r.width, r.y + r.height)
      .add(r.x, r.y + r.height);
    backgroundColor = component.getBackground();
  }

  public Shape(Point start) {
    add(start.x, start.y);
  }

  public Shape withBackgroundColor(Color color) {
    this.backgroundColor = color;
    return this;
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
    if (subshape != null) {
      subshapes.add(subshape);
    }
    return this;
  }

  public Shape add(List<? extends Shape> subshapes) {
    this.subshapes.addAll(subshapes);
    return this;
  }

  public Shape add(Component subComponent) {
    return (subComponent != null)
      ? add(new Shape(subComponent, subComponent))
      : this;
  }

  public List<Shape> getSubshapes() { return subshapes; }

  public Point translate(Point renderOffset) {
    Point translatedOffset = new Point(renderOffset);
    translatedOffset.translate(start().x, -start().y);
    return translatedOffset;
  }

  public boolean isLine() { return path.size() == 2; }

  public int[] xPositionsAsArray() {
    return path.stream().mapToInt(point -> point.x).toArray();
  }

  public int[] yPositionsAsArray() {
    return path.stream().mapToInt(point -> point.y).toArray();
  }

  public com.itextpdf.kernel.color.Color getPDFBackgroundColor() {
    return toPDFColor(backgroundColor, DEFAULT_FILL_COLOR);
  }

  private static com.itextpdf.kernel.color.Color toPDFColor(Color awtColor, com.itextpdf.kernel.color.Color fallback) {
    if (awtColor == null) {
      return fallback;
    }
    return new DeviceRgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
  }

  public boolean hasForm() { return path.size() > 0; }

  public boolean withOutline() {
    return withOutline || isLine();
  }

  public Shape withOutline(boolean withOutline) {
    this.withOutline = withOutline;
    return this;
  }

  public Shape withText(ShapeText text) {
    this.text = text;
    return this;
  }

  public ShapeText getText() {
    return text;
  }
}
