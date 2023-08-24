package specman.pdf;

public class ShapeText {
  private String content;
  private int fontsize;
  private int leftMargin;

  public ShapeText(String content, int fontsize, int leftMargin) {
    this.content = content;
    this.fontsize = fontsize;
    this.leftMargin = leftMargin;
  }

  public String getContent() {
    return content;
  }

  public int getFontsize() {
    return fontsize;
  }

  public int getLeftMargin() {
    return leftMargin;
  }
}
