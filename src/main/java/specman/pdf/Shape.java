package specman.pdf;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Shape {
  private List<Point> path = new ArrayList<>();

  public Shape(JComponent component) {
    Rectangle r = component.getBounds();
    add(r.x, r.y)
      .add(r.x + r.width, r.y)
      .add(r.x + r.width, r.y + r.height)
      .add(r.x, r.y + r.height);
  }

  public Shape add(int x, int y) {
    return add(new Point(x, y));
  }

  public Shape add(Point point) {
    path.add(point);
    return this;
  }

  public Point start() {
    return path.get(0);
  }

  public List<Point> allButStart() { return path.subList(1, path.size()); }
}
