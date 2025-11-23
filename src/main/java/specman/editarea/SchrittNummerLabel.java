package specman.editarea;

import specman.SchrittID;
import specman.Specman;
import specman.draganddrop.DragMouseAdapter;
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

public class SchrittNummerLabel extends JLabel implements InteractiveStepFragment {
  private static final Border STANDARD_BORDER = new MatteBorder(0, 2, 0, 1, Hintergrundfarbe_Schrittnummer);
  private static final Border CHANGED_BORDER = new MatteBorder(0, 2, 0, 1, AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
  private static final Border DELETED_BORDER = new MatteBorder(0, 2, 0, 1, Hintergrundfarbe_Geloescht);
  private static final String TO_TARGET_ARROW = " > ";
  private static final String FROM_SOURCE_ARROW = " < ";

  /** Position in the label text which separates a deleted part of the text from the rest.
   * <br>
   * A negative value indicates a source step label with the source step number in deleted
   * style on the left and the target step number on the right. deletionCut (negated) is
   * the last index of the leading deleted-styled text.
   * <br>
   * A positive value indicates a target step label with the target step on the left and
   * the source step number in deleted style on the right. deletionCut is the first index
   * of the trailing deleted-style text. */
  Integer deletionCut;

  public SchrittNummerLabel(SchrittID stepNumber) {
    super(String.valueOf(stepNumber));

    setFont(labelFont);
    setBackground(Hintergrundfarbe_Schrittnummer);
    setBorder(STANDARD_BORDER);
    setForeground(Color.WHITE);
    setOpaque(true);

    DragMouseAdapter ada = new DragMouseAdapter((Specman)Specman.instance());
    addMouseListener(ada);
    addMouseMotionListener(ada);
  }

  public void setStepNumber(SchrittID stepNumber) {
    String sourceSuffix = extractSourceSuffix();
    String targetSuffix = extractTargetSuffix();
    setTextUDBL(stepNumber.toString(), targetSuffix, sourceSuffix);
  }

  private String extractTargetSuffix() {
    if (hasTargetSuffix()) {
      return getText().substring(-deletionCut + TO_TARGET_ARROW.length());
    }
    return null;
  }

  private String extractSourceSuffix() {
    if (hasSourceSuffix()) {
      return getText().substring(deletionCut);
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
    Integer deletionStart = findDeletionStart();
    Integer deletionEnd = findDeletionEnd();
    if (deletionStart != null && deletionEnd != null) {
      FontMetrics metrics = getFontMetrics(getFont());
      int undeletedWidth = metrics.stringWidth(getText().substring(0, deletionStart));
      int deletedWidth = metrics.stringWidth(getText().substring(deletionStart, deletionEnd));
      return new LineShape(
        undeletedWidth + 1,
        getHeight() / 2,
        undeletedWidth + deletedWidth + 1, getHeight() / 2)
        .withColor(getForeground())
        .withWidth(0.5f);
    }
    return null;
  }

  private Integer findDeletionStart() {
    if (hasSourceSuffix()) {
      return deletionCut;
    }
    if (hasTargetSuffix() || hasDeletedStyle()) {
      return 0;
    }
    return null;
  }

  private Integer findDeletionEnd() {
    if (hasTargetSuffix()) {
      return -deletionCut-1;
    }
    else if (hasSourceSuffix() || hasDeletedStyle()) {
      return getText().length();
    }
    return null;
  }

  private boolean hasDeletedStyle() {
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
        ? text.substring(0, -deletionCut-1)
        : text.substring(0, deletionCut - FROM_SOURCE_ARROW.length());
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
      setDeletionCutUDBL((-core.length()) - 1);
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

  @Override
  public void setText(String text) {
    super.setText(text);
  }

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