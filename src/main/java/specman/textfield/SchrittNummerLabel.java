package specman.textfield;

import specman.SchrittID;

import javax.swing.*;

import static specman.textfield.TextStyles.SPAN_GELOESCHT_MARKIERT;

public class SchrittNummerLabel extends JLabel {

  public SchrittNummerLabel(String schrittId) {
    super(schrittId);
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

  public void wrapAsDeleted() {
    wrap(SPAN_GELOESCHT_MARKIERT, "</span>");
  }

  public void setWrappedText(String intro, SchrittID schrittID, String outro) {
    setWrappedText(intro, schrittID.toString(), outro);
  }

  public void setWrappedText(String intro, String schrittNummerText, String outro) {
    setText("<html><body>" + intro + schrittNummerText + outro + "</body></html>");
  }


}
