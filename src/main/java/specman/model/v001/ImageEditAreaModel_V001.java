package specman.model.v001;

import specman.Aenderungsart;

public class ImageEditAreaModel_V001 extends AbstractEditAreaModel_V001 {
  public final byte[] imageData;
  public final String imageType;
  public final Aenderungsart aenderungsart;
  public final float individualScalePercent;

  public ImageEditAreaModel_V001() { // For Jackson only
    this.imageData = null;
    this.imageType = null;
    this.aenderungsart = null;
    this.individualScalePercent = 0;
  }

  public ImageEditAreaModel_V001(byte[] imageData, String imageType, Aenderungsart aenderungsart, float individualScalePercent) {
    this.imageData = imageData;
    this.imageType = imageType;
    this.aenderungsart = aenderungsart;
    this.individualScalePercent = individualScalePercent;
  }
}
