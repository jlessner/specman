package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;

public class QuellSchrittModel_V001 extends EinfacherSchrittModel_V001{

    public final SchrittID referenzId;

    public QuellSchrittModel_V001(SchrittID id, TextMitAenderungsmarkierungen_V001 inhalt, int farbe, Aenderungsart aenderungsart, SchrittID referenzID) {
        super(id, inhalt, farbe, aenderungsart);
        this.referenzId = referenzID;
    }
}
