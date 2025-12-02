package specman.editarea.stepnumberlabel;

import specman.Aenderungsart;
import specman.Specman;
import specman.view.AbstractSchrittView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class StepnumberContextMenu implements MouseListener {
  private final JPopupMenu popup;
  private final JMenuItem delete;
  private final JCheckBoxMenuItem toggleSubNumbering;
  private AbstractSchrittView currentStep;
  private StepnumberLabel initiatingLabel;

  static StepnumberContextMenu instance = new StepnumberContextMenu();

  private StepnumberContextMenu() {
    popup = new JPopupMenu();
    delete = createDeleteItem();
    popup.add(delete);
    toggleSubNumbering = new JCheckBoxMenuItem("Unternummerierung");
    toggleSubNumbering.setState(true);
    popup.add(toggleSubNumbering);
  }

  private JMenuItem createDeleteItem() {
    JMenuItem item = new JMenuItem("Löschen");
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Specman.instance().deleteStep(currentStep, initiatingLabel);
      }
    });
    return item;
  }

  private StepnumberLabel label(MouseEvent e) {
    return (StepnumberLabel) e.getComponent();
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      AbstractSchrittView step = Specman.instance().findStep(label(e));
      initMenuForStep(step, label(e));
      popup.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  private void initMenuForStep(AbstractSchrittView currentStep, StepnumberLabel initiatingLabel) {
    this.currentStep = currentStep;
    this.initiatingLabel = initiatingLabel;
    this.delete.setEnabled(currentStep.getParent().allowsStepDeletion() && currentStep.getAenderungsart() != Aenderungsart.Geloescht);
    this.toggleSubNumbering.setVisible(currentStep.isStrukturiert());
  }

  @Override public void mousePressed(MouseEvent e) {}
  @Override public void mouseReleased(MouseEvent e) {}
  @Override public void mouseEntered(MouseEvent e) {}
  @Override public void mouseExited(MouseEvent e) {}
}
