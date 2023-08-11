package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;
import specman.view.RoundedBorderDecorationStyle;

public class QuellSchrittModel_V001 extends AbstractSchrittModel_V001{

    @Deprecated public QuellSchrittModel_V001() {} // For Jackson only

    public QuellSchrittModel_V001(
        SchrittID id,
        EditorContent_V001 inhalt,
        int farbe,
        Aenderungsart aenderungsart,
        SchrittID zielschrittID,
        RoundedBorderDecorationStyle decorationStyle) {
        super(id, inhalt, farbe, aenderungsart, zielschrittID, decorationStyle);
    }
}
