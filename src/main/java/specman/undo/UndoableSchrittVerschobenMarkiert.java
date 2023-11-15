package specman.undo;

import specman.Aenderungsart;
import specman.EditException;
import specman.EditorI;
import specman.Specman;
import specman.undo.manager.UndoRecording;
import specman.view.AbstractSchrittView;
import specman.view.QuellSchrittView;
import specman.view.SchrittSequenzView;

import static specman.Aenderungsart.Untracked;
import static specman.view.RelativeStepPosition.Before;

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
      quellschritt.getParent().schrittEntfernen(quellschritt);
      step.aenderungsmarkierungenEntfernen();
    }
    else {
      // TODO JL: Unschön, dass das hier notwendig ist. Der Stil sollte gar nicht
      // kaputt gehen, wenn der Schritt (im Rahmen von togglePosition) seine ID
      // neu gesetzt bekommt.
      step.resyncSchrittnummerStil();
    }
  }

  @Override public void redoEdit() throws EditException {
    if (quellschrittIstNeu) {
      quellschritt = new QuellSchrittView(Specman.instance(), originalParent, step.getId());
      originalParent.schrittZwischenschieben(quellschritt, Before, step);
    }
    togglePosition();
    // TODO JL: Unschön, dass das hier notwendig ist. Der Stil sollte gar nicht
    // kaputt gehen, wenn der Schritt (im Rahmen von togglePosition) seine ID
    // neu gesetzt bekommt.
    step.resyncSchrittnummerStil();
    quellschritt.resyncSchrittnummerStil();
  }
}
