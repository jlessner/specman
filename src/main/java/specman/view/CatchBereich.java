package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import specman.Aenderungsart;
import specman.ColumnSpecByPercent;
import specman.EditException;
import specman.EditorI;
import specman.SpaltenContainerI;
import specman.SpaltenResizer;
import specman.Specman;
import specman.editarea.InteractiveStepFragment;
import specman.editarea.TextStyles;
import specman.editarea.stepnumberlabel.StepnumberLabel;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.CatchBereichModel_V001;
import specman.model.v001.CatchSchrittSequenzModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.pdf.Shape;
import specman.undo.UndoableCatchSequenceAdded;

import javax.swing.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import static specman.ColumnSpecByPercent.allocPercent;
import static specman.ColumnSpecByPercent.copyOf;
import static specman.ColumnSpecByPercent.releasePercent;
import static specman.editarea.TextStyles.DIAGRAMM_LINE_COLOR;
import static specman.pdf.Shape.GAP_COLOR;

public class CatchBereich extends AbstractSchrittView implements KlappbarerBereichI, ComponentListener, SpaltenContainerI {
  private static final int BOTTOMBAR_LAYOUTROW = 6;
  private static final int TOPBAR_LAYOUTROW = 2;

  KlappButton klappen;
  JPanel bereichPanel = new JPanel();
  JPanel topBar = new JPanel();
  JPanel bottomBar = new JPanel();
  JPanel sequencesPanel = new JPanel();
  FormLayout bereichLayout;
  FormLayout catchSequencesLayout;
  List<CatchSchrittSequenzView> catchSequences = new ArrayList<>();
  String barRowSpec;
  List<Integer> sequencesWidthPercent;

  public CatchBereich(SchrittSequenzView parent) {
    super(Specman.instance(), parent, new EditorContentModel_V001(), null, Aenderungsart.Untracked);
    computeBarRowSpec(Specman.instance().getZoomFactor());
    bereichLayout = new FormLayout("10px:grow",
      FORMLAYOUT_GAP + "," + barRowSpec + "," + FORMLAYOUT_GAP + ",fill:pref," + FORMLAYOUT_GAP + "," + barRowSpec);
    bereichPanel.setLayout(bereichLayout);
    bereichPanel.add(topBar, CC.xy(1, TOPBAR_LAYOUTROW));
    bereichPanel.add(sequencesPanel, CC.xy(1, 4));
    bereichPanel.add(bottomBar, CC.xy(1, BOTTOMBAR_LAYOUTROW));

    bereichPanel.setBackground(DIAGRAMM_LINE_COLOR);
    sequencesPanel.setBackground(DIAGRAMM_LINE_COLOR);
    bottomBar.setBackground(TextStyles.Hintergrundfarbe_Deviderbar);
    topBar.setBackground(TextStyles.Hintergrundfarbe_Deviderbar);
    topBar.setLayout(null);

    klappen = new KlappButton(this, topBar, bereichLayout, 4, null);

    bereichPanel.addComponentListener(this);

    bereichPanel.setVisible(false);
  }

  private void computeBarRowSpec(int zoomfactor) {
    barRowSpec = "fill:" + KlappButton.MINIMUM_ICON_LENGTH * zoomfactor / 100 + "px";
  }

