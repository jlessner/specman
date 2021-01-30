package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;
import specman.view.RoundedBorderDecorationStyle;

public class BreakSchrittModel_V001 extends AbstractSchrittModel_V001 {

    @Deprecated public BreakSchrittModel_V001() {} // For Jackson only

    public BreakSchrittModel_V001(
        SchrittID id,
        TextMitAenderungsmarkierungen_V001 inhalt,
        int farbe,
        Aenderungsart aenderungsart,
        SchrittID quellschrittID,
        RoundedBorderDecorationStyle decorationStyle) {
        super(id, inhalt, farbe, aenderungsart, quellschrittID, decorationStyle);
    }
}
