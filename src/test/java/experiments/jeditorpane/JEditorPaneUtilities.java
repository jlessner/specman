package experiments.jeditorpane;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import specman.editarea.TextStyles;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class JEditorPaneUtilities extends JFrame {
  private JEditorPane field;
  private MutableAttributeSet schriftstilStandard = new SimpleAttributeSet();
  private MutableAttributeSet schriftstilGrossFett = new SimpleAttributeSet();
  private MutableAttributeSet aenderungsstil = new SimpleAttributeSet();
  private JButton normal;
  private JButton big;

  JEditorPaneUtilities() throws Exception {
    setSize(300, 500);
    Container pane = this.getContentPane();
    pane.setLayout(new FormLayout("80px:grow", "30px,fill:pref:grow,30px,30px"));

    field = new JEditorPane();
    field.setContentType("text/html");
    field.setFont(TextStyles.font.deriveFont((float) 15));
    field.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    pane.add(field, CC.xy(1, 2));

    normal = new JButton("Normal");
    pane.add(normal, CC.xy(1, 3));
    normal.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int start = field.getSelectionStart();
        int end = field.getSelectionEnd();
        System.out.println(start + "/" + end);
        StyledDocument document = (StyledDocument)field.getDocument();
        document.setCharacterAttributes(start, end-start, schriftstilStandard, false);
        System.out.println(field.getText());
      }
    });

    big = new JButton("Big");
    pane.add(big, CC.xy(1, 4));
    big.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int start = field.getSelectionStart();
        int end = field.getSelectionEnd();
        System.out.println(start + "/" + end);
        StyledDocument document = (StyledDocument)field.getDocument();
        document.setCharacterAttributes(start, end-start, schriftstilGrossFett, false);
        System.out.println(field.getText());
      }
    });

    this.setDefaultCloseOperation(EXIT_ON_CLOSE);

    StyleConstants.setBold(schriftstilStandard, false);
    StyleConstants.setFontSize(schriftstilStandard, 12);
    StyleConstants.setFontFamily(schriftstilStandard, "Helvetica");

    StyleConstants.setBold(schriftstilGrossFett, true);
    // Wenn man 20 setzt und hinterher den Stil abfragt, kommt 24 raus. Bei 16 kommt 18 raus,
    // d.h. intern passiert noch eine Umrechnung. Diese Faktoren muss man also initial einmal
    // feststellen, wenn man für jedes Zeichen wieder rückwärts herausbekommen will, welches
    // Styling darauf angewendet wurde.
    SimpleAttributeSet htmlFontGross = new SimpleAttributeSet();
    htmlFontGross.addAttribute(HTML.Attribute.STYLE, "font-size:large");
    StyleConstants.setFontSize(schriftstilGrossFett, 20);
    StyleConstants.setFontFamily(schriftstilGrossFett, "Helvetica");
    schriftstilGrossFett.addAttribute(HTML.Tag.SPAN, htmlFontGross);

    // Die Hauptstile äußern sich nicht zu strikethrough und Hintergrund, deswegen kann
    // man den Änderungsstil *überlagert* anwenden.
    // Das ist schön einfach, aber die Frage ist, wie man das überlagerte wieder wegkriegt
    String htmlStyleGeaendert = "background-color:" + TextStyles.INDIKATOR_GELB;
    SimpleAttributeSet htmlHintergrundStyleGeaendert = new SimpleAttributeSet();
    htmlHintergrundStyleGeaendert.addAttribute(HTML.Attribute.STYLE, htmlStyleGeaendert);
    aenderungsstil.addAttribute(HTML.Tag.SPAN, htmlHintergrundStyleGeaendert);
    StyleConstants.setBackground(aenderungsstil, TextStyles.AENDERUNGSMARKIERUNG_FARBE);
    StyleConstants.setStrikeThrough(aenderungsstil, true);

    //field.setText("<html>Lorem ipsum dol\nor <font size=\"+3\">sit</font> amet, consetetur sadipscing <h1>elitr</h1></html>");
    //field.setText("<html>eins<i>one</i><font size=\"5\">ONE</font><br>zwei<ul><li>drei</ul>vier</html>");

//    field.setText(
//      "<html>\n" +
//        "  <body>\n" +
//        "    <b><font size=\"6\" face=\"Helvetica\">&#220;<span style=\"background-color:#ffff00\"><strike>be</strike></span></font></b><span style=\"background-color:#ffff00\"><strike><font size=\"3\" face=\"Helvetica\">rschr</font><b><font size=\"6\" face=\"Helvetica\">ift</font></b>  " +
//        "  </body>\n" +
//        "</html>\n");

        field.setText(
      "<html><body>\n" +
        "<font size=\"1\">1</font>" +
        "<font size=\"2\">2</font>" +
        "<font size=\"3\">3</font>" +
        "<font size=\"4\">4</font>" +
        "<font size=\"5\">5</font>" +
        "<font size=\"6\">6</font>" +
        "<font size=\"7\">7</font>" +
        "</body></html>\n");
    StyledDocument document = (StyledDocument)field.getDocument();
    for (int i = 0; i < document.getLength(); i++) {
      System.out.println(StyleConstants.getFontSize(document.getCharacterElement(i).getAttributes()));
    }


