package specman.undo.props;

import specman.Aenderungsart;
import specman.Specman;
import specman.undo.AbstractUndoableInteraction;
import specman.view.AbstractSchrittView;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class UDBL {
  private static void addEdit(AbstractUndoableInteraction action) {
    Specman.instance().addEdit(action);
  }

  public static void bla() {

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
