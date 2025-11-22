package specman;

import specman.undo.manager.UndoRecording;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

abstract public class ActionUDBLListener implements ActionListener {
  @Override
  public void actionPerformed(ActionEvent e) {
    try(UndoRecording ur = Specman.instance().composeUndo()) {
      performActionUDBL(e);
    }
  }

  abstract public void performActionUDBL(ActionEvent e);
}
