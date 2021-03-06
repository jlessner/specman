package specman.model.v001;

import specman.SchrittID;
import specman.view.RoundedBorderDecorationStyle;

public class WhileWhileSchrittModel_V001 extends WhileSchrittModel_V001 {

    @Deprecated public WhileWhileSchrittModel_V001() {} // For Jackson only

    public WhileWhileSchrittModel_V001(
        SchrittID id,
        TextMitAenderungsmarkierungen_V001 inhalt,
        int farbe,
        RoundedBorderDecorationStyle decorationStyle,
        boolean zugeklappt,
        SchrittSequenzModel_V001 wiederholSequenz,
        int balkenbreite) {
        super(id, inhalt, farbe, decorationStyle, zugeklappt, wiederholSequenz, balkenbreite);
    }
}