//    field.setText("<html>Überschrift<br>blabla<br>Nummerierte Liste:<ul><li>eins<li>zwei, zwwei<li>Unterliste<ul><li>drei eins<li>drei zwei</ul></ul>drei<br><br><br>vier");
//    StyledDocument document = (StyledDocument)field.getDocument();
//    document.setCharacterAttributes(0, document.getLength(), schriftstilStandard, false);
//    document.setCharacterAttributes(1, 11, schriftstilGrossFett, false);
//    document.setCharacterAttributes(2, 40, aenderungsstil, false);
//    document.setCharacterAttributes(4, 5, schriftstilStandard, false);

    this.addComponentListener(new ComponentListener() {
      @Override
      public void componentResized(ComponentEvent e) {
        try {
          splitByLines();
          //checkCharacterStyling();
        }
        catch(Exception x) {
          x.printStackTrace();
        }
      }

      @Override
      public void componentMoved(ComponentEvent e) {

      }

      @Override
      public void componentShown(ComponentEvent e) {
        try {
          checkCharacterStyling();
        }
        catch(Exception x) {
          x.printStackTrace();
        }
      }

      @Override
      public void componentHidden(ComponentEvent e) {

      }
    });

    setVisible(true);

  }

  private void checkCharacterStyling() throws BadLocationException {
    StyledDocument document = (StyledDocument)field.getDocument();
    for (int i = 0; i < 12; i++) {
      AttributeSet as = document.getCharacterElement(i).getAttributes();
      System.out.print(set2string(as));
      System.out.println();
    }

    System.out.println(field.getText());
  }

  String set2string(AttributeSet as) {
    return as.getAttribute(StyleConstants.FontSize) +
      "/" + as.getAttribute(StyleConstants.FontFamily) +
      "/" + as.getAttribute(StyleConstants.StrikeThrough) +
      "/" + as.getAttribute(StyleConstants.Background) +
      " ";
  }

  private void splitByLines() throws BadLocationException {
    // Erkenntnisse:
    // Alle Rechtecke aller Buchstaben in einer Zeile mit einem sehr großen Buchstaben kriegen dessen Höhe
    // Bei HTML-Typ haben die Rechtecke alle die Breite 0.
    // An den Y-Positionen sieht man die Zeilen
    // Die Differenz der Y-Werte entspricht der Höhe der größten Buchstaben in den Zeilen. Da ist also kein magischer Platz mehr dazwischen
    // An Offset 0 steht ein Zeilenumbruch ohne Ausdehnung (Höhe 0) anhand dessen man auch nicht das rowEnd der ersten Zeile bestimmen kann.
    // Man muss zum nächsten "echten" Zeichen gehen.
    // Minimale Höhe des Textfeldes ist 175, aber die Summe der Zeilenhöhen beträgt nur 149. Woher kommt die Differenz von 26 Pixel?
    // Die Differenz bleibt gleich, auch wenn sich die Anzahl der Zeilen verändert. Es scheint sich also um "Randabstände" zu handeln
    // Ohne die einleitende H1-Überschrift ist die Differenz nur 6. Auch wenn die H1 mitten im Text steht, kommt es zu der Differenz von 26.
    // Bei zwei H1-Überschriften beträgt die Differenz 46. Mit jeder Überschrift kommt als ein Abstand hinzu, der sich nicht in den Zeilenhöhen
    //   ausdrückt.
    // Man sieht aber am Y-Versatz beim Zeilenwechsel, dass es ein Unterabsatz der H1-Bereiche ist. Wäre also quasi nicht so schlimm. Wenn man
    //   eine Folgezeile einer Überschrift hat, dann hat man für die ja auch wieder die Y-Position verfügbar. Hat man *keine* Folgezeile, braucht
    //   man für diese ja auch keine Y-Position mehr. Die Höhe des Gesamtfeldes ist ja bekannt, und es bleibt dann unterhalb einer H1 in der letzten
    //   Zeile der passende leere Abstand.
    // Eine neue Zeile für ein Listitem fängt an der eingerückten Y-Position des Textes an. Gilt auch für Folgezeilen im Falle eines Umbruchs
    //   innerhalb des Listitems.
    // Die Einrückung pro Listenebene beträgt 50 Pixel, sowohl für OL als auch für UL.
    // Aktuell ist nicht klar, wie man einer aus dem Dokument extrahierten Zeile ansieht, ob sie die erste Zeile eines Listitems ist und nicht etwas
    //   nur eine Folgezeile desselben Items durch einen Umbruch des Item-Texts.
      StyledDocument document = (StyledDocument)field.getDocument();
      System.out.println("Feldhöhe: " + field.getHeight());
      for (int i = 0; i < document.getLength(); i++) {
        System.out.println(i + ", " + field.modelToView2D(i));
      }
      int summeZeilenhoehen = 0;
      for (int pos = 1; pos < document.getLength(); pos++) {
        pos = Utilities.getRowEnd(field, pos);
        System.out.println("Zeile bis " + pos + ", Höhe " + field.modelToView2D(pos).getHeight() + ", y = " + field.modelToView2D(pos).getY());
        summeZeilenhoehen += field.modelToView2D(pos).getHeight();
      }
      System.out.println("Summe der Zeilenhöhen: " + summeZeilenhoehen);
  }

  public static void main(String[] args) throws Exception {
    new JEditorPaneUtilities();
  }

}
