package specman.textfield;

import com.jgoodies.forms.layout.FormLayout;
import specman.EditorI;
import specman.Specman;
import specman.model.v001.Aenderungsmarkierung_V001;
import specman.model.v001.TextMitAenderungsmarkierungen_V001;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.util.ArrayList;
import java.util.List;

import static specman.Specman.schrittHintergrund;

public class TextfieldShef2 extends JPanel implements ComponentListener, KeyListener {
  public static final Color SCHRITTNUMMER_HINTERGRUNDFARBE = Color.LIGHT_GRAY;
  public static final Color AENDERUNGSMARKIERUNG_FARBE = Color.yellow;
  public static final Color AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE = new Color(255, 255, 200);
  public static final String INDIKATOR_GELB = getHTMLColor(AENDERUNGSMARKIERUNG_FARBE);
  public static final String INDIKATOR_AENDERUNGSMARKIERUNG =
      CSS.Attribute.BACKGROUND_COLOR + ": " + INDIKATOR_GELB;
  public static final String INDIKATOR_GELOESCHT_MARKIERT = "line-through";
  public static final int FONTSIZE = 15;
  public static final int SCHRITTNR_FONTSIZE = 10;

  public static MutableAttributeSet geaendertStil = new SimpleAttributeSet();
  public static MutableAttributeSet geloeschtStil = new SimpleAttributeSet();
  public static Font font = new Font(Font.SERIF, Font.PLAIN, FONTSIZE);
  public static Font labelFont = new Font(Font.SANS_SERIF, Font.BOLD, SCHRITTNR_FONTSIZE);

  static {
    // Das hier ist ein bisschen tricky:
    // Die Zeile mit StyleConstants.setBackground sorgt dafür, dass man die Hintergrundfarbe unmittelbar
    // beim Editieren in der Oberflche sieht. Allerdings taucht sie dann nicht im abgespeicherten HTML
    // auf und geht auch verloren, sobald man einen Zeilenumbruch im Text einfügt. Also braucht man noch
    // ein weiteres, persistentes Styling über ein Span-Tag, wie ich es hier gefunden habe:
    // https://stackoverflow.com/questions/13285526/jtextpane-text-background-color-does-not-work
    String htmlStyle = "background-color:" + getHTMLColor(Color.yellow);
    SimpleAttributeSet htmlHintergrundStyle = new SimpleAttributeSet();
    htmlHintergrundStyle.addAttribute(HTML.Attribute.STYLE, htmlStyle);
    geaendertStil.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyle);
    StyleConstants.setBackground(geaendertStil, AENDERUNGSMARKIERUNG_FARBE);

