package specman.model.v001;

import specman.Aenderungsart;
import specman.StepID;
import specman.view.RoundedBorderDecorationStyle;

public class BreakSchrittModel_V001 extends AbstractSchrittModel_V001 {

    @Deprecated public BreakSchrittModel_V001() {} // For Jackson only

    public BreakSchrittModel_V001(
        StepID id,
        EditorContentModel_V001 inhalt,
        int farbe,
        Aenderungsart aenderungsart,
        StepID quellschrittID,
        RoundedBorderDecorationStyle decorationStyle) {
        super(id, inhalt, farbe, aenderungsart, quellschrittID, decorationStyle);
    }
}