  @Override
  public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
    return null;
  }

  @Override
  public JComponent getPanel() {
    return bereichPanel;
  }

  @Override public void componentResized(ComponentEvent e) {
    super.componentResized(e);
    klappen.updateLocation(topBar.getWidth());
  }

  @Override public void componentMoved(ComponentEvent e) {}
  @Override public void componentShown(ComponentEvent e) {}
  @Override public void componentHidden(ComponentEvent e) {}

  @Override
  public void geklappt(boolean auf) {
    sequencesPanel.setVisible(auf);
    String bottomBarLayout = auf ? barRowSpec : ZEILENLAYOUT_INHALT_VERBORGEN;
    bereichLayout.setRowSpec(BOTTOMBAR_LAYOUTROW, RowSpec.decode(bottomBarLayout));
    // As the catch area is always placed at the bottom of a sequence, we also
    // hide the bottom gap line of the area to avoid *three* gaps to pile up
    String gapLayout = auf ? FORMLAYOUT_GAP : ZEILENLAYOUT_INHALT_VERBORGEN;
    bereichLayout.setRowSpec(BOTTOMBAR_LAYOUTROW -1, RowSpec.decode(gapLayout));
    bottomBar.setVisible(auf);
  }

  @Override
  public boolean enthaeltAenderungsmarkierungen() {
    for (CatchSchrittSequenzView seq: catchSequences) {
      if (seq.enthaeltAenderungsmarkierungen()) {
        return true;
      }
    }
    return false;
  }

  public int catchEntfernen(CatchSchrittSequenzView catchSequence) {
    int index = catchSequences.indexOf(catchSequence);
    catchSequences.remove(index);
    if (!catchSequences.isEmpty()) {
      sequencesWidthPercent = releasePercent(index, sequencesWidthPercent);
      recomputeLayout();
    }
    else {
      bereichPanel.setVisible(false);
    }
    return index;
  }

  public AbstractSchrittView findeSchritt(InteractiveStepFragment fragment) {
    for (CatchSchrittSequenzView seq: catchSequences) {
      if (seq.ueberschrift.enthaelt(fragment)) {
        return this;
      }
      AbstractSchrittView result = seq.findeSchritt(fragment);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public void entfernen(SchrittSequenzView schrittSequenzView, StepRemovalPurpose purpose) {
  }

  public void zusammenklappenFuerReview() {
    boolean anyChanges = catchSequences
      .stream()
      .anyMatch(sequence -> sequence.enthaeltAenderungsmarkierungen());
    if (!anyChanges) {
      klappen.init(true);
    }
  }

  public void skalieren(int prozentNeu, int prozentAktuell) {
    catchSequences.forEach(seq -> seq.skalieren(prozentNeu, prozentAktuell));
    computeBarRowSpec(prozentNeu);
    bereichLayout.setRowSpec(TOPBAR_LAYOUTROW, RowSpec.decode(barRowSpec));
    if (!klappen.isSelected()) {
      bereichLayout.setRowSpec(BOTTOMBAR_LAYOUTROW, RowSpec.decode(barRowSpec));
    }
    klappen.scale(prozentNeu, prozentAktuell);
  }

  public CatchSchrittSequenzView catchSequenzAnhaengen(BreakSchrittView breakStepToLink) {
    List<Integer> originalSequencesWidthPercents = copyOf(sequencesWidthPercent);
    CatchSchrittSequenzView catchSequence = new CatchSchrittSequenzView(this, breakStepToLink);
    addCatchSequence(catchSequence, null, null);
    Specman.instance().addEdit(new UndoableCatchSequenceAdded(catchSequence, originalSequencesWidthPercents));
    return catchSequence;
  }

  public void addCatchSequence(CatchSchrittSequenzView catchSequence, Integer catchIndex, List<Integer> backupSequencesWidthPercent) {
    if (catchIndex == null) {
      catchIndex = catchSequences.size();
    }
    catchSequences.add(catchIndex, catchSequence);
    bereichPanel.setVisible(true);
    if (backupSequencesWidthPercent != null) {
      sequencesWidthPercent = backupSequencesWidthPercent;
    }
    else {
      allocPercent(catchIndex, sequencesWidthPercent);
    }
    recomputeLayout();
  }

  private void recomputeLayout() {
    createSequencesPanelLayout();
    reassignSequencesAndResizers();
    sequencesPanel.revalidate();
  }

  private void reassignSequencesAndResizers() {
    sequencesPanel.removeAll();
    for (int c = 0; c < catchSequences.size(); c++) {
      CatchSchrittSequenzView catchSequence = catchSequences.get(c);
      sequencesPanel.add(catchSequence.getCatchUeberschrift(), CC.xy(c*2 + 1, 1));
      sequencesPanel.add(catchSequence.sequenzBereich, CC.xy(c*2 + 1, 3));
    }
    for (int c = 0; c < catchSequences.size()-1; c++) {
      sequencesPanel.add(new SpaltenResizer(this, c, Specman.instance()), CC.xywh(c*2 + 2, 1, 1, 3));
    }
  }

  private void createSequencesPanelLayout() {
    String columnSpecs = ColumnSpecByPercent.percents2specs(catchSequences.size(), sequencesWidthPercent);
    catchSequencesLayout = new FormLayout(columnSpecs, "fill:pref, 2px, fill:pref");
    sequencesPanel.setLayout(catchSequencesLayout);
  }

  @Override
  public int spaltenbreitenAnpassenNachMausDragging(int vergroesserung, int spalte) {
    Integer[] columnWidths = catchSequences
      .stream()
      .map(seq -> seq.catchUeberschrift.getWidth())
      .toArray(Integer[]::new);
    List<Integer> recomputed = ColumnSpecByPercent.recomputePercents(columnWidths, vergroesserung, spalte);
    if (recomputed != null) {
      sequencesWidthPercent = recomputed;
      recomputeLayout();
      return vergroesserung;
    }
    return 0;
  }

  public CatchSchrittSequenzView headingToBranch(InteractiveStepFragment fragment) {
    return catchSequences
      .stream()
      .filter(seq -> seq.hatUeberschrift(fragment))
      .findFirst()
      .orElse(null);
  }

  public int aenderungenUebernehmen(EditorI editor) throws EditException {
    int changesCommitted = 0;
    for (CatchSchrittSequenzView seq: modifyableCatchSequences()) {
      changesCommitted += seq.aenderungenUebernehmen(editor);
    }
    return changesCommitted;
  }

  @Override
  public int aenderungenVerwerfen(EditorI editor) throws EditException {
    int changesReverted = 0;
    for (CatchSchrittSequenzView seq: modifyableCatchSequences()) {
      changesReverted += seq.aenderungenVerwerfen(editor);
    }
    return changesReverted;
  }

  @Override
  public void aenderungsmarkierungenEntfernen() {
    super.aenderungsmarkierungenEntfernen();
    for (CatchSchrittSequenzView seq: catchSequences) {
      seq.aenderungsmarkierungenEntfernen();
    }
  }

  /** Required for iterations that may modify the list of edit areas.
   * Working directly on the list whould cause concurrent operation exceptions
   * in these cases. */
  private List<CatchSchrittSequenzView> modifyableCatchSequences() { return new ArrayList<>(catchSequences); }

  public CatchBereichModel_V001 generiereCatchBereichModel(boolean formatierterText) {
    CatchBereichModel_V001 model = new CatchBereichModel_V001(sequencesWidthPercent, klappen.isSelected());
    for (CatchSchrittSequenzView seq: catchSequences) {
      model.catchSequences.add(seq.generiereModel(formatierterText));
    }
    return model;
  }

  public void populate(CatchBereichModel_V001 model) {
    EditorI editor = Specman.instance();
    for (CatchSchrittSequenzModel_V001 seqModel: model.catchSequences) {
      BreakSchrittView breakSchritt = (BreakSchrittView) getParent().findStepByStepID(seqModel.id.toString());
      CatchSchrittSequenzView view = new CatchSchrittSequenzView(editor, this, seqModel, breakSchritt);
      addCatchSequence(view, null, null);
    }
    if (model.sequencesWidthPercent != null) {
      sequencesWidthPercent = model.sequencesWidthPercent;
      recomputeLayout();
    }
    klappen.init(model.zugeklappt);
  }

  @Override
  public Shape getShape() {
    if (bereichPanel.isVisible()) {
      Shape shape = new Shape(getPanel(), this)
        .withBackgroundColor(GAP_COLOR)
        .add(new Shape(topBar).withBackgroundColor(topBar.getBackground()))
        .add(new Shape(bottomBar).withBackgroundColor(bottomBar.getBackground()));
      Shape sequencesShape = new Shape(sequencesPanel);
      for (CatchSchrittSequenzView seq: catchSequences) {
        sequencesShape.add(seq.catchUeberschrift.getShape());
        sequencesShape.add(seq.getShapeSequence());
      }
      shape.add(sequencesShape);
      return decoratedShape(shape);
    }
    return null;
  }

  public void scrollToBreak(StepnumberLabel stepnumberLabel) {
    CatchSchrittSequenzView catchSchrittSequenzView = headingToBranch(stepnumberLabel);
    // The user might not have focussed anything in the catch step sequence before he
    // scrolled to the break step - so in case hwe want's to scroll back by CTRL+ALT+Left,
    // we explicitely add the heading to the edit history here.
    Specman.instance().appendToEditHistory(catchSchrittSequenzView.ueberschrift);
    catchSchrittSequenzView.linkedBreakStep.scrollTo();
  }

  public boolean refersToOtherStep() { return true; }

}
