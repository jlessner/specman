package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import specman.*;
import specman.editarea.EditContainer;
import specman.editarea.InteractiveStepFragment;
import specman.editarea.TextEditArea;
import specman.editarea.TextStyles;
import specman.editarea.stepnumberlabel.StepnumberLabel;
import specman.model.v001.CatchSchrittSequenzModel_V001;
import specman.model.v001.CoCatchModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.pdf.Shape;
import specman.undo.UndoableCatchSequenceRemoved;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import static specman.Aenderungsart.Geloescht;
import static specman.Aenderungsart.Hinzugefuegt;
import static specman.ColumnSpecByPercent.copyOf;
import static specman.view.AbstractSchrittView.FORMLAYOUT_GAP;
import static specman.view.AbstractSchrittView.ZEILENLAYOUT_INHALT_SICHTBAR;

public class CatchSchrittSequenzView extends ZweigSchrittSequenzView implements FocusListener {
  JPanel headingPanel;
  FormLayout headingPanelLayout;
  CatchUeberschrift primaryCatchHeading;

  /** The co-catches are additional linked break steps which share the same catch sequence
   * with the primary linked break step. This is the correspondence to something like
   * <pre>
   *   catch (ExceptionType1 | ExceptionType2... e) {
   * </pre>
   * where everything following the very first exception type is a co-catch. It contributes
   * only an additional heading to this {@link CatchSchrittSequenzView}, being placed between
   * the primary heading and the handling sequence. */
  List<CatchUeberschrift> coCatchHeadings = new ArrayList<>();

  public CatchSchrittSequenzView(CatchBereich catchBereich, BreakSchrittView linkedBreakStep) {
    super(Specman.instance(), catchBereich, linkedBreakStep.id.naechsteEbene(), linkedBreakStep.getEditorContent(true));
    einfachenSchrittAnhaengen(Specman.instance());
    init(linkedBreakStep);
    initHeadingsLayout();
  }

  public CatchSchrittSequenzView(AbstractSchrittView parent, CatchSchrittSequenzModel_V001 model) {
    super(Specman.instance(), parent, model);
    BreakSchrittView linkedBreakSchritt = (BreakSchrittView) parent.getParent().findStepByStepID(model.id.toString());
    init(linkedBreakSchritt);
    initCoCatches(model.coCatches);
    initHeadingsLayout();
  }

  private void initHeadingsLayout() {
    createHeadingsLayout();
    reassignHeadingsToLayout();
  }

  private void createHeadingsLayout() {
    String rowSpecs = ZEILENLAYOUT_INHALT_SICHTBAR;
    for (int i = 0; i < coCatchHeadings.size(); i++) {
      rowSpecs += ", " + FORMLAYOUT_GAP + ", " + ZEILENLAYOUT_INHALT_SICHTBAR;
    }
    headingPanelLayout = new FormLayout("fill:pref:grow", rowSpecs);
    headingPanel.setLayout(headingPanelLayout);
  }

  private void reassignHeadingsToLayout() {
    headingPanel.removeAll();
    headingPanel.add(primaryCatchHeading, CC.xy(1, 1));
    for (int i = 0; i < coCatchHeadings.size(); i++) {
      headingPanel.add(coCatchHeadings.get(i), CC.xy(1, 3 + i * 2));
    }
  }

  private void init(BreakSchrittView linkedBreakStep) {
    headingPanel = new JPanel();
    headingPanel.setBackground(TextStyles.DIAGRAMM_LINE_COLOR);
    ueberschrift.setId(linkedBreakStep.id);
    primaryCatchHeading = new CatchUeberschrift(ueberschrift, linkedBreakStep, this);
    linkedBreakStep.catchAnkoppeln(primaryCatchHeading);
    ueberschrift.addEditAreasFocusListener(this);
  }

  private void initCoCatches(List<CoCatchModel_V001> coCatches) {
    if (coCatches != null) { // For compatibility with Specman versions < 1.0.2

    }
  }

  @Override
  protected void ueberschriftInitialisieren(EditorI editor, EditorContentModel_V001 initialerText, SchrittID initialeSchrittnummer) {
    // Dummy SchrittID causes the heading to be created with step number label which will be updated later
    super.ueberschriftInitialisieren(editor, initialerText, new SchrittID(0));
  }

  @Override
  public void skalieren(int prozentNeu, int prozentAktuell) {
    super.skalieren(prozentNeu, prozentAktuell);
    primaryCatchHeading.skalieren(prozentNeu, prozentAktuell);
  }

  protected void catchBereichInitialisieren() {
    // There is no catch area in a catch sequence ;-)
  }

  protected void catchBereichSkalieren(int prozentNeu, int prozentAktuell) {}

  public Component getHeadingPanel() { return headingPanel; }

  @Override
  public CatchBereich getParent() { return (CatchBereich) super.getParent(); }