    geloeschtStil.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyle);
    StyleConstants.setBackground(geloeschtStil, AENDERUNGSMARKIERUNG_FARBE);
    StyleConstants.setStrikeThrough(geloeschtStil, true);
  }

  public static String getHTMLColor(Color color) {
    if (color == null) {
      return "#000000";
    }
    return "#" + Integer.toHexString(color.getRGB()).substring(2).toLowerCase();
  }


  private final JEditorPane editorPane;
  private final JLabel schrittNummer;
  boolean schrittNummerSichtbar = true;

  public TextfieldShef2(EditorI editor, String initialerText, String schrittId) {
    editorPane = new JEditorPane();
    editor.instrumentWysEditor(editorPane, initialerText, 0);
    editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    editorPane.setFont(font);
    setBackground(schrittHintergrund());
    editorPane.addKeyListener(this);

    if (schrittId != null) {
      schrittNummer = new JLabel(schrittId);
      schrittNummer.setFont(labelFont);
      schrittNummer.setBackground(SCHRITTNUMMER_HINTERGRUNDFARBE);
      schrittNummer.setBorder(new MatteBorder(0, 2, 1, 1, SCHRITTNUMMER_HINTERGRUNDFARBE));
      schrittNummer.setForeground(Color.WHITE);
      schrittNummer.setOpaque(true);
      editorPane.add(schrittNummer);
      editorPane.addComponentListener(this);
    }
    else {
      schrittNummer = null;
    }

    setLayout(new FormLayout("3px,10px:grow,3px", "3px,fill:pref:grow,3px"));
    add(editorPane);

    if (editor != null) {
      skalieren(editor.getZoomFactor(), 0);
    }
  }

  public TextfieldShef2(EditorI editor) {
    this(editor, null, null);
  }

  @Override public void setBackground(Color bg) {
    super.setBackground(bg);
    editorPane.setBackground(bg);
  }

  public void setId(String id) {
    schrittNummer.setText(id);
  }

  public void setPlainText(String plainText) {
    setPlainText(plainText, StyleConstants.ALIGN_LEFT);
  }

  public void setPlainText(String plainText, int orientation) {
    switch(orientation) {
      case StyleConstants.ALIGN_CENTER:
        editorPane.setText("<div align='center'>" + plainText + "</div>");
        break;
      case StyleConstants.ALIGN_RIGHT:
        editorPane.setText("<div align='right'>" + plainText + "</div>");
        break;
      default:
        editorPane.setText(plainText);
    }
  }

  public void setText(TextMitAenderungsmarkierungen_V001 inhalt) {
    setPlainText(inhalt.text);
    setAenderungsmarkierungen(inhalt.aenderungen);
  }

  public TextMitAenderungsmarkierungen_V001 getTextMitAenderungsmarkierungen(boolean formatierterText) {
    String text;
    java.util.List<Aenderungsmarkierung_V001> aenderungen = null;
    if (formatierterText) {
      // Wenn wir die Zeilenumbrüche nicht rausnehmen, dann entstehen später beim Laden u.U.
      // Leerzeichen an Zeilenenden, die im ursprünglichen Text nicht drin waren. Das ist doof,
      // weil dann die separat abgespeicherten Textintervalle der Aenderungsmarkierungen
      // nicht mehr passen.
      text = editorPane.getText().replace("\n", "");
      aenderungen = findeAenderungsmarkierungen(false);
    }
    else {
      text = getPlainText().replace("\n", " ").trim();
    }
    return new TextMitAenderungsmarkierungen_V001(text, aenderungen);
  }

  public String getPlainText() {
    try {
      return editorPane.getText(0, editorPane.getDocument().getLength());
    }
    catch(BadLocationException blx) {
      blx.printStackTrace();
      return null;
    }
  }

  @Override public void componentMoved(ComponentEvent e) {}
  @Override public void componentHidden(ComponentEvent e) {}
  @Override public void componentShown(ComponentEvent e) {}

  @Override
  public void componentResized(ComponentEvent e) {
    Dimension schrittnummerGroesse = schrittNummer.getPreferredSize();
    if (schrittNummerSichtbar)
      schrittNummer.setBounds(getWidth() - schrittnummerGroesse.width, 0, schrittnummerGroesse.width, schrittnummerGroesse.height - 2);
    else
      schrittNummer.setBounds(0, 0, 0, 0);
  }

  public void schrittnummerAnzeigen(boolean sichtbar) {
    if (schrittNummer != null) {
      schrittNummerSichtbar = sichtbar;
      componentResized(null); // Sorgt daf�r, dass der Label auch optisch sofort verschwindet
    }
  }

  public void repaintSchrittId() {
    if (schrittNummer != null)
      schrittNummer.repaint();
  }

  public java.util.List<Aenderungsmarkierung_V001> findeAenderungsmarkierungen(boolean nurErste) {
    java.util.List<Aenderungsmarkierung_V001> ergebnis = new ArrayList<Aenderungsmarkierung_V001>();
    StyledDocument doc = (StyledDocument)editorPane.getDocument();
    for (Element e: doc.getRootElements()) {
      findeAenderungsmarkierungen(e, ergebnis, nurErste);
      if (ergebnis.size() > 0 && nurErste)
        break;
    }
    return ergebnis;
  }

  private void findeAenderungsmarkierungen(Element e, java.util.List<Aenderungsmarkierung_V001> ergebnis, boolean nurErste) {
    if (elementHatAenderungshintergrund(e)) {
      ergebnis.add(new Aenderungsmarkierung_V001(e.getStartOffset(), e.getEndOffset()));
      if (ergebnis.size() > 0 && nurErste)
        return;
    }
    if (ergebnis.size() == 0 || !nurErste) {
      for (int i = 0; i < e.getElementCount(); i++) {
        findeAenderungsmarkierungen(e.getElement(i), ergebnis, nurErste);
        if (ergebnis.size() > 0 && nurErste)
          break;
      }
    }
  }

  private boolean elementHatAenderungshintergrund(Element e) {
    AttributeSet attr = e.getAttributes();
    Object backgroundColorValue = attr.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
    return (backgroundColorValue != null && backgroundColorValue.toString().equals(INDIKATOR_GELB));
  }

  public void setAenderungsmarkierungen(java.util.List<Aenderungsmarkierung_V001> aenderungen) {
    //TODO JL: Brauchen wir aktuell nicht mehr. Das war nötig, weil die Hintergrundfarbe nicht
    // im abgespeicherten HTML erhalten blieb. Das ist jetzt dank des Tricks aus
    // https://stackoverflow.com/questions/13285526/jtextpane-text-background-color-does-not-work
    // der Fall. Die Funktion kann also evt. weg, sofern wir aus den HTML-Formatierungen allein
    // alle die Änderungsinformationen vollständig wieder auslesen können.
    //		StyledDocument doc = (StyledDocument)getDocument();
    //        MutableAttributeSet attr = new SimpleAttributeSet();
    //        StyleConstants.setBackground(attr, AENDERUNGSMARKIERUNG_FARBE);
    //		for (Aenderungsmarkierung aenderung: aenderungen) {
    //			doc.setCharacterAttributes(aenderung.getVon(), aenderung.laenge(), attr, false);
    //		}
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (Specman.instance().aenderungenVerfolgen()) {
      StyledDocument doc = (StyledDocument)editorPane.getDocument();
      int p0 = editorPane.getSelectionStart();
      int p1 = editorPane.getSelectionEnd();
      if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
        if (p0 != p1) {
          // Eigentlich muss man das hier komplizierter machen. Sind nämlich in der
          // Selektion Zeichen enthalten, die als geändert markiert sind, dann muss
          // man diese entfernen statt sie als gelöscht zu markieren.
          editorPane.setCaretPosition(p0);
          doc.setCharacterAttributes(p0, p1 - p0, geloeschtStil, false);
        }
        else {
          if (aenderungsStilGesetzt())
            return;
          int caretPos = editorPane.getCaretPosition();
          if (caretPos > 0) {
            editorPane.setCaretPosition(caretPos-1);
            doc.setCharacterAttributes(caretPos-1, 1, geloeschtStil, false);
          }
        }
        e.consume();
        return;
      }
      aenderungsStilSetzenWennNochNichtVorhanden();
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {
    if (Specman.instance().aenderungenVerfolgen()) {
      StyledDocument doc = (StyledDocument)editorPane.getDocument();
      int p0 = editorPane.getSelectionStart();
      int p1 = editorPane.getSelectionEnd();
      if (p0 != p1) {
        doc.setCharacterAttributes(p0, p1 - p0, geloeschtStil, false);
        editorPane.setSelectionStart(p1);
        // Jetzt ist am Ende der vorherigen Selektion noch der Geloescht-Stil gesetzt
        // D.h. die Durchstreichung muss noch weg f�r das neue Zeichen, das grade eingef�gt werden soll
        StyledEditorKit k = (StyledEditorKit)editorPane.getEditorKit();
        MutableAttributeSet inputAttributes = k.getInputAttributes();
        StyleConstants.setStrikeThrough(inputAttributes, false);
      }
    }
  }

  private void aenderungsStilSetzenWennNochNichtVorhanden() {
    // Durch die folgende If-Abfrage verhindert man, dass die als geändert markierten Buchstaben
    // alle einzelne Elements werden. Wenn an der aktuellen Position schon gelbe Hintegrundfarbe
    // eingestellt ist, dann ändern wir den aktuellen Style gar nicht mehr.
    if (!aenderungsStilGesetzt()) {
      StyledEditorKit k = (StyledEditorKit)editorPane.getEditorKit();
      MutableAttributeSet inputAttributes = k.getInputAttributes();
      StyleConstants.setStrikeThrough(inputAttributes, false); // Falls noch Geloescht-Stil herrschte
      inputAttributes.addAttributes(geaendertStil);
    }
  }

  private boolean aenderungsStilGesetzt() {
    StyledEditorKit k = (StyledEditorKit)editorPane.getEditorKit();
    MutableAttributeSet inputAttributes = k.getInputAttributes();
    Object currentTextDecoration = inputAttributes.getAttribute(CSS.Attribute.TEXT_DECORATION);
    if (currentTextDecoration != null && currentTextDecoration.toString().equals(INDIKATOR_GELOESCHT_MARKIERT))
      return false;
    Object currentBackgroundColorValue = inputAttributes.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
    return currentBackgroundColorValue != null && currentBackgroundColorValue.toString().equalsIgnoreCase(INDIKATOR_GELB);
  }


  @Override public void keyReleased(KeyEvent e) {}

  public void skalieren(int prozentNeu, int prozentAktuell) {
    setFont(font.deriveFont((float)FONTSIZE * prozentNeu / 100));
    if (schrittNummer != null) {
      schrittNummer.setFont(labelFont.deriveFont((float)SCHRITTNR_FONTSIZE * prozentNeu / 100));
    }
    // prozentAktuell = 0 ist ein Indikator für initiales Laden. Da brauchen wir nur den Font
    // anpassen. Die Bilder stehen bereits entsprechend des im Modell abgespeicherten Zoomfaktors
    // skaliert im HTML.
    if (prozentAktuell != 0 && prozentNeu != prozentAktuell) {
      ImageScaler imageScaler = new ImageScaler(prozentNeu, prozentAktuell);
      editorPane.setText(imageScaler.scaleImages(editorPane.getText()));
    }
  }

  public java.util.List<Line2D.Double> getRechteZeilenraender() {
    java.util.List<Line2D.Double> raender = new ArrayList<Line2D.Double>();
    try {
      int anzahlZeichen = getPlainText().length();
      int offset = 1;
      while (offset < anzahlZeichen) {
        System.out.println(offset);
        offset = Utilities.getRowEnd(editorPane, offset) + 1;
        Rectangle r = editorPane.modelToView(offset-1);
        Line2D.Double randLinie = new Line2D.Double(r.getX(), r.getY(), r.getX(), r.getMaxY());
        raender.add(randLinie);
      }
    }
    catch(BadLocationException blx) {
      blx.printStackTrace();
    }
    return raender;
  }

  public java.util.List<Line2D.Double> getLinkeZeilenraender() {
    List<Line2D.Double> raender = new ArrayList<Double>();
    try {
      int anzahlZeichen = getPlainText().length();
      int offset = anzahlZeichen;
      while (offset > 0) {
        System.out.println(offset);
        offset = Utilities.getRowStart(editorPane, offset) - 1;
        Rectangle r = editorPane.modelToView(offset+1);
        Line2D.Double randLinie = new Line2D.Double(r.getX(), r.getY(), r.getX(), r.getMaxY());
        raender.add(randLinie);
      }
    }
    catch(BadLocationException blx) {
      blx.printStackTrace();
    }
    return raender;
  }

  public static String right(String text) {
    return "<div align='right'>" + Specman.initialtext(text) + "</div>";
  }

  public static String center(String text) {
    return "<div align='center'>" + Specman.initialtext(text) + "</div>";
  }

}
