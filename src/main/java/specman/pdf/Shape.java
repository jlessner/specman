package specman.pdf;

import specman.view.AbstractSchrittView;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static specman.view.AbstractSchrittView.LINIENBREITE;

public class Shape {
  private List<Point> path = new ArrayList<>();
  private List<Shape> subshapes = new ArrayList<>();

  public Shape() {}

  public Shape(JComponent component) {
    Rectangle r = component.getBounds();
    add(r.x - LINIENBREITE, r.y - LINIENBREITE)
      .add(r.x + r.width + LINIENBREITE, r.y - LINIENBREITE)
      .add(r.x + r.width + LINIENBREITE, r.y + r.height + LINIENBREITE)
      .add(r.x - LINIENBREITE, r.y + r.height + LINIENBREITE);
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

  public void add(Shape subshape) {
    subshapes.add(subshape);
  }

  public List<Shape> getSubshapes() { return subshapes; }
}
