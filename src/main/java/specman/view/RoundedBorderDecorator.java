package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class RoundedBorderDecorator extends JPanel {
  private static final int INSET = 10;
  private static final int INNER_BORDERLINE_WIDTH = 2;
  private static final int ARC_SIZE = 25;

  public RoundedBorderDecorator(JComponent componentToDecorate) {
    setBackground(Color.white);
    String layoutInsetSpec = (INSET + INNER_BORDERLINE_WIDTH) + "px";
    setLayout(new FormLayout(
        layoutInsetSpec + ",10px:grow," + layoutInsetSpec,
        layoutInsetSpec + ",fill:pref:grow," + layoutInsetSpec));
    add(componentToDecorate, CC.xy(2, 2));
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    if (g instanceof Graphics2D) {
      Graphics2D g2d = (Graphics2D) g;
      Color originalColor = g2d.getColor();
      int offsetTopAndLeft = INSET + (INNER_BORDERLINE_WIDTH / 2);
      int offsetBottomAndRight = offsetTopAndLeft * 2;

      antialiasingOn(g2d);
      drawOuterBorderArea(g2d, offsetTopAndLeft, offsetBottomAndRight);
      drawInnerBorderLine(g2d, offsetTopAndLeft, offsetBottomAndRight);

      g2d.setColor(originalColor);
    }
  }

  private void antialiasingOn(Graphics2D g2d) {
    RenderingHints rh = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHints(rh);
  }

  private void drawInnerBorderLine(Graphics2D g2d, int offsetTopAndLeft, int offsetBottomAndRight) {
    g2d.setColor(Color.black);
    g2d.setStroke(new BasicStroke(2));
    g2d.draw(new RoundRectangle2D.Float(
        offsetTopAndLeft,
        offsetTopAndLeft,
        (float)(getWidth() - offsetBottomAndRight),
        (float)(getHeight() - offsetBottomAndRight),
        ARC_SIZE, ARC_SIZE));
  }

  private void drawOuterBorderArea(Graphics2D g2d, int offsetTopAndLeft, int offsetBottomAndRight) {
    g2d.setColor(Color.white);
    Shape outer = new java.awt.geom.Rectangle2D.Float(0, 0, getWidth(), getHeight());
    Shape inner = new RoundRectangle2D.Float(
        offsetTopAndLeft,
        offsetTopAndLeft,
        (float)(getWidth() - offsetBottomAndRight),
        (float)(getHeight() - offsetBottomAndRight),
        ARC_SIZE, ARC_SIZE);
    Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
    path.append(outer, false);
    path.append(inner, false);
    g2d.fill(path);
  }
}
