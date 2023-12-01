package specman.view;

import specman.Aenderungsart;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.ZweigSchrittSequenzModel_V001;
import specman.undo.UndoableCatchEntfernt;

import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class CatchSchrittSequenzView extends ZweigSchrittSequenzView implements FocusListener {
  BreakSchrittView linkedBreakStep;
  CatchUeberschrift catchUeberschrift;

  public CatchSchrittSequenzView(CatchBereich catchBereich, BreakSchrittView linkedBreakStep, Aenderungsart aenderungsart) {
    super(Specman.instance(), catchBereich, linkedBreakStep.id.naechsteEbene(), aenderungsart, linkedBreakStep.getEditorContent(true));
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

  public CatchSchrittSequenzView(EditorI editor, AbstractSchrittView parent, SchrittID sequenzBasisId, Aenderungsart aenderungsart, EditorContentModel_V001 initialerText) {
    super(editor, parent, sequenzBasisId.naechsteEbene(), aenderungsart, initialerText);
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

  public void entfernen() {
    CatchBereich catchBereich = getParent();
    int catchIndex = catchBereich.catchEntfernen(this);
    linkedBreakStep.catchAnkoppeln(null);
    Specman.instance().addEdit(new UndoableCatchEntfernt(this, catchBereich, catchIndex));
  }

  @Override public void focusGained(FocusEvent e) {}

  @Override public void focusLost(FocusEvent e) {
    linkedBreakStep.updateContent(ueberschrift.editorContent2Model(true));
  }
}
