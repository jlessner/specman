package specman.undo;

import specman.EditException;
import specman.Specman;
import specman.view.AbstractSchrittView;
import specman.view.QuellSchrittView;
import specman.view.SchrittSequenzView;

import static specman.view.RelativeStepPosition.Before;
import static specman.view.StepRemovalPurpose.Discard;

public class UndoableSchrittVerschobenMarkiert extends UndoableSchrittVerschoben {
  private QuellSchrittView quellschritt;
  boolean quellschrittIstNeu;

  public UndoableSchrittVerschobenMarkiert(AbstractSchrittView step, SchrittSequenzView originalParent, int originalIndex, QuellSchrittView quellschritt) {
    super(step, originalParent, originalIndex);
    this.originalParent = originalParent;
    this.originalIndex = originalIndex;
    this.quellschritt = quellschritt;
    this.quellschrittIstNeu = step.getQuellschritt() == null;
  }

  @Override public void undoEdit() throws EditException {
    togglePosition();
    if (quellschrittIstNeu) {
      quellschritt.getParent().schrittEntfernen(quellschritt, Discard);
      step.aenderungsmarkierungenEntfernen();
    }
  }

  @Override public void redoEdit() throws EditException {
    if (quellschrittIstNeu) {
      originalParent.schrittZwischenschieben(quellschritt, Before, step);
    }
    togglePosition();
  }
}
