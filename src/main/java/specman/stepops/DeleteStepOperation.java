package specman.stepops;

import specman.Aenderungsart;
import specman.EditException;
import specman.EditorI;
import specman.Specman;
import specman.editarea.InteractiveStepFragment;
import specman.editarea.TextEditArea;
import specman.undo.UndoableSchrittEntfernt;
import specman.undo.UndoableZweigEntfernt;
import specman.view.*;

import javax.swing.*;

import static specman.Aenderungsart.Hinzugefuegt;
import static specman.view.StepRemovalPurpose.Discard;

public class DeleteStepOperation {
  private final AbstractSchrittView step;
  private final InteractiveStepFragment initiatingFragment;
  private final EditorI editor;

  private static AbstractSchrittView findStep(TextEditArea initiatingTextArea) throws EditException{
    AbstractSchrittView schritt = Specman.instance().findeSchritt(initiatingTextArea);
    if (schritt == null) {
      throw new EditException("Ups - niemandem scheint das Feld zu gehören, in dem steht: " + initiatingTextArea.getText());
    }
    return schritt;
  }

  public DeleteStepOperation(TextEditArea initiatingTextArea) throws EditException {
    this(findStep(initiatingTextArea), initiatingTextArea);
  }

  public DeleteStepOperation(AbstractSchrittView step, InteractiveStepFragment initiatingFragment) throws EditException {
    this.step = step;
    this.initiatingFragment = initiatingFragment;
    this.editor = Specman.instance();
  }

  public void execute() throws EditException{
    //Der Teil wird nur durchlaufen, wenn die Aenderungsverfolgung aktiviert ist
    if (editor != null
      && editor.aenderungenVerfolgen()
      && step.getAenderungsart() != Hinzugefuegt
      && !(step instanceof CatchBereich)) {
      //Muss hinzugefügt werden um zu gucken ob die Markierung schon gesetzt wurde
      if (step.getAenderungsart() != Aenderungsart.Geloescht) {
        schrittAlsGeloeschtMarkierenUDBL(step);
        editor.resyncStepnumberStyleUDBL();
      }
    }
    else {
      //Hier erfolgt das richtige Löschen, Aenderungsverfolgung nicht aktiviert
      if (step instanceof CaseSchrittView) {
        CaseSchrittView caseSchritt = (CaseSchrittView) step;
        ZweigSchrittSequenzView zweig = caseSchritt.headingToBranch(initiatingFragment);
        if (zweig != null) {
          int zweigIndex = caseSchritt.zweigEntfernen(editor, zweig);
          editor.addEdit(new UndoableZweigEntfernt(editor, zweig, caseSchritt, zweigIndex));
          return;
        }
      }
      else if (step instanceof CatchBereich) {
        CatchBereich catchBereich = (CatchBereich) step;
        CatchSchrittSequenzView catchSequence = catchBereich.headingToBranch(initiatingFragment);
        if (catchSequence != null) {
          catchSequence.removeOrMarkAsDeletedUDBL();
          // No undo action required here. The undo composition of low-level changes covers everything
        }
        return;
      }
      if (isStepDeletionAllowed(step)) {
        step.markStepnumberLinksAsDefect();
        SchrittSequenzView sequenz = step.getParent();
        int schrittIndex = sequenz.schrittEntfernen(step, Discard);
        editor.addEdit(new UndoableSchrittEntfernt(step, sequenz, schrittIndex));
        editor.resyncStepnumberStyleUDBL();
      }
    }
  }

  public boolean isStepDeletionAllowed(AbstractSchrittView step) throws EditException {
    if (!step.getParent().allowsStepDeletion()) {
      throw new EditException("Der letzte Schritt kann nicht entfernt werden.");
    }
    if (step.hasStepnumberLinks()) {
      int dialogResult = editor.showConfirmDialog(
        "Der zu löschende Schritt wird referenziert. Möchten Sie den Schritt " +
          "wirklich löschen? Die Referenzen werden dann als 'Defekt' markiert.",
        "Verknüpfte Schrittreferenzen", JOptionPane.OK_CANCEL_OPTION);
      return dialogResult == JOptionPane.OK_OPTION;
    }
    return true;
  }

  private void schrittAlsGeloeschtMarkierenUDBL(AbstractSchrittView schritt) throws EditException {
    //Es wird geschaut, ob der Schritt nur noch alleine ist und überhaupt gelöscht werden darf
    if (isStepDeletionAllowed(schritt)) {
      schritt.alsGeloeschtMarkierenUDBL(editor);
    }
  }

}
