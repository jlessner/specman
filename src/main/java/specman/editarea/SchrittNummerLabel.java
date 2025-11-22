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

  private SchrittID stepNumber, sourceStepNumber, targetStepNumber;

  public SchrittNummerLabel(SchrittID stepNumber) {
    super(String.valueOf(stepNumber));
    this.stepNumber = stepNumber;

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
    this.stepNumber = stepNumber;
    assembleTextUDBL();
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
    if (sourceStepNumber != null) {
      return createDeletionLine(stepNumber.toString().length() + FROM_SOURCE_ARROW.length(), sourceStepNumber.toString().length());
    }
    else if (targetStepNumber != null) {
      return createDeletionLine(0, stepNumber.toString().length());
    }
    return null;
  }

  private LineShape createDeletionLine(int fromTextIndex, int textLength) {
    FontMetrics metrics = getFontMetrics(getFont());
    int undeletedWidth = metrics.stringWidth(getText().substring(0, fromTextIndex));
    int deletedWidth = metrics.stringWidth(getText().substring(fromTextIndex, fromTextIndex + textLength));
    return new LineShape(
      undeletedWidth + 1,
      getHeight() / 2,
      undeletedWidth + deletedWidth + 1, getHeight() / 2)
      .withColor(getForeground())
      .withWidth(0.5f);
  }

  public void setStandardStil(SchrittID id) {
    setBorder(STANDARD_BORDER);
    setBackground(Hintergrundfarbe_Schrittnummer);
    setForeground(SCHRITTNUMMER_VORDERGRUNDFARBE);
    this.stepNumber = id;
    this.targetStepNumber = null;
    assembleTextUDBL();
  }

  public void setTargetStyleUDBL(SchrittID quellschrittId) {
    setBorderUDBL(CHANGED_BORDER);
    setBackgroundUDBL(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
    setForegroundUDBL(Hintergrundfarbe_Geloescht);
    markAsTargetUDBL(quellschrittId);
  }

  public void markAsTargetUDBL(SchrittID sourceStepNumber) {
    this.sourceStepNumber = sourceStepNumber;
    assembleTextUDBL();
  }

  public void setSourceStyle(SchrittID zielschrittID) {
    setBorder(DELETED_BORDER);
    setBackground(Hintergrundfarbe_Geloescht);
    setForeground(Schriftfarbe_Geloescht);
    markAsSourceUDBL(zielschrittID);
  }

  public void markAsSourceUDBL(SchrittID zielschrittID) {
    this.targetStepNumber = zielschrittID;
    assembleTextUDBL();
  }

  public void setDeletedStyleUDBL(SchrittID id) {
    setBorderUDBL(DELETED_BORDER);
    setBackgroundUDBL(Hintergrundfarbe_Geloescht);
    setForegroundUDBL(Schriftfarbe_Geloescht);
    stepNumber = id;
    markAsDeletedUDBL();
  }

  public void markAsDeletedUDBL() {
    // Only change and record undo action if necessary
    if (targetStepNumber != stepNumber) {
      targetStepNumber = stepNumber;
      assembleTextUDBL();
    }
  }

  private void assembleTextUDBL() {
    String text = String.valueOf(stepNumber);
    if (sourceStepNumber != null) {
      text = text + FROM_SOURCE_ARROW + sourceStepNumber;
    }
    else if (targetStepNumber != null && targetStepNumber != stepNumber) {
      text = text + TO_TARGET_ARROW + targetStepNumber;
    }
    setTextUDBL(text);
  }

  public Shape getShape() {
    return new Shape(this)
      .withText(new LabelShapeText(getText(), getInsets(), getForeground(), getFont()))
      .add(createDeletionLine());
  }

  private void setTextUDBL(String text) { UDBL.setTextUDBL(this, text); }
  private void setForegroundUDBL(Color fg) { UDBL.setForegroundUDBL(this, fg); }
  private void setBackgroundUDBL(Color fg) { UDBL.setBackgroundUDBL(this, fg); }
  private void setBorderUDBL(Border border) { UDBL.setBorderUDBL(this, border); }

  @Override
  public String toString() {
    return "SchrittNummerLabel " + getText();
  }

}