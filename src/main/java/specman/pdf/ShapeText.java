package specman.pdf;

import com.itextpdf.kernel.font.PdfFont;

import java.awt.*;

public class ShapeText {
  private String content;
  private Insets insets;
  private Color color;
  private Font font;

  public ShapeText(String content, Insets insets, Color color, Font font) {
    this.content = content;
    this.insets = insets;
    this.color = color;
    this.font = font;
  }

  public String getContent() {
    return content;
  }

  public int getFontsize() {
    return font.getSize();
  }

  public Insets getInsets() {
    return insets;
  }

  public com.itextpdf.kernel.color.Color getPDFColor() {
    return Shape.toPDFColor(color, Shape.DEFAULT_LINE_COLOR);
  }

  public Font getFont() {
    return font;
  }
}
