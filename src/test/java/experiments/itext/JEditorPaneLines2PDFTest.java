package experiments.itext;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.element.Paragraph;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import specman.pdf.TextlineDimension;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Utilities;
import java.awt.*;
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
    field.setContentType("text/html");
    pane.add(field, CC.xy(1, 2));
    field.setText("eins<ul><li>zwei zwei zwei zwei zwei zwei zwei<li>drei<li>dreieinhalb<ul><li>drei eins<li>drei zwei</ul></ul>vier<br><font size=\"6\">fünf</font><br>sechs");
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
    lines2PDF();
  }

  void lines2PDF() {
    try {
      PdfDocument pdf = new PdfDocument(new PdfWriter("sample.pdf"));
      com.itextpdf.layout.Document pdfdocument = new com.itextpdf.layout.Document(pdf);

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
      .replace("<div>", "")
      .replace("</div>", "")
      .replace("<br>", "");
  }
}
