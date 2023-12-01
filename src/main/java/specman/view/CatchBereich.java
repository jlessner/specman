package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import specman.Aenderungsart;
import specman.EditException;
import specman.EditorI;
import specman.SpaltenContainerI;
import specman.SpaltenResizer;
import specman.Specman;
import specman.editarea.InteractiveStepFragment;
import specman.editarea.TextEditArea;
import specman.editarea.TextStyles;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.EditorContentModel_V001;

import javax.swing.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import static specman.Aenderungsart.Untracked;
import static specman.editarea.TextStyles.DIAGRAMM_LINE_COLOR;

public class CatchBereich extends AbstractSchrittView implements KlappbarerBereichI, ComponentListener, SpaltenContainerI {
  @Deprecated public CatchSchrittView[] catchBloecke;
  KlappButton klappen;
  JPanel bereichPanel = new JPanel();
  JPanel topBar = new JPanel();
  JPanel bottomBar = new JPanel();
  JPanel sequencesPanel = new JPanel();
  FormLayout bereichLayout;
  FormLayout catchSequencesLayout;
  List<CatchSchrittSequenzView> catchSequences = new ArrayList<>();

  public CatchBereich(SchrittSequenzView parent) {
    super(Specman.instance(), parent, new EditorContentModel_V001(), null, Aenderungsart.Untracked);
    bereichLayout = new FormLayout("10px:grow",
      FORMLAYOUT_GAP + ",fill:10px," + FORMLAYOUT_GAP + ",fill:pref," + FORMLAYOUT_GAP + ",fill:10px");
    bereichPanel.setLayout(bereichLayout);
    bereichPanel.add(topBar, CC.xy(1, 2));
    bereichPanel.add(sequencesPanel, CC.xy(1, 4));
    bereichPanel.add(bottomBar, CC.xy(1, 6));

    bereichPanel.setBackground(DIAGRAMM_LINE_COLOR);
    sequencesPanel.setBackground(DIAGRAMM_LINE_COLOR);
    topBar.setBackground(TextStyles.Hintergrundfarbe_Deviderbar);
    bottomBar.setBackground(TextStyles.Hintergrundfarbe_Deviderbar);

    klappen = new KlappButton(this, topBar, bereichLayout, 2);

    bereichPanel.setVisible(false);
  }

  @Override
  public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
    return null;
  }

  @Override
  public JComponent getPanel() {
    return bereichPanel;
  }

  private void styleBar(JPanel bar) {
    bar.setBackground(TextStyles.Hintergrundfarbe_Deviderbar);
  }

  @Override public void componentResized(ComponentEvent e) {
    klappen.updateLocation(bereichPanel.getWidth());
  }
  @Override public void componentMoved(ComponentEvent e) {}
  @Override public void componentShown(ComponentEvent e) {}
  @Override public void componentHidden(ComponentEvent e) {}

  @Override
  public void geklappt(boolean auf) {

  }

  @Override
  public boolean enthaeltAenderungsmarkierungen() {
    return false;
  }

  public int catchEntfernen(CatchSchrittView schritt) {
    return 0;
  }

  public int catchEntfernen(CatchSchrittSequenzView catchSequence) {
    int index = catchSequences.indexOf(catchSequence);
    catchSequences.remove(index);
    recomputeLayout();
    if (catchSequences.isEmpty()) {
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

  public void entfernen(SchrittSequenzView schrittSequenzView) {

  }

  public void zusammenklappenFuerReview() {


  }

  public BreakSchrittView findeBreakSchritt(String catchText) {
    // A catch sequence must not have break steps. At least not yet.
    return null;
  }

  public void skalieren(int prozentNeu, int prozentAktuell) {
    catchSequences.forEach(seq -> seq.skalieren(prozentNeu, prozentAktuell));
    int barWidth = 10 * prozentNeu / 100;
    RowSpec barRowSpec = RowSpec.decode("fill:" + barWidth + "px");
    bereichLayout.setRowSpec(2, barRowSpec);
    bereichLayout.setRowSpec(6, barRowSpec);
  }

  public CatchSchrittSequenzView catchSequenzAnhaengen(BreakSchrittView breakStepToLink) {
    CatchSchrittSequenzView catchSequence = new CatchSchrittSequenzView(this, breakStepToLink);
    addCatchSequence(catchSequence, null);
    return catchSequence;
  }

  public void addCatchSequence(CatchSchrittSequenzView catchSequence, Integer catchIndex) {
    if (catchIndex == null) {
      catchSequences.add(catchSequence);
    }
    else {
      catchSequences.add(catchIndex, catchSequence);
    }
    bereichPanel.setVisible(true);
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
    String columnSpecs = "10px:grow";
    for (int i = 1; i < catchSequences.size(); i++) {
      columnSpecs += ", 2px, 10px:grow";
    }
    catchSequencesLayout = new FormLayout(columnSpecs, "fill:pref, 2px, fill:pref");
    sequencesPanel.setLayout(catchSequencesLayout);
  }

  @Override
  public int spaltenbreitenAnpassenNachMausDragging(int vergroesserung, int spalte) {
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
    for (CatchSchrittSequenzView seq: catchSequences) {
      changesCommitted += seq.aenderungenUebernehmen(editor);
    }
    return changesCommitted;
  }

  @Override
  public void aenderungsmarkierungenEntfernen() {
    super.aenderungsmarkierungenEntfernen();
    for (CatchSchrittSequenzView seq: catchSequences) {
      seq.aenderungsmarkierungenEntfernen();
    }
  }
}
