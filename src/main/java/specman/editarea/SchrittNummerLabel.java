package specman.editarea;

import specman.SchrittID;
import specman.Specman;
import specman.draganddrop.DragMouseAdapter;
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

  public void setZielschrittStil(SchrittID quellschrittId) {
    wrapAsZiel(quellschrittId);
    setBorder(CHANGED_BORDER);
    setBackground(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
    setForeground(Hintergrundfarbe_Geloescht);
  }

  public void setQuellschrittStil(SchrittID zielschrittID) {
    wrapAsQuelle(zielschrittID);
    setBorder(DELETED_BORDER);
    setBackground(Hintergrundfarbe_Geloescht);
    setForeground(Schriftfarbe_Geloescht);
  }

  public void setGeloeschtStil(SchrittID id) {
    setBorder(DELETED_BORDER);
    setBackground(Hintergrundfarbe_Geloescht);
    setForeground(Schriftfarbe_Geloescht);
    setWrappedText(SPAN_GELOESCHT_MARKIERT, id, SPAN_OUTRO);
  }

  public void wrap(String intro, String outro) {
    setWrappedText(intro, getText(), outro);
  }

  public void wrapAsZiel(SchrittID quellschrittId) {
    wrap(SPAN_INTRO,
      SPAN_OUTRO + SPAN_INTRO + "&lArr" + SPAN_OUTRO + SPAN_GELOESCHT_MARKIERT + quellschrittId + SPAN_OUTRO);
  }

  public void wrapAsQuelle(SchrittID zielschrittID) {
    wrap(SPAN_GELOESCHT_MARKIERT,
      SPAN_OUTRO + SPAN_INTRO + "&rArr" + SPAN_OUTRO + SPAN_INTRO + zielschrittID + SPAN_OUTRO);
  }

  public void wrapAsDeleted() { wrap(SPAN_GELOESCHT_MARKIERT, SPAN_OUTRO); }

  public void setWrappedText(String intro, SchrittID schrittID, String outro) {
    setWrappedText(intro, schrittID.toString(), outro);
  }

  public void setWrappedText(String intro, String schrittNummerText, String outro) {
    setText(HTML_INTRO + BODY_INTRO + intro + schrittNummerText + outro + BODY_OUTRO + HTML_OUTRO);
  }

  public Shape getShape() {
    return new Shape(this).withText(new LabelShapeText(getText(), getInsets(), getForeground(), getFont()));
  }
}