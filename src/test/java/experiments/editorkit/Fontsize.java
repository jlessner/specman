package experiments.editorkit;

public class Fontsize {
  public static final float SWING_FONTSIZE = 15;

  private int size;
  private final String name;
  private int readSize;

  public Fontsize(int size, String name) {
    this.size = size;
    this.name = name;
  }

  @Override
  public String toString() { return name; }

  public int getSize() {
    return size;
  }

  public int getReadSize() {
    return readSize;
  }

  public void setReadSize(int readSize) {
    this.readSize = readSize;
  }
}
