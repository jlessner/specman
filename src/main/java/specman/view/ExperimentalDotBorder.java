package specman.view;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.Line2D;

public class ExperimentalDotBorder extends AbstractBorder {

    public ExperimentalDotBorder() {
        color = Color.red;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x, y, width, height);
        Graphics2D g2d;
        if (g instanceof Graphics2D) {
            g2d = (Graphics2D) g;
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(new Line2D.Double((double)x, (double)y, (double)x + 20, (double)y + 20));
        }
    }

//    @Override
//    public Insets getBorderInsets(Component c) {
//        return (getBorderInsets(c, new Insets(2, 10, 2, 10)));
//    }
//
//    @Override
//    public Insets getBorderInsets(Component c, Insets insets) {
//        insets.left = insets.right = 10;
//        insets.top = insets.bottom = 2;
//        return insets;
//    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    // Variable declarations
    private final Color color;
}