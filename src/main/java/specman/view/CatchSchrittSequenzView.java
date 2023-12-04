package specman.view;

import specman.ColumnSpecByPercent;
import specman.EditException;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.CatchSchrittSequenzModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.ZweigSchrittSequenzModel_V001;
import specman.undo.UndoableCatchSequenceRemoved;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

import static specman.Aenderungsart.Geloescht;
import static specman.Aenderungsart.Hinzugefuegt;
import static specman.ColumnSpecByPercent.copyOf;

public class CatchSchrittSequenzView extends ZweigSchrittSequenzView implements FocusListener {
  BreakSchrittView linkedBreakStep;
  CatchUeberschrift catchUeberschrift;

  public CatchSchrittSequenzView(CatchBereich catchBereich, BreakSchrittView linkedBreakStep) {
    super(Specman.instance(), catchBereich, linkedBreakStep.id.naechsteEbene(), linkedBreakStep.getEditorContent(true));
    einfachenSchrittAnhaengen(Specman.instance());
    init(linkedBreakStep);
  }

  public CatchSchrittSequenzView(EditorI editor, AbstractSchrittView parent, ZweigSchrittSequenzModel_V001 model, BreakSchrittView linkedBreakStep) {
    super(editor, parent, model);
    init(linkedBreakStep);
  }

  private void init(BreakSchrittView linkedBreakStep) {
    this.linkedBreakStep = linkedBreakStep;
    ueberschrift.setId(linkedBreakStep.id.toString());
    linkedBreakStep.catchAnkoppeln(this);
    catchUeberschrift = new CatchUeberschrift(ueberschrift);
    ueberschrift.addEditAreasFocusListener(this);
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
    List<Integer> backupSequencesWidthPercent = copyOf(catchBereich.sequencesWidthPercent);
    int catchIndex = catchBereich.catchEntfernen(this);
    linkedBreakStep.catchAnkoppeln(null);
    Specman.instance().addEdit(new UndoableCatchSequenceRemoved(this, catchIndex, backupSequencesWidthPercent));
  }

  @Override
  protected void ueberschriftAlsGeloeschtMarkierenUDBL() {
    catchUeberschrift.alsGeloeschtMarkierenUDBL(linkedBreakStep.id);
  }

  @Override public void focusGained(FocusEvent e) {}

  @Override public void focusLost(FocusEvent e) {
    linkedBreakStep.updateContent(ueberschrift.editorContent2Model(true));
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

  public CatchSchrittSequenzModel_V001 generiereModel(boolean formatierterText) {
    CatchSchrittSequenzModel_V001 model = new CatchSchrittSequenzModel_V001(
      linkedBreakStep.id, aenderungsart, ueberschrift.editorContent2Model(formatierterText));
    populateModel(model, formatierterText);
    return model;
  }
}
