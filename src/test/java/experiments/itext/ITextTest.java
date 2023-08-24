package experiments.itext;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfTextAnnotation;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static com.itextpdf.kernel.pdf.PdfName.BaseFont;

/**
 *  Simple examples taken from https://www.tutorialspoint.com/itext/itext_drawing_line.htm
 */
public class ITextTest {

  @Test
  void testCreateEmptyDocument() throws Exception {
    String dest = "sample.pdf";
    PdfWriter writer = new PdfWriter(dest);
    PdfDocument pdfDoc = new PdfDocument(writer);
    pdfDoc.addNewPage();
    Document document = new Document(pdfDoc);
    document.close();
    System.out.println("PDF Created");
  }

  @Test
  void testDrawLine() throws Exception {
    String dest = "sample.pdf";
    PdfWriter writer = new PdfWriter(dest);
    PdfDocument pdfDoc = new PdfDocument(writer);
    PdfPage page = pdfDoc.addNewPage();
    Document document = new Document(pdfDoc);

    PdfCanvas canvas = new PdfCanvas(page);
    canvas.moveTo(100, 300);
    canvas.lineTo(500, 300);
    canvas.closePathStroke();

    document.close();
    System.out.println("PDF Created");
  }

  @Test
  void testDrawTextAndLine() throws Exception {
    String dest = "sample.pdf";
    PdfWriter writer = new PdfWriter(dest);
    PdfDocument pdf = new PdfDocument(writer);
    PdfPage page = pdf.addNewPage();
    PdfCanvas pdfCanvas = new PdfCanvas(page);


    pdfCanvas.moveTo(10, 30);
    pdfCanvas.lineTo(500, 300);
    pdfCanvas.closePathStroke();

    Rectangle rectangle = new Rectangle(2, 2, 100, 100);
    pdfCanvas.rectangle(rectangle);
    pdfCanvas.stroke();
    Canvas canvas = new Canvas(pdfCanvas, pdf, rectangle);
    PdfFont font = PdfFontFactory.createFont(FontConstants.TIMES_ROMAN);
    PdfFont bold = PdfFontFactory.createFont(FontConstants.TIMES_BOLD);
    Text title = new Text("The Strange Case of Dr. Jekyll and Mr. Hyde").setFont(bold).setFontSize(10.0f);
    Text author = new Text("Robert Louis Stevenson").setFont(font);
    Paragraph p = new Paragraph().setBackgroundColor(Color.ORANGE).add(title).add(" by ").add(author);
    canvas.add(p);
    canvas.close();

    pdf.close();
    System.out.println("PDF Created");
  }

  @Test
  void testParagraphs() throws Exception {
    String dest = "sample.pdf";
    PdfWriter writer = new PdfWriter(dest);

    PdfDocument pdf = new PdfDocument(writer);

    Document document = new Document(pdf);
    String para1 = "Tutorials Point originated from the idea that there exists " +
      "a class of readers who respond better to online content and prefer to learn " +
      "new skills at their own pace from the comforts of their drawing rooms.";

    String para2 = "The journey commenced with a single tutorial on HTML in 2006 " +
      "and elated by the response it generated, we worked our way to adding fresh " +
      "tutorials to our repository which now proudly flaunts a wealth of tutorials " +
      "and allied articles on topics ranging from programming languages to web designing " +
      "to academics and much more.";

    Paragraph paragraph1 = new Paragraph(para1);
    Paragraph paragraph2 = new Paragraph(para2);

    document.add(paragraph1);
    document.add(paragraph2);

    document.close();
    System.out.println("Paragraph added");
  }

  @Test
  void testTextAnnotation() throws Exception {
    String dest = "sample.pdf";
    PdfWriter writer = new PdfWriter(dest);
    PdfDocument pdf = new PdfDocument(writer);
    Document document = new Document(pdf);

    // Creating PdfTextAnnotation object
    Rectangle rect = new Rectangle(20, 800, 0, 0);
    PdfAnnotation ann = new PdfTextAnnotation(rect);
    ann.setColor(Color.GREEN);
    ann.setTitle(new PdfString("Hello"));
    ann.setContents("Hi welcome to Tutorialspoint.");

    PdfPage page =  pdf.addNewPage();
    page.addAnnotation(ann);

    document.close();

    System.out.println("Annotation added successfully");
  }

  @Test
  void testShowText() throws Exception {
    String dest = "sample.pdf";
    PdfWriter writer = new PdfWriter(dest);
    PdfDocument pdf = new PdfDocument(writer);
    Document document = new Document(pdf);

    PdfFont labelFont = PdfFontFactory.createFont(FontConstants.HELVETICA);

    PdfPage page =  pdf.addNewPage();
    PdfCanvas pdfCanvas = new PdfCanvas(page);
    pdfCanvas.setFillColor(Color.BLUE);

    pdfCanvas.beginText().setFontAndSize(labelFont, 7)
      .moveText(20, 800)
      .showText("Hello World!")
      .endText();

    document.close();
    pdf.close();

    Desktop desktop = Desktop.getDesktop();
    desktop.open(new java.io.File("sample.pdf"));
  }

