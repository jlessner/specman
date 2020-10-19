package specman.model.v001;

import specman.SchrittID;

public class BreakSchrittModel_V001 extends AbstractSchrittModel_V001 {

    @Deprecated public BreakSchrittModel_V001() {} // For Jackson only

    public BreakSchrittModel_V001(SchrittID id, TextMitAenderungsmarkierungen_V001 inhalt, int farbe) {
        super(id, inhalt, farbe);
    }
}
