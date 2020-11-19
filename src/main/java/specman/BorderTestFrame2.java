package specman;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class BorderTestFrame2 extends JFrame {

    BorderTestFrame2() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(200, 200);

        setLayout(new BorderLayout());

        //JPanel p = new JPanel();
        //JTextField p = new JTextField("Text");
        JEditorPane p = new JEditorPane();
        //p.setBackground(Color.lightGray);

        JPanel panel = new FramePanel(p);
        getContentPane().add(panel, BorderLayout.CENTER);

        setVisible(true);
    }

    public static void main(String[] args) {
        new BorderTestFrame2();
    }

    public static class FramePanel extends JPanel {
        public FramePanel(JComponent picture) {
            //setBackground(Color.green.darker());
            setBackground(Color.white);
            setLayout(new FormLayout("12px,10px:grow,12px", "12px,fill:pref:grow,12px"));
            add(picture, CC.xy(2, 2));
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (g instanceof Graphics2D) {
                Graphics2D g2d = (Graphics2D) g;
                RenderingHints rh = new RenderingHints(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHints(rh);
                g2d.setColor(Color.white);
                Shape outer = new java.awt.geom.Rectangle2D.Float((float)0, (float)0, (float)getWidth(), (float)getHeight());
                //Shape inner = new java.awt.geom.Rectangle2D.Float(10, 10, (float)(getWidth() - 20), (float)(getHeight() - 20));
                Shape inner = new RoundRectangle2D.Float(11, 11, (float)(getWidth() - 22), (float)(getHeight() - 22), 25, 25);
                Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
                path.append(outer, false);
                path.append(inner, false);
                g2d.fill(path);
                g2d.setColor(Color.black);
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(new RoundRectangle2D.Float(11, 11, (float)(getWidth() - 22), (float)(getHeight() - 22), 25, 25));
            }
        }
    }
}
