package experiments.jeditorpane;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.element.Paragraph;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import specman.pdf.TextlineDimension;
import specman.editarea.litrack.LIRecordingHTMLEditorKit;
import specman.editarea.litrack.LIRecordingListView;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class JEditorPaneLines2PDFTest extends JFrame implements ComponentListener {
  JEditorPane field = new JEditorPane();

  public static void main(String[] args) throws Exception {
    new JEditorPaneLines2PDFTest();
  }

  JEditorPaneLines2PDFTest() throws Exception {
    setSize(200, 350);
    Container pane = this.getContentPane();
    pane.setLayout(new FormLayout("80px:grow", "30px,fill:pref:grow,30px"));
    field.setEditorKit(new LIRecordingHTMLEditorKit());
    field.setContentType("text/html");
    pane.add(field, CC.xy(1, 2));
    JButton button = new JButton("LOS");
    pane.add(button, CC.xy(1, 1));
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        field.setVisible(false);
        field.setVisible(true);
        LIRecordingListView.startRecording();
        SwingUtilities.invokeLater(() -> {
          System.out.println("Repaint done");
          lines2PDF();
          LIRecordingListView.stopRecording();
        });
      }
    });
    field.setText("eins<ul><li>zwei zwei zwei zwei zwei zwei zwei<li><font size=\"6\">dreig</font><li>dreieinhalb<ol><li>drei eins<li>drei zwei</ol></ul>vier<br><font size=\"6\">fünf</font><br>sechs");
    //field.setText("<html>  <head>      </head>  <body>    <div>      <font size=\"7\">F</font><span style=\"background-color: #ffffff\"><font color=\"#000000\" size=\"7\">ontgr&#246;&#223;e       </font></span><font color=\"#000000\" size=\"7\"><span style=\"background-color: #ffffff\">Gro&#223;       XX</span></font>    </div>    <div>      <span style=\"background-color: #ffffff\"><font color=\"#000000\" size=\"6\">Fontgr&#246;&#223;e       </font></span><font color=\"#000000\" size=\"6\"><span style=\"background-color: #ffffff\">Gro&#223;       </span><span style=\"background-color: #ffffff\">X</span></font>    </div>    <div>      <span style=\"background-color: #ffffff\"><font color=\"#000000\" size=\"5\">Fontgr&#246;&#223;e       Gro&#223;</font></span>    </div>    <div>      <span style=\"background-color: #ffffff\"><font color=\"#000000\">Fontgr&#246;&#223;e       Mittel</font></span>    </div>    <div>      <span style=\"background-color: #ffffff\"><font color=\"#000000\" size=\"3\">Fontgr&#246;&#223;e       Klein</font></span>    </div>    <div>      <span style=\"background-color: #ffffff\"><font color=\"#000000\" size=\"2\">Fontgr&#246;&#223;e       Klein X</font></span>    </div>    <div>      <span style=\"background-color: #ffffff\"><font color=\"#000000\" size=\"1\">Fontgr&#246;&#223;e       Klein XX</font></span>    </div>  </body></html>");
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    addComponentListener(this);
    setVisible(true);
  }

  @Override public void componentResized(ComponentEvent e) {}
  @Override public void componentMoved(ComponentEvent e) {}
  @Override public void componentHidden(ComponentEvent e) {}

  @Override
  public void componentShown(ComponentEvent e) {

  }

  void lines2PDF() {
    try {
      PdfDocument pdf = new PdfDocument(new PdfWriter("sample.pdf"));
      com.itextpdf.layout.Document pdfdocument = new com.itextpdf.layout.Document(pdf);
      HTMLDocument htmldoc = (HTMLDocument) field.getDocument();
      StyleSheet styleSheet = htmldoc.getStyleSheet();
      // Zeichnen der Nummern von Listen: StyleSheet#ListPainter#drawLetter
      // Zeichnen der Bullets von Listen: StyleSheet#ListPainter#drawShape
      // Erkenntnisse:
      // Jede Liste in einer JEditorPane hat eine eigene ListView
      // Es werden nicht immer alle ListViews neu gezeichnet. Wenn z.B. der Cursor in einer Liste steht,
      //   wird die entsprechende ListView regelmäßig wegen des Cursor-Blinkens aktualisiert. Die anderen
      //   ListViews des gleichen JEditPanes aber nicht.

      java.util.List<TextlineDimension> lines = scanLineDimensions();
      for (TextlineDimension line: lines) {
        System.out.println("Zeile bis " + line.getDocIndexTo() + ", Höhe " + line.getHeight() + ", y = " + line.getY());

        String lineHtml = line.extractLineHtml(field);
        lineHtml = removeLinebreakingElementsFromHtmlLine(lineHtml);
        java.util.List<IElement> elements = HtmlConverter.convertToElements(lineHtml);

        Paragraph p = new Paragraph()
          .setFixedPosition((float)line.getX(), 500 - (float)line.getY() - (float)line.getHeight(), 300.0f);
        p.add((IBlockElement)elements.get(0));
        pdfdocument.add(p);

        Integer liIndex = LIRecordingListView.isLILine(htmldoc, line.getY());
        if (liIndex != null) {
          p = new Paragraph()
            .setFixedPosition((float)line.getX() - 10, 500 - (float)line.getY() - (float)line.getHeight(), 300.0f);
          p.add(liIndex.toString());
          pdfdocument.add(p);
        }

      }

      pdfdocument.close();
      Desktop desktop = Desktop.getDesktop();
      desktop.open(new java.io.File("sample.pdf"));
    }
    catch(Exception x) {
      x.printStackTrace();
    }
  }

  private java.util.List<TextlineDimension> scanLineDimensions() throws BadLocationException {
    List<TextlineDimension> lines = new ArrayList<>();
    javax.swing.text.Document document = field.getDocument();
    // At index 0 there is always a zero-sized thing the purpose of which is unclear.
    // It is useless for splitting the text into lines, so we skip it and start at index 1
    for (int rowStart = 1; rowStart < document.getLength(); rowStart++) {
      Rectangle2D lineSpace = field.modelToView2D(rowStart);
      int rowEnd = Utilities.getRowEnd(field, rowStart);
      lines.add(new TextlineDimension(rowStart, rowEnd, lineSpace));
      rowStart = rowEnd;
    }
    return lines;
  }

  private String removeLinebreakingElementsFromHtmlLine(String subHtml) {
    return subHtml
      .replace("<li>", "")
      .replace("</li>", "")
      .replace("<ol>", "")
      .replace("</ol>", "")
      .replace("<ul>", "")
      .replace("</ul>", "")
      .replace("<br>", "");
  }
}
