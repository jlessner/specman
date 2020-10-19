package specman.model.v001;

import specman.SchrittID;

public class EinfacherSchrittModel_V001 extends AbstractSchrittModel_V001 {

    @Deprecated public EinfacherSchrittModel_V001() {} // For Jackson only

    public EinfacherSchrittModel_V001(SchrittID id, TextMitAenderungsmarkierungen_V001 inhalt, int farbe) {
        super(id, inhalt, farbe);
    }
}
