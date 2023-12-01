package specman.undo;

import specman.EditException;
import specman.Specman;
import specman.view.CatchBereich;
import specman.view.CatchSchrittSequenzView;

import javax.swing.undo.UndoableEdit;

public class UndoableCatchEntfernt extends AbstractUndoableInteraction {
  public UndoableCatchEntfernt(CatchSchrittSequenzView catchSequence, CatchBereich catchBereich, int catchIndex) {
  }

  @Override
  protected void undoEdit() throws EditException {

  }

  @Override
  protected void redoEdit() throws EditException {

  }
}
