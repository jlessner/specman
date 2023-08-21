package specman.pdf;

import specman.view.AbstractSchrittView;

import java.awt.*;

import static specman.view.AbstractSchrittView.LINIENBREITE;

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

  public void translateOffset(Point parentStart) {
    offset.translate(parentStart.x + LINIENBREITE, parentStart.y + LINIENBREITE);
  }
}
