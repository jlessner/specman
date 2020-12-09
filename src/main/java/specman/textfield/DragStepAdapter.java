package specman.textfield;

import specman.EditorI;
import specman.Specman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author MaxKnauer
 */
public class DragStepAdapter extends MouseAdapter {
    Specman spec;
    JTextField dummy;


    public DragStepAdapter(EditorI edit) {
        this.spec = (Specman) edit;
    }

    public void mousePressed(MouseEvent e) {
        dummy = new JTextField("Neuer Schritt");
        dummy.setBounds(new Rectangle(100, 15));
    }

    public void mouseDragged(MouseEvent e) {
        //-2 da performanter
        Point pt = e.getPoint();
        JComponent parent = (JComponent) e.getComponent();
        Point dp = pt;
        Point dragOffset = new Point(pt.x - dp.x, pt.y - dp.y);
        Point ptCon = SwingUtilities.convertPoint((Component)e.getSource(),(int) e.getPoint().getX(),(int)e.getPoint().getY()-2, spec);
        spec.dragGlassPanePos(ptCon, spec.hauptSequenz.schritte,false,e);

        spec.window.add( dummy);
        spec.window.pack();
        updateWindowLocation(pt,dragOffset, parent);
        spec.window.setVisible(true);
    }

    private void updateWindowLocation(Point pt,Point dragOffset, JComponent parent){
        Point p = new Point(pt.x - dragOffset.x +3, pt.y - dragOffset.y +3);
        SwingUtilities.convertPointToScreen(p,parent);
        spec.window.setLocation(p);
    }

    public void mouseReleased(MouseEvent e) {
        spec.window.setVisible(false);
        Point ptCon = SwingUtilities.convertPoint((Component)e.getSource(),(int) e.getPoint().getX(),(int)e.getPoint().getY()-2, spec);
        spec.dragGlassPanePos(ptCon, spec.hauptSequenz.schritte,true,e);

        spec.getGlassPane().setVisible(false);
    }
}
