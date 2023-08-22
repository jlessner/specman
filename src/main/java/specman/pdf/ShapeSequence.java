package specman.pdf;

import java.awt.*;

public class ShapeSequence extends Shape {
  private Point offset;

  public ShapeSequence(int x, int y) {
    offset = new Point(x, y);
  }

  public Point translate(Point renderOffset) {
    Point translatedOffset = new Point(renderOffset);
    translatedOffset.translate(offset.x, -offset.y);
    return translatedOffset;
  }

  public ShapeSequence add(Shape subshape) {
    super.add(subshape);
    return this;
  }
}
