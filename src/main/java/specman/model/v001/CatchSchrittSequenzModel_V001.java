package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;

import java.util.ArrayList;
import java.util.List;

public class CatchSchrittSequenzModel_V001 extends ZweigSchrittSequenzModel_V001 {
  public final List<CoCatchModel_V001> coCatches;
  public final Integer headingRightBarWidth;

  public CatchSchrittSequenzModel_V001() {
    this.coCatches = null;
    this.headingRightBarWidth = null;
  }

  public CatchSchrittSequenzModel_V001(SchrittID breakSchrittId, Aenderungsart aenderungsart, EditorContentModel_V001 ueberschrift, List<CoCatchModel_V001> coCatches, Integer headingRightBarWidth) {
    super(breakSchrittId, aenderungsart, null, ueberschrift);
    this.coCatches = coCatches;
    this.headingRightBarWidth = headingRightBarWidth;
  }
}
