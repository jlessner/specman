package specman.view;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class ExperimentalRoundedBorder2 extends AbstractBorder
{
    private static final int THICKNESS = 20;
    float arc;

    public ExperimentalRoundedBorder2(float arc)  {
        this.arc = arc;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;

            RenderingHints rh = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHints(rh);

            Color oldColor = g2d.getColor();
            g2d.setColor(Color.green);

            int offs = THICKNESS;
            int size = offs + offs;

            Shape outer = new java.awt.geom.Rectangle2D.Float((float)x, (float)y, (float)width, (float)height);
            Shape inner = new java.awt.geom.Rectangle2D.Float((float)(x + 10), (float)(y + 1), (float)(width - 20), (float)(height - 2));
            //Shape inner = new java.awt.geom.Rectangle2D.Float((float)(x + offs), (float)(y + offs), (float)(width - size), (float)(height - size));
            //Shape inner = new RoundRectangle2D.Float(x+10, y+1, width-10, height, arc, arc);

//            Shape outer = new Rectangle2D.Float(x, y, width, height);
//            Shape inner = new Rectangle2D.Float(x, y, width, height);
            Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
            path.append(outer, false);
            path.append(inner, false);

            g2d.fill(path);
            g2d.setColor(oldColor);
        }
    }

    /**
     * Reinitialize the insets parameter with this Border's current Insets.
     *
     * @param c the component for which this border insets value applies
     * @param insets the object to be reinitialized
     */
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(THICKNESS, THICKNESS, THICKNESS, THICKNESS);
        return insets;
    }

}
