package specman.pdf;

import java.awt.*;

public class ShapeSequence extends Shape {
  public ShapeSequence(int x, int y) {
    add(x, y);
  }

  public ShapeSequence add(Shape subshape) {
    super.add(subshape);
    return this;
  }
}
