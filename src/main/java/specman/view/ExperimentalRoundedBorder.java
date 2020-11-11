package specman.view;

import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class ExperimentalRoundedBorder extends AbstractBorder
{
    float arc;

    public ExperimentalRoundedBorder(float arc)  {
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
            g2d.setColor(Color.black);

            int offs = 2;
            int size = offs + offs;
            Shape outer = new RoundRectangle2D.Float(x, y, width, height, arc+2, arc+2);
            Shape inner = new RoundRectangle2D.Float(x + offs, y + offs, width - size, height - size, arc-2, arc-2);
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
        insets.set(2, 2, 2, 2);
        return insets;
    }

}
