package specman.editarea.litrack;

public class LIRecord {
  private final int yPosition;
  private final int liIndex;

  public LIRecord(int yPosition, int liIndex) {
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
