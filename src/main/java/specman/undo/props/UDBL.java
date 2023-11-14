package specman.undo.props;

import specman.Aenderungsart;
import specman.Specman;
import specman.undo.AbstractUndoableInteraction;
import specman.view.AbstractSchrittView;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/** Wrapper class to perform typical property changes in an undoable way
 * by just one line. The class name is very short to make these lines become
 * readable. Usually there are the same method names available within the
 * calling classes itself and in this case, the methods in here can't be
 * imported statically. So the class name must often be used as qualifier.
 */
public class UDBL {
  private static void addEdit(AbstractUndoableInteraction action) {
    Specman.instance().addEdit(action);
  }

  public static void setBackgroundUDBL(Component component, Color bg) {
    Color undoBackground = component.getBackground();
    component.setBackground(bg);
    addEdit(new UndoableSetBackground(component, undoBackground));
  }

  public static void setForegroundUDBL(Component component, Color bg) {
    Color undoForeground = component.getForeground();
    component.setForeground(bg);
    addEdit(new UndoableSetForeground(component, undoForeground));
  }

  public static void setBorderUDBL(JComponent component, Border border) {
    Border undoBorder = component.getBorder();
    component.setBorder(border);
    addEdit(new UndoableSetBorder(component, undoBorder));
  }

  public static void setTextUDBL(JLabel label, String text) {
    String undoText = label.getText();
    label.setText(text);
    addEdit(new UndoableSetText(label, undoText));
  }

  public static void setAenderungsart(AbstractSchrittView abstractSchrittView, Aenderungsart aenderungsart) {
    Aenderungsart undoAenderungsart = aenderungsart;
    abstractSchrittView.setAenderungsart(aenderungsart);
    addEdit(new UndoableSetAenderungsart(abstractSchrittView, undoAenderungsart));
  }

  public static void repaint(Component component) {
    component.repaint();
    addEdit(new UndoableRepaint(component));
  }
}
