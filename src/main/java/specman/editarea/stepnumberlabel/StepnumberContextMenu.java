package specman.editarea.stepnumberlabel;

import specman.Aenderungsart;
import specman.Specman;
import specman.undo.UndoableFlatNumberingToggled;
import specman.undo.manager.UndoRecording;
import specman.view.AbstractSchrittView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class StepnumberContextMenu implements MouseListener {
  private final JPopupMenu popup;
  private final JMenuItem delete;
  private final JCheckBoxMenuItem toggleFlatNumbering;
  private AbstractSchrittView currentStep;
  private StepnumberLabel initiatingLabel;

  static StepnumberContextMenu instance = new StepnumberContextMenu();

  private StepnumberContextMenu() {
    popup = new JPopupMenu();
    delete = createDeleteItem();
    popup.add(delete);
    toggleFlatNumbering = createSubnumberingItem();
    popup.add(toggleFlatNumbering);
  }

  private JCheckBoxMenuItem createSubnumberingItem() {
    JCheckBoxMenuItem item = new JCheckBoxMenuItem("Flache Nummerierung");
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try (UndoRecording ur = Specman.instance().composeUndo()) {
          currentStep.toggleFlatNumbering(toggleFlatNumbering.getState());
          Specman.instance().addEdit(new UndoableFlatNumberingToggled(currentStep, toggleFlatNumbering.getState()));
        }
      }
    });
    return item;
  }

  private JMenuItem createDeleteItem() {
    JMenuItem item = new JMenuItem("Löschen");
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Specman.instance().deleteStepUDBL(currentStep, initiatingLabel);
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
    this.toggleFlatNumbering.setVisible(currentStep.getFlatNumbering() != null);
    if (toggleFlatNumbering.isVisible()) {
      toggleFlatNumbering.setState(currentStep.getFlatNumbering());
    }
  }

  @Override public void mousePressed(MouseEvent e) {}
  @Override public void mouseReleased(MouseEvent e) {}
  @Override public void mouseEntered(MouseEvent e) {}
  @Override public void mouseExited(MouseEvent e) {}
}
