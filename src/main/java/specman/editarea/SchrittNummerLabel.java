package specman.editarea;

import specman.SchrittID;
import specman.Specman;
import specman.draganddrop.DragMouseAdapter;
import specman.undo.props.UDBL;
import specman.pdf.LabelShapeText;
import specman.pdf.Shape;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static specman.editarea.HTMLTags.BODY_INTRO;
import static specman.editarea.HTMLTags.BODY_OUTRO;
import static specman.editarea.HTMLTags.HTML_INTRO;
import static specman.editarea.HTMLTags.HTML_OUTRO;
import static specman.editarea.HTMLTags.SPAN_INTRO;
import static specman.editarea.HTMLTags.SPAN_OUTRO;
import static specman.editarea.TextStyles.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE;
import static specman.editarea.TextStyles.Hintergrundfarbe_Geloescht;
import static specman.editarea.TextStyles.Hintergrundfarbe_Schrittenummer;
import static specman.editarea.TextStyles.Hintergrundfarbe_Schrittnummer;
import static specman.editarea.TextStyles.SCHRITTNUMMER_VORDERGRUNDFARBE;
import static specman.editarea.TextStyles.SPAN_GELOESCHT_MARKIERT;
import static specman.editarea.TextStyles.Schriftfarbe_Geloescht;
import static specman.editarea.TextStyles.labelFont;

public class SchrittNummerLabel extends JLabel implements InteractiveStepFragment {
  private static final Border STANDARD_BORDER = new MatteBorder(0, 2, 0, 1, Hintergrundfarbe_Schrittnummer);
  private static final Border CHANGED_BORDER = new MatteBorder(0, 2, 0, 1, AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
  private static final Border DELETED_BORDER = new MatteBorder(0, 2, 0, 1, Hintergrundfarbe_Geloescht);

  public SchrittNummerLabel(String schrittId) {
    super(schrittId);
    setFont(labelFont);
    setBackground(Hintergrundfarbe_Schrittnummer);
    setBorder(STANDARD_BORDER);
    setForeground(Color.WHITE);
    setOpaque(true);

    DragMouseAdapter ada = new DragMouseAdapter((Specman)Specman.instance());
    addMouseListener(ada);
    addMouseMotionListener(ada);
  }

  public void setStandardStil(SchrittID id) {
    setText(String.valueOf(id));
    setBorder(STANDARD_BORDER);
    setBackground(Hintergrundfarbe_Schrittenummer);
    setForeground(SCHRITTNUMMER_VORDERGRUNDFARBE);
  }

  public void setZielschrittStilUDBL(SchrittID quellschrittId) {
    wrapAsZielUDBL(quellschrittId);
    setBorderUDBL(CHANGED_BORDER);
    setBackgroundUDBL(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
    setForegroundUDBL(Hintergrundfarbe_Geloescht);
  }

  public void setQuellschrittStil(SchrittID zielschrittID) {
    wrapAsQuelleUDBL(zielschrittID);
    setBorder(DELETED_BORDER);
    setBackground(Hintergrundfarbe_Geloescht);
    setForeground(Schriftfarbe_Geloescht);
  }

  public void setGeloeschtStilUDBL(SchrittID id) {
    setBorderUDBL(DELETED_BORDER);
    setBackgroundUDBL(Hintergrundfarbe_Geloescht);
    setForegroundUDBL(Schriftfarbe_Geloescht);
    setWrappedTextUDBL(SPAN_GELOESCHT_MARKIERT, id, SPAN_OUTRO);
  }

  public void wrapUDBL(String intro, String outro) {
    setWrappedTextUDBL(intro, getText(), outro);
  }

  public void wrapAsZielUDBL(SchrittID quellschrittId) {
    wrapUDBL(SPAN_INTRO,
      SPAN_OUTRO + SPAN_INTRO + "&lArr" + SPAN_OUTRO + SPAN_GELOESCHT_MARKIERT + quellschrittId + SPAN_OUTRO);
  }

  public void wrapAsQuelleUDBL(SchrittID zielschrittID) {
    wrapUDBL(SPAN_GELOESCHT_MARKIERT,
      SPAN_OUTRO + SPAN_INTRO + "&rArr" + SPAN_OUTRO + SPAN_INTRO + zielschrittID + SPAN_OUTRO);
  }

  public void wrapAsDeletedUDBL() { wrapUDBL(SPAN_GELOESCHT_MARKIERT, SPAN_OUTRO); }

  public void setWrappedTextUDBL(String intro, SchrittID schrittID, String outro) {
    setWrappedTextUDBL(intro, schrittID.toString(), outro);
  }

  public void setWrappedTextUDBL(String intro, String schrittNummerText, String outro) {
    setTextUDBL(HTML_INTRO + BODY_INTRO + intro + schrittNummerText + outro + BODY_OUTRO + HTML_OUTRO);
  }

  public Shape getShape() {
    return new Shape(this).withText(new LabelShapeText(getText(), getInsets(), getForeground(), getFont()));
  }

  public void setTextUDBL(String text) { UDBL.setTextUDBL(this, text); }
  public void setForegroundUDBL(Color fg) { UDBL.setForegroundUDBL(this, fg); }
  public void setBackgroundUDBL(Color fg) { UDBL.setBackgroundUDBL(this, fg); }
  private void setBorderUDBL(Border border) { UDBL.setBorderUDBL(this, border); }

}