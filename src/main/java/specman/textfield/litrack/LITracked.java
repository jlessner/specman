package specman.textfield.litrack;

public class LITracked {
  private final int yPosition;
  private final int liIndex;

  public LITracked(int yPosition, int liIndex) {
    this.yPosition = yPosition;
    this.liIndex = liIndex;
  }


  public float getYPosition() {
    return yPosition;
  }

  public int getLiIndex() {
    return liIndex;
  }
}
