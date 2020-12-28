package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;

public class WhileWhileSchrittModel_V001 extends WhileSchrittModel_V001 {

    @Deprecated public WhileWhileSchrittModel_V001() {} // For Jackson only

    public WhileWhileSchrittModel_V001(
        SchrittID id,
        TextMitAenderungsmarkierungen_V001 inhalt,
        int farbe,
        Aenderungsart aenderungsart,
        boolean zugeklappt,
        SchrittSequenzModel_V001 wiederholSequenz,
        int balkenbreite) {
        super(id, inhalt, farbe, aenderungsart, zugeklappt, wiederholSequenz, balkenbreite);
    }
}