  public void setId(SchrittID id) {
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

  public void remove(CatchUeberschrift catchHeading) {
    if (catchHeading == primaryCatchHeading) {
      remove();
    }
    else {
      coCatchHeadings.remove(catchHeading);
      initHeadingsLayout();
    }
  }

  public void remove() {
    CatchBereich catchBereich = getParent();
    List<Integer> backupSequencesWidthPercent = copyOf(catchBereich.sequencesWidthPercent);
    int catchIndex = catchBereich.catchEntfernen(this);
    primaryCatchHeading.disconnectLinkedBreakStep();
    coCatchHeadings.stream().forEach(coCatchHeading -> coCatchHeading.disconnectLinkedBreakStep());
    Specman.instance().addEdit(new UndoableCatchSequenceRemoved(this, catchIndex, backupSequencesWidthPercent));
  }

  @Override
  protected void ueberschriftAlsGeloeschtMarkierenUDBL() {
    primaryCatchHeading.alsGeloeschtMarkierenUDBL();
  }

  @Override public void focusGained(FocusEvent e) {}

  @Override public void focusLost(FocusEvent e) {
    if (aenderungsart != Geloescht) {
      TextEditArea editArea = (TextEditArea) e.getSource();
      CatchUeberschrift catchHeading = editArea.containingCatchHeading();
      catchHeading.updateLinkedBreakStepContent();
    }
  }

  @Override
  public int aenderungenUebernehmen(EditorI editor) throws EditException {
    if (aenderungsart == Geloescht) {
      remove();
      return 1;
    }
    else {
      return super.aenderungenUebernehmen(editor) + primaryCatchHeading.aenderungenUebernehmen();
    }
  }

  @Override
  public int aenderungenVerwerfen(EditorI editor) throws EditException {
    Aenderungsart lastChangetype = aenderungsart;
    int changesReverted = super.aenderungenVerwerfen(editor) + primaryCatchHeading.aenderungenVerwerfen();
    if (lastChangetype == Geloescht) {
      // While the catch sequences was marked as deleted, its heading was not synchronized
      // with the linked break step's content. So when we have rolled back a deletion, we
      // might have to resynchronize
      updateHeadings();
    }
    return changesReverted;
  }

  private void updateHeadings() {
    primaryCatchHeading.updateLinkedBreakStepContent();
    coCatchHeadings.stream().forEach(coCatchHeading -> coCatchHeading.updateLinkedBreakStepContent());
  }

  @Override
  public void aenderungsmarkierungenEntfernen() {
    primaryCatchHeading.aenderungsmarkierungenEntfernen();
  }

  public CatchSchrittSequenzModel_V001 generiereModel(boolean formatierterText) {
    CatchSchrittSequenzModel_V001 model = new CatchSchrittSequenzModel_V001(
      primaryCatchHeading.linkedBreakStepId(), aenderungsart, ueberschrift.editorContent2Model(formatierterText));
    populateModel(model, formatierterText);
    return model;
  }

  @Override
  public Shape getShapeSequence() {
    return super.getShapeSequence();
  }

  /** Reconnecting is required for undo / redo operations. As a catch sequence may be removed
   * either <i>separately</i> or <i>combined with the linked breakstep</i>. Therefore the
   * catch sequence gets de-connected from its break step on removal. Otherwise the break step
   * could not be connected with a new sequence. If this sequence here is restored by undo / redo,
   * the cut connection must be re-established. */
  public void reconnectToBreakstep() {
    primaryCatchHeading.connectLinkedBreakStep();
    coCatchHeadings.stream().forEach(coCatchHeading -> coCatchHeading.connectLinkedBreakStep());
  }

  public boolean contains(CatchUeberschrift catchHeading) {
    return primaryCatchHeading == catchHeading || coCatchHeadings.contains(catchHeading);
  }

  public void addCoCatch(CatchUeberschrift referenceCatchHeading, BreakSchrittView breakStepToLink) {
    int insertionIndex = coCatchHeadings.indexOf(referenceCatchHeading) + 1;
    EditorContentModel_V001 breakStepContent = breakStepToLink.getEditorContent(true);
    EditContainer coCatchHeadingContent = new EditContainer(Specman.instance(), breakStepContent, breakStepToLink.id);
    coCatchHeadingContent.addEditAreasFocusListener(this);
    CatchUeberschrift coCatchHeading = new CatchUeberschrift(coCatchHeadingContent, breakStepToLink, this);
    coCatchHeadings.add(insertionIndex, coCatchHeading);
    breakStepToLink.catchAnkoppeln(coCatchHeading);
    initHeadingsLayout();
  }

  public boolean isDeleted() {
    return aenderungsart == Geloescht;
  }

  public boolean isPrimaryHeading(CatchUeberschrift catchUeberschrift) {
    return catchUeberschrift == primaryCatchHeading;
  }

  public boolean enthaelt(InteractiveStepFragment fragment) {
    return headingFromFragment(fragment) != null;
  }

  CatchUeberschrift headingFromFragment(InteractiveStepFragment fragment) {
    if (primaryCatchHeading.ueberschrift.enthaelt(fragment)) {
      return primaryCatchHeading;
    }
    return coCatchHeadings
      .stream()
      .filter(cch -> cch.ueberschrift.enthaelt(fragment))
      .findFirst()
      .orElse(null);
  }
}
