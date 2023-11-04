package experiments.jeditorpane;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class JEditorPaneUtilities extends JFrame {

  JEditorPaneUtilities() throws Exception {
    setSize(150, 300);
    setVisible(true);
    Container pane = this.getContentPane();
    pane.setLayout(new FormLayout("80px:grow", "30px,fill:pref:grow,30px"));

    JEditorPane field = new JEditorPane();
    field.setContentType("text/html");
    pane.add(field, CC.xy(1, 2));
    //field.setText("<html>Lorem ipsum dol\nor <font size=\"+3\">sit</font> amet, consetetur sadipscing <h1>elitr</h1></html>");
    //field.setText("<html>eins<i>one</i><font size=\"5\">ONE</font><br>zwei<ul><li>drei</ul>vier</html>");
    field.setText("<html>Nummerierte Liste:<ul><li>eins<li>zwei, zwwei<li>Unterliste<ul><li>drei eins<li>drei zwei</ul></ul>drei");
    //field.setText("<html>  <head>      </head>  <body>    <div>      <font size=\"7\">F</font><span style=\"background-color: #ffffff\"><font color=\"#000000\" size=\"7\">ontgr&#246;&#223;e       </font></span><font color=\"#000000\" size=\"7\"><span style=\"background-color: #ffffff\">Gro&#223;       XX</span></font>    </div>    <div>      <span style=\"background-color: #ffffff\"><font color=\"#000000\" size=\"6\">Fontgr&#246;&#223;e       </font></span><font color=\"#000000\" size=\"6\"><span style=\"background-color: #ffffff\">Gro&#223;       </span><span style=\"background-color: #ffffff\">X</span></font>    </div>    <div>      <span style=\"background-color: #ffffff\"><font color=\"#000000\" size=\"5\">Fontgr&#246;&#223;e       Gro&#223;</font></span>    </div>    <div>      <span style=\"background-color: #ffffff\"><font color=\"#000000\">Fontgr&#246;&#223;e       Mittel</font></span>    </div>    <div>      <span style=\"background-color: #ffffff\"><font color=\"#000000\" size=\"3\">Fontgr&#246;&#223;e       Klein</font></span>    </div>    <div>      <span style=\"background-color: #ffffff\"><font color=\"#000000\" size=\"2\">Fontgr&#246;&#223;e       Klein X</font></span>    </div>    <div>      <span style=\"background-color: #ffffff\"><font color=\"#000000\" size=\"1\">Fontgr&#246;&#223;e       Klein XX</font></span>    </div>  </body></html>");
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);

    this.addComponentListener(new ComponentListener() {
      @Override
      public void componentResized(ComponentEvent e) {
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
        try {
          StyledDocument document = (StyledDocument)field.getDocument();
          System.out.println("Feldhöhe: " + field.getHeight());
          for (int i = 0; i < document.getLength(); i++) {
            Element el = document.getCharacterElement(i);
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
        catch(Exception x) {
          x.printStackTrace();
        }
      }

      @Override
      public void componentMoved(ComponentEvent e) {

      }

      @Override
      public void componentShown(ComponentEvent e) {
      }

      @Override
      public void componentHidden(ComponentEvent e) {

      }
    });
  }

  public static void main(String[] args) throws Exception {
    new JEditorPaneUtilities();
  }

}
