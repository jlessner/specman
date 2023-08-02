package specman.textfield;

import specman.SchrittID;
import specman.Specman;
import specman.draganddrop.DragMouseAdapter;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

import java.awt.*;

import static specman.textfield.TextStyles.*;

public class SchrittNummerLabel extends JLabel {
  private static final Border STANDARD_BORDER = new MatteBorder(0, 2, 1, 1, Hintergrundfarbe_Schrittnummer);
  private static final Border CHANGED_BORDER = new MatteBorder(0, 2, 1, 1, AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
  private static final Border DELETED_BORDER = new MatteBorder(0, 2, 1, 1, Hintergrundfarbe_Geloescht);

  public SchrittNummerLabel(String schrittId) {
    super(schrittId);
    setFont(labelFont);
    setBackground(Hintergrundfarbe_Schrittnummer);
    setBorder(STANDARD_BORDER);
    setForeground(Color.WHITE);
    setOpaque(true);

    DragMouseAdapter ada = new DragMouseAdapter(Specman.instance());
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
    setWrappedText(SPAN_GELOESCHT_MARKIERT, id, "</span>");
  }

  public void wrap(String intro, String outro) {
    setWrappedText(intro, getText(), outro);
  }

  public void wrapAsZiel(SchrittID quellschrittId) {
    wrap("<span>",
      "</span><span>&lArr</span>" + SPAN_GELOESCHT_MARKIERT + quellschrittId + "</span>");
  }

  public void wrapAsQuelle(SchrittID zielschrittID) {
    wrap(SPAN_GELOESCHT_MARKIERT,
      "</span><span>&rArr</span><span>" + zielschrittID + "</span>");
  }

  public void wrapAsDeleted() { wrap(SPAN_GELOESCHT_MARKIERT, "</span>"); }

  public void setWrappedText(String intro, SchrittID schrittID, String outro) {
    setWrappedText(intro, schrittID.toString(), outro);
  }

  public void setWrappedText(String intro, String schrittNummerText, String outro) {
    setText("<html><body>" + intro + schrittNummerText + outro + "</body></html>");
  }

}
