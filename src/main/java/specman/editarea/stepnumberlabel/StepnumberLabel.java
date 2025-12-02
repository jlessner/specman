package specman.editarea.stepnumberlabel;

import specman.SchrittID;
import specman.Specman;
import specman.draganddrop.DragMouseAdapter;
import specman.editarea.InteractiveStepFragment;
import specman.pdf.LineShape;
import specman.undo.props.UDBL;
import specman.pdf.LabelShapeText;
import specman.pdf.Shape;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static specman.SchrittID.asString;
import static specman.editarea.TextStyles.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE;
import static specman.editarea.TextStyles.Hintergrundfarbe_Geloescht;
import static specman.editarea.TextStyles.Hintergrundfarbe_Schrittnummer;
import static specman.editarea.TextStyles.SCHRITTNUMMER_VORDERGRUNDFARBE;
import static specman.editarea.TextStyles.Schriftfarbe_Geloescht;
import static specman.editarea.TextStyles.labelFont;

public class StepnumberLabel extends JLabel implements InteractiveStepFragment {
  private static final Border STANDARD_BORDER = new MatteBorder(0, 2, 0, 1, Hintergrundfarbe_Schrittnummer);
  private static final Border CHANGED_BORDER = new MatteBorder(0, 2, 0, 1, AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
  private static final Border DELETED_BORDER = new MatteBorder(0, 2, 0, 1, Hintergrundfarbe_Geloescht);
  private static final String TO_TARGET_ARROW = " > ";
  private static final String FROM_SOURCE_ARROW = " < ";

  /** Position in the label text which separates a deleted substring of the text from the rest.
   * <br>
   * A negative value indicates a source step label with the source step number in deleted
   * style on the left and the target step number on the right. deletionCut (negated) is
   * the first index of the trailing text which is not deleted-styled. E.g. <br>
   *   Label string "1.2 > 4" where substring "1.2" is displayed as deleted. deletionCut
   *   is 3, so the deleted substring is extracted by substring(0, -deletionCut).
   * <br>
   * A positive value indicates a target step label with the target step on the left and
   * the source step number in deleted style on the right. deletionCut is the first index
   * of the trailing deleted-style text. E.g. <br>
   *   Label string "4 < 1.2" where substring "1.2" is displayed as deleted. deletionCut
   *   is 4, so the deleted substring is extracted by substring(deletionCut, getText().length).
   * <br>
   * Why so complicated? Because this way, the deletionCut integer alone is sufficient
   * to determine which part of the label text is to be drawn with a deletion line and can
   * easily be added to the undo system. Propper restauration of labels on undo / redo
   * requires to add changes to the cut position to the undo buffer.
   */
  private Integer deletionCut;

  public StepnumberLabel(SchrittID stepNumber) {
    super(String.valueOf(stepNumber));

    setFont(labelFont);
    setBackground(Hintergrundfarbe_Schrittnummer);
    setBorder(STANDARD_BORDER);
    setForeground(Color.WHITE);
    setOpaque(true);

    DragMouseAdapter ada = new DragMouseAdapter((Specman)Specman.instance());
    addMouseListener(ada);
    addMouseMotionListener(ada);
    addMouseListener(BreakCatchScrollMouseAdapter.instance);
    addMouseListener(StepnumberContextMenu.instance);
  }

  public void setStepNumber(SchrittID stepNumber) {
    String sourceSuffix = extractSourceSuffix();
    String targetSuffix = extractTargetSuffix();
    setTextUDBL(stepNumber.toString(), targetSuffix, sourceSuffix);
  }

  private String extractTargetSuffix() {
    if (hasTargetSuffix()) {
      return getText().substring(findDelSubstringStart() + TO_TARGET_ARROW.length());
    }
    return null;
  }

  private String extractSourceSuffix() {
    if (hasSourceSuffix()) {
      return getText().substring(findDelSubstringStart());
    }
    return null;
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    drawDeletionLine(g);
  }

  private void drawDeletionLine(Graphics g) {
    LineShape dline = createDeletionLine();
    if (dline != null) {
      g.drawLine(dline.start().x, dline.start().y, dline.end().x, dline.end().y);
    }
  }

  private LineShape createDeletionLine() {
    // + 1 turned out to produce a better vertical line placement
    int VERTICAL_LINE_PLACEMENT_OFFSET = 1;

    Integer delSubStringStart = findDelSubstringStart();
    Integer delSubstringEnd = findDeSubstringEnd();
    if (delSubStringStart != null && delSubstringEnd != null) {
      FontMetrics metrics = getFontMetrics(getFont());
      int undeletedWidth = metrics.stringWidth(getText().substring(0, delSubStringStart));
      int deletedWidth = metrics.stringWidth(getText().substring(delSubStringStart, delSubstringEnd));
      return new LineShape(
        undeletedWidth + VERTICAL_LINE_PLACEMENT_OFFSET,
        getHeight() / 2,
        undeletedWidth + deletedWidth + VERTICAL_LINE_PLACEMENT_OFFSET,
        getHeight() / 2)
        .withColor(getForeground())
        .withWidth(0.5f);
    }
    return null;
  }

  private Integer findDelSubstringStart() {
    if (hasSourceSuffix()) {
      return deletionCut;
    }
    if (hasTargetSuffix() || fullTextDeleted()) {
      return 0;
    }
    return null;
  }

  /** Important to remember: The end index of Java's String#substring method
  * is the index of the first character NOT included in the substring. I.e.
   * a substring from index 0 to index 0 is an empty string. The end index
   * runs from 0 to */
  private Integer findDeSubstringEnd() {
    if (hasTargetSuffix()) {
      return -deletionCut;
    }
    else if (hasSourceSuffix() || fullTextDeleted()) {
      return getText().length();
    }
    return null;
  }

  private boolean fullTextDeleted() {
    return getBackground() == Hintergrundfarbe_Geloescht;
  }

  public void setStandardStyle(SchrittID id) {
    setBorder(STANDARD_BORDER);
    setBackground(Hintergrundfarbe_Schrittnummer);
    setForeground(SCHRITTNUMMER_VORDERGRUNDFARBE);
    this.deletionCut = null;
    setText(id.toString());
  }

  public void setTargetStyleUDBL(SchrittID quellschrittId) {
    setBorderUDBL(CHANGED_BORDER);
    setBackgroundUDBL(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
    setForegroundUDBL(Hintergrundfarbe_Geloescht);
    resyncSourceSuffixUDBL(quellschrittId);
  }

  public void resyncSourceSuffixUDBL(SchrittID sourceStepNumber) {
    setTextUDBL(extractCore(), null, sourceStepNumber.toString());
  }

  private String extractCore() {
    String text = getText();
    if (deletionCut != null) {
      return hasTargetSuffix()
        ? text.substring(0, findDeSubstringEnd())
        : text.substring(0, findDelSubstringStart() - FROM_SOURCE_ARROW.length());
    }
    return text;
  }

  private boolean hasTargetSuffix() {
    return deletionCut != null && deletionCut <= 0;
  }

  private boolean hasSourceSuffix() {
    return deletionCut != null && !hasTargetSuffix();
  }

  public void setSourceStyle(SchrittID zielschrittID) {
    setBorder(DELETED_BORDER);
    setBackground(Hintergrundfarbe_Geloescht);
    setForeground(Schriftfarbe_Geloescht);
    setTextUDBL(getText(), asString(zielschrittID), null);
  }

  private void setTextUDBL(String core, String targetSuffix, String sourceSuffix) {
    if (targetSuffix != null) {
      setDeletionCutUDBL(-core.length());
      setTextUDBL(core + TO_TARGET_ARROW + targetSuffix);
    }
    else if (sourceSuffix != null) {
      String undeletedText = core + FROM_SOURCE_ARROW;
      setDeletionCutUDBL(undeletedText.length());
      setTextUDBL(undeletedText + sourceSuffix);
    }
    else {
      setDeletionCutUDBL(null);
      setTextUDBL(core);
    }
  }

  public void resyncTargetSuffixUDBL(SchrittID zielschrittID) {
    setTextUDBL(extractCore(), asString(zielschrittID), null);
  }

  public void setDeletedStyleUDBL(SchrittID id) {
    setBorderUDBL(DELETED_BORDER);
    setBackgroundUDBL(Hintergrundfarbe_Geloescht);
    setForegroundUDBL(Schriftfarbe_Geloescht);
    setTextUDBL(id.toString(), null, null);
  }

  public Shape getShape() {
    return new Shape(this)
      .withText(new LabelShapeText(getText(), getInsets(), getForeground(), getFont()))
      .add(createDeletionLine());
  }

  public Integer getDeletionCut() { return deletionCut; }
  public void setDeletionCut(Integer deletionCut) { this.deletionCut = deletionCut; }

  private void setDeletionCutUDBL(Integer deletionCut) { UDBL.setDeletionCutUDBL(this, deletionCut); }
  private void setTextUDBL(String text) { UDBL.setTextUDBL(this, text); }
  private void setForegroundUDBL(Color fg) { UDBL.setForegroundUDBL(this, fg); }
  private void setBackgroundUDBL(Color fg) { UDBL.setBackgroundUDBL(this, fg); }
  private void setBorderUDBL(Border border) { UDBL.setBorderUDBL(this, border); }

  @Override
  public String toString() {
    return "SchrittNummerLabel " + getText();
  }

}