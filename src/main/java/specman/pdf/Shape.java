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
  static final int PDF_LINIENBREITE = LINIENBREITE / 2;

  protected List<Point> path = new ArrayList<>();
  protected Color backgroundColor;
  protected boolean withBorder;
  private List<Shape> subshapes = new ArrayList<>();
  private Object source;

  public Shape(Component component) {
    this(component, true, null);
  }

  public Shape(Component component, AbstractSchrittView source) {
    this(component, true, source);
  }

  public Shape(Component component, boolean withBorder, Object source) {
    this.withBorder = withBorder;
    this.source = source;
    Rectangle r = component.getBounds();
    int pathExtension = withBorder ? PDF_LINIENBREITE : 0;
    add(r.x - pathExtension, r.y - pathExtension)
      .add(r.x + r.width + pathExtension, r.y - pathExtension)
      .add(r.x + r.width + pathExtension, r.y + r.height + pathExtension)
      .add(r.x - pathExtension, r.y + r.height + pathExtension);
    backgroundColor = component.getBackground();
  }

  public Shape() {
    withBorder(true);
  }

  public Shape withBackgroundColor(Color color) {
    this.backgroundColor = color;
    return this;
  }

  public Shape withBorder(boolean withBorder) {
    this.withBorder = withBorder;
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

  public com.itextpdf.kernel.color.Color getPDFBackgroundColor() {
    return toPDFColor(backgroundColor, DEFAULT_FILL_COLOR);
  }

  private static com.itextpdf.kernel.color.Color toPDFColor(Color awtColor, com.itextpdf.kernel.color.Color fallback) {
    if (awtColor == null) {
      return fallback;
    }
    return new DeviceRgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
  }


}
