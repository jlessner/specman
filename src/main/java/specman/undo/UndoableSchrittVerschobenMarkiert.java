package specman.undo;

import specman.Aenderungsart;
import specman.EditException;
import specman.EditorI;
import specman.view.AbstractSchrittView;
import specman.view.QuellSchrittView;
import specman.view.SchrittSequenzView;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

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
    try {
      togglePosition();
      if (quellschrittIstNeu) {
        editor.pauseUndoRecording();
        quellschritt.getParent().schrittEntfernen(quellschritt);
        step.setQuellschritt(null);
        step.setStandardStil();
        step.setAenderungsart(null);
        editor.resumeUndoRecording();
      }
      else {
        // TODO JL: Unschön, dass das hier notwendig ist. Der Stil sollte gar nicht
        // kaputt gehen, wenn der Schritt (im Rahmen von togglePosition) seine ID
        // neu gesetzt bekommt.
        step.resyncSchrittnummerStil();
      }
    }
    catch(EditException ex) {
      editor.showError(ex);
      throw new CannotUndoException();
    }
  }

  @Override public void redo() throws CannotRedoException {
    try {
      if (quellschrittIstNeu) {
        editor.pauseUndoRecording();
        quellschritt = new QuellSchrittView(editor, originalParent, step.getId());
        originalParent.schrittZwischenschieben(quellschritt, Before, step, editor);
        step.setQuellschritt(quellschritt);
        quellschritt.setZielschritt(step);
        step.setZielschrittStil();
        step.setAenderungsart(Aenderungsart.Zielschritt);
        editor.resumeUndoRecording();
      }
      togglePosition();
      // TODO JL: Unschön, dass das hier notwendig ist. Der Stil sollte gar nicht
      // kaputt gehen, wenn der Schritt (im Rahmen von togglePosition) seine ID
      // neu gesetzt bekommt.
      step.resyncSchrittnummerStil();
      quellschritt.resyncSchrittnummerStil();
    }
    catch(EditException ex) {
      editor.showError(ex);
      throw new CannotRedoException();
    }
  }
}
