package specman;

import specman.undo.manager.UndoRecording;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

abstract public class AtomicActionListener implements ActionListener {
  @Override
  public void actionPerformed(ActionEvent e) {
    try(UndoRecording ur = Specman.instance().composeUndo()) {
      performAtomicAction(e);
    }
  }

  abstract public void performAtomicAction(ActionEvent e);
}
