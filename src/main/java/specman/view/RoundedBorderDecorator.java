package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import specman.Specman;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class RoundedBorderDecorator extends JPanel {
  private static final int INSET = 15;
  private static final int INNER_BORDERLINE_WIDTH = 2;
  private static final int ARC_SIZE = 25;

  private final JComponent decoratedComponent;
  private final FormLayout layout;
  private boolean withTopInset;
  private int inset;
  private int arc;

  public RoundedBorderDecorator(JComponent componentToDecorate) {
    inset = INSET;
    arc = ARC_SIZE;
    setBackground(Color.white);
    String commonInsetSpec = (inset + INNER_BORDERLINE_WIDTH) + "px";
    layout = new FormLayout("0px,10px:grow,0px", "0px,fill:pref:grow,0px");
    setLayout(layout);
    add(componentToDecorate, CC.xy(2, 2));
    decoratedComponent = componentToDecorate;
    withTopInset = false;
    skalieren(Specman.instance().getZoomFactor());
  }

  public void withTopInset(boolean withTopInset) {
    this.withTopInset = withTopInset;
    int topInset = withTopInset ? inset + INNER_BORDERLINE_WIDTH : INNER_BORDERLINE_WIDTH;
    layout.setRowSpec(1, RowSpec.decode(topInset + "px"));
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    if (g instanceof Graphics2D) {
      Graphics2D g2d = (Graphics2D) g;
      Color originalColor = g2d.getColor();

      int borderX = inset + (INNER_BORDERLINE_WIDTH / 2);
      int borderY = withTopInset ? borderX : INNER_BORDERLINE_WIDTH / 2;
      int borderWidthMinus = borderX * 2;
      int borderHeightMinus = borderX + borderY;

      antialiasingOn(g2d);
      drawOuterBorderArea(g2d, borderX, borderY, borderWidthMinus, borderHeightMinus);
      drawInnerBorderLine(g2d, borderX, borderY, borderWidthMinus, borderHeightMinus);

      g2d.setColor(originalColor);
    }
  }

  private void antialiasingOn(Graphics2D g2d) {
    RenderingHints rh = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHints(rh);
  }

  private void drawInnerBorderLine(Graphics2D g2d,
      int borderX, int borderY, int borderWidthMinus, int borderHeightMinus) {
    g2d.setColor(Color.black);
    g2d.setStroke(new BasicStroke(2));
    g2d.draw(new RoundRectangle2D.Float(
        borderX,
        borderY,
        (float)(getWidth() - borderWidthMinus),
        (float)(getHeight() - borderHeightMinus),
        arc, arc));
  }

  private void drawOuterBorderArea(Graphics2D g2d,
      int borderX, int borderY, int borderWidthMinus, int borderHeightMinus) {
    g2d.setColor(Color.white);
    Shape outer = new java.awt.geom.Rectangle2D.Float(0, 0, getWidth(), getHeight());
    Shape inner = new RoundRectangle2D.Float(
        borderX,
        borderY,
        (float)(getWidth() - borderWidthMinus),
        (float)(getHeight() - borderHeightMinus),
        arc, arc);
    Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
    path.append(outer, false);
    path.append(inner, false);
    g2d.fill(path);
  }

  public JComponent getDecoratedComponent() { return decoratedComponent; }

  public boolean withTopInset() { return withTopInset; }

  public void skalieren(int percent) {
    inset = (INSET * percent) / 100;
    arc = (ARC_SIZE * percent) / 100;
    withTopInset(withTopInset);
    String otherInsetSpec = (inset + INNER_BORDERLINE_WIDTH) + "px";
    ColumnSpec columnSpec = ColumnSpec.decode(otherInsetSpec);
    layout.setColumnSpec(1, columnSpec);
    layout.setColumnSpec(3, columnSpec);
    layout.setRowSpec(3, RowSpec.decode(otherInsetSpec));
  }

}
