package specman.undo.props;

import specman.Aenderungsart;
import specman.Specman;
import specman.editarea.EditArea;
import specman.editarea.TextEditArea;
import specman.undo.AbstractUndoableInteraction;
import specman.view.AbstractSchrittView;
import specman.view.QuellSchrittView;
import specman.view.SchrittSequenzView;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
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
    Aenderungsart undoAenderungsart = abstractSchrittView.getAenderungsart();
    abstractSchrittView.setAenderungsart(aenderungsart);
    addEdit(new UndoableSetAenderungsart(abstractSchrittView, undoAenderungsart));
  }

  public static void setAenderungsart(SchrittSequenzView schrittSequenzView, Aenderungsart aenderungsart) {
    Aenderungsart undoAenderungsart = schrittSequenzView.getAenderungsart();
    schrittSequenzView.setAenderungsart(aenderungsart);
    addEdit(new UndoableSetAenderungsartSchrittSequenzView(schrittSequenzView, undoAenderungsart));
  }

  public static void setAenderungsart(EditArea editArea, Aenderungsart aenderungsart) {
    Aenderungsart undoAenderungsart = editArea.getAenderungsart();
    editArea.setAenderungsart(aenderungsart);
    addEdit(new UndoableSetAenderungsartEditArea(editArea, undoAenderungsart));
  }

  public static void setEditable(JTextComponent component, boolean editable) {
    boolean undoEditable = component.isEditable();
    component.setEditable(editable);
    addEdit(new UndoableSetEditable(component, undoEditable));
  }

  public static void setQuellschrittUDBL(AbstractSchrittView abstractSchrittView, QuellSchrittView quellschritt) {
    QuellSchrittView undoQuellschritt = abstractSchrittView.getQuellschritt();
    abstractSchrittView.setQuellschritt(quellschritt);
    addEdit((new UndoableSetQuellschritt(abstractSchrittView, undoQuellschritt)));
  }

  public static void setZielschrittUDBL(QuellSchrittView quellSchrittView, AbstractSchrittView zielschritt) {
    AbstractSchrittView undoZielschritt = quellSchrittView.getZielschritt();
    quellSchrittView.setZielschritt(zielschritt);
    addEdit((new UndoableSetZielschritt(quellSchrittView, undoZielschritt)));
  }

  public static void repaint(Component component) {
    component.repaint();
    addEdit(new UndoableRepaint(component));
  }

}
