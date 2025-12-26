package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;

public class CoCatchModel_V001 {
  public final SchrittID breakStepId;
  public final EditorContentModel_V001 heading;
  public final Aenderungsart changetype;

  public CoCatchModel_V001() {
    this.breakStepId = null;
    this.heading = null;
    this.changetype = null;
  }

  public CoCatchModel_V001(SchrittID breakStepId, EditorContentModel_V001 heading, Aenderungsart changetype) {
    this.breakStepId = breakStepId;
    this.heading = heading;
    this.changetype = changetype;
  }
}
