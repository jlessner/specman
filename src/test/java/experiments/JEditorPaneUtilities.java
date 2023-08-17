package experiments;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
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
    field.setText("<html>Lorem ipsum dol\nor <font size=\"+3\">sit</font> amet, consetetur sadipscing <h1>elitr</h1></html>");
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
        // Minimale Höhe des Textfeldes ist 175, aber die Summe der Zeilenhöhen beträgt nur 149. Woher kommt die Differenz von 26 Zeichen?
        // Die Differenz bleibt gleich, auch wenn sich die Anzahl der Zeilen verändert. Es scheint sich also um "Randabstände" zu handeln
        // Ohne die einleitende H1-Überschrift ist die Differenz nur 6. Auch wenn die H1 mitten im Text steht, kommt es zu der Differenz von 26.
        // Bei zwei H1-Überschriften beträgt die Differenz 46. Mit jeder Überschrift kommt als ein Abstand hinzu, der sich nicht in den Zeilenhöhen
        //   ausdrückt.
        // Man sieht aber am Y-Versatz beim Zeilenwechsel, dass es ein Unterabsatz der H1-Bereiche ist. Wäre also quasi nicht so schlimm. Wenn man
        //   eine Folgezeile einer Überschrift hat, dann hat man für die ja auch wieder die Y-Position verfügbar. Hat man *keine* Folgezeile, braucht
        //   man für diese ja auch keine Y-Position mehr. Die Höhe des Gesamtfeldes ist ja bekannt, und es bleibt dann unterhelb einer H1 in der letzten
        //   Zeile der passende leere Abstand.
        try {
          System.out.println(field.getHeight());
          for (int i = 0; i < field.getDocument().getLength(); i++) {
            System.out.println(field.modelToView2D(i));
          }
          for (int pos = 1; pos < field.getDocument().getLength(); pos++) {
            pos = Utilities.getRowEnd(field, pos);
            System.out.println("Zeile bis " + pos + ", Höhe " + field.modelToView2D(pos).getHeight());
          }
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