  @Test
  void testSpecmanLookAlike() throws Exception {
    PdfDocument pdf = new PdfDocument(new PdfWriter("sample.pdf"));
    Document document = new Document(pdf);
    PdfPage page =  pdf.addNewPage();

    PdfCanvas pdfCanvas = new PdfCanvas(page);
    PdfFont textFont = PdfFontFactory.createFont(FontConstants.TIMES_ROMAN);
    PdfFont labelFont = PdfFontFactory.createFont(FontConstants.HELVETICA);
    Border textLeftBorder = new SolidBorder(Color.WHITE, 3, 0);
    Border textUpperBorder = new SolidBorder(Color.WHITE, 2, 0);

    Rectangle rectangle = new Rectangle(50, 800, 300, 20);
    pdfCanvas.setLineWidth(1).rectangle(rectangle).stroke();
    Canvas canvas = new Canvas(pdfCanvas, pdf, rectangle);
    Text title = new Text("Neuer Schritt 1")
      .setBorderLeft(textLeftBorder)
      .setBorderTop(textUpperBorder)
      .setFont(textFont)
      .setFontSize(10.0f);
    Paragraph p = new Paragraph().add(title);
    canvas.add(p);
    canvas.close();

    rectangle = new Rectangle(342.5f, 809.5f, 7, 10);
    pdfCanvas.setLineWidth(1).rectangle(rectangle).setFillColor(Color.LIGHT_GRAY).fill();
    document.setFont(labelFont).setFontColor(Color.WHITE).setFontSize(8).showTextAligned("1", 345.5f, 810, TextAlignment.CENTER);

    rectangle = new Rectangle(50, 780, 150, 20);
    pdfCanvas.setLineWidth(1).rectangle(rectangle).stroke();

    rectangle = new Rectangle(200, 780, 150, 20);
    pdfCanvas.setLineWidth(1).rectangle(rectangle).stroke();
    canvas = new Canvas(pdfCanvas, pdf, rectangle).setTextAlignment(TextAlignment.CENTER);
    title = new Text("Neue Bedingung 2")
      .setBorderLeft(textLeftBorder)
      .setBorderTop(textUpperBorder)
      .setFont(textFont)
      .setFontColor(Color.BLACK)
      .setFontSize(10.0f);
    p = new Paragraph().add(title);
    canvas.add(p);
    canvas.close();

    rectangle = new Rectangle(50, 760, 150, 20);
    pdfCanvas.setLineWidth(1).rectangle(rectangle).stroke();
    canvas = new Canvas(pdfCanvas, pdf, rectangle);
    title = new Text("Ja")
      .setBorderLeft(textLeftBorder)
      .setBorderTop(textUpperBorder)
      .setFont(textFont)
      .setFontColor(Color.BLACK)
      .setFontSize(10.0f);
    p = new Paragraph().add(title);
    canvas.add(p);
    canvas.close();

    rectangle = new Rectangle(200, 760, 150, 20);
    pdfCanvas.setLineWidth(1).rectangle(rectangle).stroke();
    canvas = new Canvas(pdfCanvas, pdf, rectangle).setTextAlignment(TextAlignment.RIGHT);
    title = new Text("Nein")
      .setBorderRight(textLeftBorder)
      .setBorderTop(textUpperBorder)
      .setFont(textFont)
      .setFontColor(Color.BLACK)
      .setFontSize(10.0f);
    p = new Paragraph().add(title);
    canvas.add(p);
    canvas.close();

    pdfCanvas.setFillColor(Color.WHITE);
    pdfCanvas.moveTo(200, 795).lineTo(215, 780).lineTo(200, 765).lineTo(185, 780).lineTo(200, 795).fill();
    pdfCanvas.moveTo(200, 795).lineTo(215, 780).lineTo(200, 765).lineTo(185, 780).lineTo(200, 795).stroke();

    rectangle = new Rectangle(342.5f, 789.5f, 7, 10);
    pdfCanvas.setLineWidth(1).rectangle(rectangle).setFillColor(Color.LIGHT_GRAY).fill();
    document.setFont(labelFont).setFontColor(Color.WHITE).setFontSize(8).showTextAligned("2", 345.5f, 790, TextAlignment.CENTER);

    rectangle = new Rectangle(50, 740, 150, 20);
    pdfCanvas.setLineWidth(1).rectangle(rectangle).stroke();
    canvas = new Canvas(pdfCanvas, pdf, rectangle);
    title = new Text("Neuer Schritt 1")
      .setBorderLeft(textLeftBorder)
      .setBorderTop(textUpperBorder)
      .setFont(textFont)
      .setFontColor(Color.BLACK)
      .setFontSize(10.0f);
    p = new Paragraph().add(title);
    canvas.add(p);
    canvas.close();

    rectangle = new Rectangle(200, 740, 150, 20);
    pdfCanvas.setLineWidth(1).rectangle(rectangle).stroke();
    canvas = new Canvas(pdfCanvas, pdf, rectangle);
    title = new Text("Neuer Schritt 1")
      .setBorderLeft(textLeftBorder)
      .setBorderTop(textUpperBorder)
      .setFont(textFont)
      .setFontColor(Color.BLACK)
      .setFontSize(10.0f);
    p = new Paragraph().add(title);
    canvas.add(p);
    canvas.close();

    rectangle = new Rectangle(334.5f, 749.5f, 15, 10);
    pdfCanvas.setLineWidth(1).rectangle(rectangle).setFillColor(Color.LIGHT_GRAY).fill();
    document.setFont(labelFont).setFontColor(Color.WHITE).setFontSize(8).showTextAligned("3.1", 342.5f, 750, TextAlignment.CENTER);

    rectangle = new Rectangle(184.5f, 749.5f, 15, 10);
    pdfCanvas.setLineWidth(1).rectangle(rectangle).setFillColor(Color.LIGHT_GRAY).fill();
    document.setFont(labelFont).setFontColor(Color.WHITE).setFontSize(8).showTextAligned("2.1", 192.5f, 750, TextAlignment.CENTER);

    pdf.close();
    document.close();

    Desktop desktop = Desktop.getDesktop();
    desktop.open(new java.io.File("sample.pdf"));
  }
}
