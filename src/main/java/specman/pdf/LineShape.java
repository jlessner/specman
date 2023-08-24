package specman.pdf;

import java.awt.*;

public class LineShape extends Shape {
  public LineShape(int fromX, int fromY, int toX, int toY) {
    this(new Point(fromX, fromY), new Point(toX, toY));
  }

  public LineShape(Point from, Point to) {
    add(from).add(to);
  }

  public Point end() {
    return path.get(1);
  }

}
