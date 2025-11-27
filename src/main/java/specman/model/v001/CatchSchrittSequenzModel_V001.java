package specman.model.v001;

import specman.Aenderungsart;
import specman.StepID;

public class CatchSchrittSequenzModel_V001 extends ZweigSchrittSequenzModel_V001 {

  public CatchSchrittSequenzModel_V001() {
  }

  public CatchSchrittSequenzModel_V001(StepID breakStepId, Aenderungsart aenderungsart, EditorContentModel_V001 ueberschrift) {
    super(breakStepId, aenderungsart, null, ueberschrift);
  }
}
