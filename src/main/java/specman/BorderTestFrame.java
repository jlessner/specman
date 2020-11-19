package specman;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class BorderTestFrame extends JFrame {

    BorderTestFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(200, 200);

        setLayout(new BorderLayout());

        //JPanel p = new JPanel();
        //JTextField p = new JTextField("Text");
        JEditorPane p = new JEditorPane();
        System.out.println(p.getInsets());
        System.out.println(p.getBorder());
        p.setBackground(Color.lightGray);
        p.setBorder(null);

        JPanel borderPanel = new JPanel();
        borderPanel.setBackground(Color.cyan);
        borderPanel.setLayout(new FormLayout("10px,fill:pref:grow,10px", "fill:pref:grow"));
        borderPanel.add(p, CC.xy(2, 1));

        JPanel panel = new FramePanel(borderPanel);
        getContentPane().add(panel, BorderLayout.CENTER);

        setVisible(true);
    }

    public static void main(String[] args) {
        new BorderTestFrame();
    }

    private class FramePanel extends JPanel {
        FramePanel(JComponent picture) {
            setBackground(Color.green.darker());
            //setBorder(new LineBorder(Color.gray, 10));
            //setBorder(new ExperimentalDotBorder());
            setLayout(new BorderLayout());
            add(picture, BorderLayout.CENTER);
            setBorder(new EmptyBorder(12, 12, 12, 12));
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
                g2d.setColor(Color.red);
                Shape outer = new java.awt.geom.Rectangle2D.Float((float)0, (float)0, (float)getWidth(), (float)getHeight());
                //Shape inner = new java.awt.geom.Rectangle2D.Float(10, 10, (float)(getWidth() - 20), (float)(getHeight() - 20));
                Shape inner = new RoundRectangle2D.Float(10, 10, (float)(getWidth() - 20), (float)(getHeight() - 20), 30, 30);
                Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
                path.append(outer, false);
                path.append(inner, false);
                g2d.fill(path);
                g2d.setColor(Color.black);
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(new RoundRectangle2D.Float(10, 10, (float)(getWidth() - 20), (float)(getHeight() - 20), 30, 30));
            }
        }
    }
}
