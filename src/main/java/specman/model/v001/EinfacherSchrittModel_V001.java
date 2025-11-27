package specman.model.v001;

import specman.Aenderungsart;
import specman.StepID;
import specman.view.RoundedBorderDecorationStyle;

public class EinfacherSchrittModel_V001 extends AbstractSchrittModel_V001 {

    @Deprecated public EinfacherSchrittModel_V001() {} // For Jackson only

    public EinfacherSchrittModel_V001(
        StepID id,
        EditorContentModel_V001 inhalt,
        int farbe,
        Aenderungsart aenderungsart,
        StepID quellschrittID,
        RoundedBorderDecorationStyle decorationStyle) {
        super(id, inhalt, farbe, aenderungsart, quellschrittID, decorationStyle);
    }
}
