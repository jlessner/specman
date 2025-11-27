package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;

public class CatchSchrittSequenzModel_V001 extends ZweigSchrittSequenzModel_V001 {

  public CatchSchrittSequenzModel_V001() {
  }

  public CatchSchrittSequenzModel_V001(SchrittID breakSchrittId, Aenderungsart aenderungsart, EditorContentModel_V001 ueberschrift) {
    super(breakSchrittId, aenderungsart, null, ueberschrift);
  }
}
