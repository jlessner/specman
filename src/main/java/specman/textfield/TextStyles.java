package specman.textfield;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import java.awt.*;

public class TextStyles {
  public static final int FONTSIZE = 15;
  public static final int SCHRITTNR_FONTSIZE = 10;

  public static MutableAttributeSet geaendertStil = new SimpleAttributeSet();
  public static MutableAttributeSet geloeschtStil = new SimpleAttributeSet();
  public static MutableAttributeSet ganzerSchrittGeloeschtStil = new SimpleAttributeSet();
  public static MutableAttributeSet standardStil = new SimpleAttributeSet();
  public static MutableAttributeSet quellschrittStil = new SimpleAttributeSet();

  public static Font font = new Font(Font.SERIF, Font.PLAIN, FONTSIZE);
  public static Font labelFont = new Font(Font.SANS_SERIF, Font.BOLD, SCHRITTNR_FONTSIZE);

  public static final Color Hintergrundfarbe_Schrittenummer = Color.LIGHT_GRAY;
  public static final Color Schriftfarbe_Geloescht = Color.LIGHT_GRAY;
  public static final Color Hintergrundfarbe_Geloescht = Color.BLACK;
  public static final Color Schriftfarbe_Standard = Color.BLACK;
  public static final Color Hintergrundfarbe_Standard = Color.WHITE;
  public static final Color AENDERUNGSMARKIERUNG_FARBE = Color.yellow;
  public static final Color AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE = new Color(255, 255, 200);
  public static final Color SCHRITTNUMMER_VORDERGRUNDFARBE = Hintergrundfarbe_Standard;
  public static final Color SCHRITTNUMMER_HINTERGRUNDFARBE2 = Color.BLACK;
  public static final String INDIKATOR_GELB = toHTMLColor(AENDERUNGSMARKIERUNG_FARBE);
  public static final String INDIKATOR_GELOESCHT_MARKIERT = "line-through";

  public static final String INDIKATOR_GRAU = toHTMLColor(Hintergrundfarbe_Schrittenummer);
  public static final String INDIKATOR_SCHWARZ = toHTMLColor(SCHRITTNUMMER_HINTERGRUNDFARBE2);

  static {
    // Das hier ist ein bisschen tricky:
    // Die Zeile mit StyleConstants.setBackground sorgt dafür, dass man die Hintergrundfarbe
    // unmittelbar beim Editieren in der Oberfläche sieht. Allerdings taucht sie dann nicht
    // im abgespeicherten HTML auf und geht auch verloren, sobald man einen Zeilenumbruch im
    // Text einfügt. Also braucht man noch ein weiteres, persistentes Styling über ein Span-Tag,
    // wie ich es hier gefunden habe:
    // https://stackoverflow.com/questions/13285526/jtextpane-text-background-color-does-not-work
    String htmlStyle = "background-color:" + toHTMLColor(Color.yellow);
    String htmlStyleSchwarz = "background-color:" + toHTMLColor(Color.black);
    String htmlStyleStandard = "background-color:" + toHTMLColor(Color.white);

    SimpleAttributeSet htmlHintergrundStyle = new SimpleAttributeSet();
    SimpleAttributeSet htmlHintergrundStyleSchwarz = new SimpleAttributeSet();
    SimpleAttributeSet htmlHintergrundStyleStandard = new SimpleAttributeSet();

    htmlHintergrundStyle.addAttribute(HTML.Attribute.STYLE, htmlStyle);
    geaendertStil.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyle);
    StyleConstants.setBackground(geaendertStil, AENDERUNGSMARKIERUNG_FARBE);

    geloeschtStil.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyle);
    StyleConstants.setBackground(geloeschtStil, AENDERUNGSMARKIERUNG_FARBE);
    StyleConstants.setStrikeThrough(geloeschtStil, true);

    htmlHintergrundStyleSchwarz.addAttribute(HTML.Attribute.STYLE, htmlStyleSchwarz);
    ganzerSchrittGeloeschtStil.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyleSchwarz);
    StyleConstants.setBackground(ganzerSchrittGeloeschtStil, Hintergrundfarbe_Geloescht);
    StyleConstants.setStrikeThrough(ganzerSchrittGeloeschtStil, true);
    StyleConstants.setForeground(ganzerSchrittGeloeschtStil, Schriftfarbe_Geloescht);

    htmlHintergrundStyleStandard.addAttribute(HTML.Attribute.STYLE, htmlStyleStandard);
    standardStil.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyleStandard);
    StyleConstants.setBackground(standardStil, Hintergrundfarbe_Standard);
    StyleConstants.setStrikeThrough(standardStil, false);
    StyleConstants.setForeground(standardStil, Schriftfarbe_Standard);

    quellschrittStil.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyle);
    StyleConstants.setBackground(quellschrittStil, AENDERUNGSMARKIERUNG_FARBE);
    StyleConstants.setStrikeThrough(quellschrittStil, true);
    StyleConstants.setForeground(quellschrittStil, Schriftfarbe_Geloescht);
    StyleConstants.setFontSize(quellschrittStil, 7);
  }

  public static String toHTMLColor(Color color) {
    if (color == null) {
      return "#000000";
    }
    return "#" + Integer.toHexString(color.getRGB()).substring(2).toLowerCase();
  }

}
