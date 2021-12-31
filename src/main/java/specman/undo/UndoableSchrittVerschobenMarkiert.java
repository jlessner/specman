package specman.undo;

import specman.Aenderungsart;
import specman.EditorI;
import specman.view.AbstractSchrittView;
import specman.view.QuellSchrittView;
import specman.view.SchrittSequenzView;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import static specman.view.RelativeStepPosition.Before;

public class UndoableSchrittVerschobenMarkiert extends UndoableSchrittVerschoben {
  private final EditorI editor;
  private QuellSchrittView quellschritt;
  boolean quellschrittIstNeu;

  public UndoableSchrittVerschobenMarkiert(AbstractSchrittView step, SchrittSequenzView originalParent, int originalIndex, QuellSchrittView quellschritt, EditorI editor) {
    super(step, originalParent, originalIndex);
    this.originalParent = originalParent;
    this.originalIndex = originalIndex;
    this.editor = editor;
    this.quellschritt = quellschritt;
    this.quellschrittIstNeu = step.getQuellschritt() == null;
  }

  @Override public void undo() throws CannotUndoException {
    togglePosition();
    if (quellschrittIstNeu) {
      quellschritt.getParent().schrittEntfernen(quellschritt);
      step.setQuellschritt(null);
      step.setStandardStil();
      step.setAenderungsart(null);
    }
    else {
      // TODO JL: Unschön, dass das hier notwendig ist. Der Stil sollte gar nicht
      // kaputt gehen, wenn der Schritt (im Rahmen von togglePosition) seine ID
      // neu gesetzt bekommt.
      step.resyncSchrittnummerStil();
    }
  }

  @Override public void redo() throws CannotRedoException {
    if (quellschrittIstNeu) {
      quellschritt = new QuellSchrittView(editor, originalParent, step.getId());
      originalParent.schrittZwischenschieben(quellschritt, Before, step, editor);
      step.setQuellschritt(quellschritt);
      step.setZielschrittStil();
      step.setAenderungsart(Aenderungsart.Zielschritt);
    }
    togglePosition();
    if (!quellschrittIstNeu) {
      // TODO JL: Unschön, dass das hier notwendig ist. Der Stil sollte gar nicht
      // kaputt gehen, wenn der Schritt (im Rahmen von togglePosition) seine ID
      // neu gesetzt bekommt.
      step.resyncSchrittnummerStil();
    }
  }
}
