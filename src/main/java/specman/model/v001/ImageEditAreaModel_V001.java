package specman.model.v001;

public class ImageEditAreaModel_V001 extends AbstractEditAreaModel_V001 {
  public final byte[] imageData;
  public final String imageType;

  public ImageEditAreaModel_V001() { // For Jackson only
    this.imageData = null;
    this.imageType = null;
  }

  public ImageEditAreaModel_V001(byte[] imageData, String imageType) {
    this.imageData = imageData;
    this.imageType = imageType;
  }
}
