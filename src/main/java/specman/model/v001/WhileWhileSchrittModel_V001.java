package specman.model.v001;

import specman.Aenderungsart;
import specman.StepID;
import specman.view.RoundedBorderDecorationStyle;

public class WhileWhileSchrittModel_V001 extends WhileSchrittModel_V001 {

    @Deprecated public WhileWhileSchrittModel_V001() {} // For Jackson only

    public WhileWhileSchrittModel_V001(
        StepID id,
        EditorContentModel_V001 inhalt,
        int farbe,
        Aenderungsart aenderungsart,
        boolean zugeklappt,
        SchrittSequenzModel_V001 wiederholSequenz,
        int balkenbreite,
        StepID quellschrittID,
        RoundedBorderDecorationStyle decorationStyle) {
        super(id, inhalt, farbe, aenderungsart, zugeklappt, wiederholSequenz, balkenbreite, quellschrittID, decorationStyle);
    }
}
