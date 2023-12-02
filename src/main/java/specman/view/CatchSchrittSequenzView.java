package specman.view;

import specman.EditException;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.ZweigSchrittSequenzModel_V001;
import specman.undo.UndoableCatchSequenceRemoved;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import static specman.Aenderungsart.Geloescht;
import static specman.Aenderungsart.Hinzugefuegt;

public class CatchSchrittSequenzView extends ZweigSchrittSequenzView implements FocusListener {
  BreakSchrittView linkedBreakStep;
  CatchUeberschrift catchUeberschrift;

  public CatchSchrittSequenzView(CatchBereich catchBereich, BreakSchrittView linkedBreakStep) {
    super(Specman.instance(), catchBereich, linkedBreakStep.id.naechsteEbene(), linkedBreakStep.getEditorContent(true));
    this.linkedBreakStep = linkedBreakStep;
    einfachenSchrittAnhaengen(Specman.instance());
    ueberschrift.setId(linkedBreakStep.id.toString());
    catchUeberschrift = new CatchUeberschrift(ueberschrift);
    linkedBreakStep.catchAnkoppeln(this);
    ueberschrift.addEditAreasFocusListener(this);
  }

  public CatchSchrittSequenzView(EditorI editor, AbstractSchrittView parent, ZweigSchrittSequenzModel_V001 model) {
    super(editor, parent, model);
  }

  public CatchSchrittSequenzView(EditorI editor, AbstractSchrittView parent, SchrittID sequenzBasisId, EditorContentModel_V001 initialerText) {
    super(editor, parent, sequenzBasisId.naechsteEbene(), initialerText);
    ueberschrift.setId(sequenzBasisId.toString());
  }

  @Override
  protected void ueberschriftInitialisieren(EditorI editor, EditorContentModel_V001 initialerText, String initialeSchrittnummer) {
    super.ueberschriftInitialisieren(editor, initialerText, "");
  }

  @Override
  public void skalieren(int prozentNeu, int prozentAktuell) {
    super.skalieren(prozentNeu, prozentAktuell);
    catchUeberschrift.skalieren(prozentNeu, prozentAktuell);
  }

  protected void catchBereichInitialisieren() {
    // There is no catch area in a catch sequence ;-)
  }

  protected void catchBereichSkalieren(int prozentNeu, int prozentAktuell) {}

  public CatchUeberschrift getCatchUeberschrift() { return catchUeberschrift; }

  public void updateHeading(EditorContentModel_V001 breakStepContent) {
    ueberschrift.setEditorContent(breakStepContent);
  }

  @Override
  public CatchBereich getParent() { return (CatchBereich) super.getParent(); }

  public void setId(SchrittID id) {
    ueberschrift.setId(id.toString());
    sequenzBasisId = id.naechsteEbene();
    renummerieren();
  }

  public void removeOrMarkAsDeletedUDBL() {
    EditorI editor = Specman.instance();
    if (aenderungsart == Hinzugefuegt || !editor.aenderungenVerfolgen()) {
      remove();
    }
    else {
      alsGeloeschtMarkierenUDBL(editor);
    }
  }

  public void remove() {
    CatchBereich catchBereich = getParent();
    int catchIndex = catchBereich.catchEntfernen(this);
    linkedBreakStep.catchAnkoppeln(null);
    Specman.instance().addEdit(new UndoableCatchSequenceRemoved(this, catchIndex));
  }

  @Override
  protected void ueberschriftAlsGeloeschtMarkierenUDBL() {
    catchUeberschrift.alsGeloeschtMarkierenUDBL(linkedBreakStep.id);
  }

  @Override public void focusGained(FocusEvent e) {}

  @Override public void focusLost(FocusEvent e) {
    //linkedBreakStep.updateContent(ueberschrift.editorContent2Model(true));
  }

  @Override
  public int aenderungenUebernehmen(EditorI editor) throws EditException {
    if (aenderungsart == Geloescht) {
      remove();
      return 1;
    }
    else {
      return super.aenderungenUebernehmen(editor) + catchUeberschrift.aenderungenUebernehmen();
    }
  }

  @Override
  public int aenderungenVerwerfen(EditorI editor) throws EditException {
    return super.aenderungenVerwerfen(editor) + catchUeberschrift.aenderungenVerwerfen();
  }

  @Override
  public void aenderungsmarkierungenEntfernen() {
    catchUeberschrift.aenderungsmarkierungenEntfernen(linkedBreakStep.id);
  }

  public void updateHeadingBounds() { ueberschrift.updateBounds(); }
}
