package specman.textfield;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import java.awt.Color;
import java.awt.Font;
import java.util.Locale;

public class TextStyles {
    public static final int FONTSIZE = 15;
    public static final int SCHRITTNR_FONTSIZE = 10;

    public static MutableAttributeSet geaendertStil = new SimpleAttributeSet();
    public static MutableAttributeSet geloeschtStil = new SimpleAttributeSet();
    public static MutableAttributeSet ganzerSchrittGeloeschtStil = new SimpleAttributeSet();
    public static MutableAttributeSet standardStil = new SimpleAttributeSet();
    public static MutableAttributeSet quellschrittStil = new SimpleAttributeSet();
    public static MutableAttributeSet stepnumberLinkStyle = new SimpleAttributeSet();
    public static MutableAttributeSet changedStepnumberLinkStyle = new SimpleAttributeSet();
    public static MutableAttributeSet deletedStepnumberLinkStyle = new SimpleAttributeSet();

    //public static Font font = new Font(Font.SERIF, Font.PLAIN, FONTSIZE);
    //public static Font font = new Font("Times New Roman", Font.PLAIN, FONTSIZE);

    /**
     * The following font is part of the Specman delivery and turned out to be rendered
     * almost identical in both Swing UI and PDF export. This is a crucial aspect as the
     * sizes of the text boxes in PDF are directly derived from the sizes in Swing. So the
     * text rendering within the boxes must match very well. It is not easy to find a font
     * which guarantees that. E.g. using simply a font construction like
     * <pre>new Font("Times New Roman", Font.PLAIN, FONTSIZE);</pre>
     * and using "Times New Roman" also for PDF rendering, causes slightly different lengths
     * of the text lines.
     */
    public static final String SERIF_FONTCOLLECTION_REGULAR = "src/main/resources/fonts/Sitka.ttc";
    public static final int FONT_INDEX = 4;

    //public static final String SERIF_FONT = "C:/Windows/Fonts/times.ttf";
    //public static final String SERIF_FONT = "C:/Users/jlessner/AppData/Local/Microsoft/Windows/Fonts/TimesNewRomanPSMT.ttf";
    public static Font font;
    static {
        try {
            Font[] fonts = Font.createFonts(new java.io.File(SERIF_FONTCOLLECTION_REGULAR));
            font = fonts[FONT_INDEX];
        }
        catch(Exception x) {
            x.printStackTrace();
        }
    }

    public static Font labelFont = new Font(Font.SANS_SERIF, Font.BOLD, SCHRITTNR_FONTSIZE);

    public static final Color Hintergrundfarbe_Schrittenummer = Color.LIGHT_GRAY;
    public static final Color Schriftfarbe_Geloescht = Color.LIGHT_GRAY;
    public static final Color Hintergrundfarbe_Schrittnummer = Color.LIGHT_GRAY;
    public static final Color Hintergrundfarbe_Geloescht = Color.BLACK;
    public static final Color Schriftfarbe_Standard = Color.BLACK;
    public static final Color Hintergrundfarbe_Standard = new Color(255, 255, 255, 0);
    public static final Color AENDERUNGSMARKIERUNG_FARBE = Color.yellow;
    public static final Color AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE = new Color(255, 255, 200);
    public static final Color SCHRITTNUMMER_VORDERGRUNDFARBE = Hintergrundfarbe_Standard;
    public static final Color SCHRITTNUMMER_HINTERGRUNDFARBE2 = Color.BLACK;
    public static final Color stepnumberLinkStyleColor = new Color(188, 188, 188);
    public static final Color changedStepnumberLinkColor = combineColors(stepnumberLinkStyleColor, AENDERUNGSMARKIERUNG_FARBE);
    public static final String INDIKATOR_GELB = toHTMLColor(AENDERUNGSMARKIERUNG_FARBE);
    public static final String INDIKATOR_GELOESCHT_MARKIERT = "line-through";

    public static final String SPAN_GELOESCHT_MARKIERT = "<span style='text-decoration: " + INDIKATOR_GELOESCHT_MARKIERT + ";'>";

    public static final String INDIKATOR_GRAU = toHTMLColor(Hintergrundfarbe_Schrittenummer);
    public static final String INDIKATOR_SCHWARZ = toHTMLColor(SCHRITTNUMMER_HINTERGRUNDFARBE2);
    public static final String stepnumberLinkStyleHTMLColor = toHTMLColor(stepnumberLinkStyleColor);
    public static final String changedStepnumberLinkHTMLColor = toHTMLColor(changedStepnumberLinkColor);

    static {
        // Das hier ist ein bisschen tricky:
        // Die Zeile mit StyleConstants.setBackground sorgt dafür, dass man die Hintergrundfarbe
        // unmittelbar beim Editieren in der Oberfläche sieht. Allerdings taucht sie dann nicht
        // im abgespeicherten HTML auf und geht auch verloren, sobald man einen Zeilenumbruch im
        // Text einfügt. Also braucht man noch ein weiteres, persistentes Styling über ein Span-Tag,
        // wie ich es hier gefunden habe:
        // https://stackoverflow.com/questions/13285526/jtextpane-text-background-color-does-not-work
        String htmlStyle = "background-color:" + INDIKATOR_GELB;
        String htmlStyleSchwarz = "background-color:" + INDIKATOR_SCHWARZ;
        String htmlStyleStandard = "background-color:" + toHTMLColor(Hintergrundfarbe_Standard);
        String htmlStyleStepnumber = "background-color:" + stepnumberLinkStyleHTMLColor;

        SimpleAttributeSet htmlHintergrundStyle = new SimpleAttributeSet();
        SimpleAttributeSet htmlHintergrundStyleSchwarz = new SimpleAttributeSet();
        SimpleAttributeSet htmlHintergrundStyleStandard = new SimpleAttributeSet();
        SimpleAttributeSet htmlHintergrundStyleStepnumber = new SimpleAttributeSet();

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

        htmlHintergrundStyleStepnumber.addAttribute(HTML.Attribute.STYLE, htmlStyleStepnumber);
        stepnumberLinkStyle.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyleStepnumber);
        StyleConstants.setBackground(stepnumberLinkStyle, stepnumberLinkStyleColor);

        changedStepnumberLinkStyle.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyle);
        StyleConstants.setBackground(changedStepnumberLinkStyle, changedStepnumberLinkColor);

        deletedStepnumberLinkStyle.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyle);
        StyleConstants.setBackground(deletedStepnumberLinkStyle, changedStepnumberLinkColor);
        StyleConstants.setStrikeThrough(deletedStepnumberLinkStyle, true);
    }

    public static String toHTMLColor(Color color) {
        if (color == null) {
            return "#000000";
        }
        else if (color.getAlpha() != 255) {
            return String.format(Locale.US, "rgba(%d, %d, %d, %1.1f)",
              color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 255f);
        }
        return "#" + Integer.toHexString(color.getRGB()).substring(2).toLowerCase();
    }

    public static Color combineColors(Color color, Color anotherColor) {
        int r = (color.getRed() + anotherColor.getRed()) / 2;
        int g = (color.getGreen() + anotherColor.getGreen()) / 2;
        int b = (color.getBlue() + anotherColor.getBlue()) / 2;
        return new Color(r, g, b);
    }

}