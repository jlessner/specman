package specman.model.v001;

import specman.SchrittID;

public class CoCatchModel_V001 {
  public final SchrittID breakStepId;
  public final EditorContentModel_V001 heading;

  public CoCatchModel_V001() {
    this.breakStepId = null;
    this.heading = null;
  }

  public CoCatchModel_V001(SchrittID breakStepId, EditorContentModel_V001 heading) {
    this.breakStepId = breakStepId;
    this.heading = heading;
  }
}
