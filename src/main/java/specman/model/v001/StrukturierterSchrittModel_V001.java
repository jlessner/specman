package specman.model.v001;

import specman.SchrittID;

public class StrukturierterSchrittModel_V001 extends AbstractSchrittModel_V001 {
	public final boolean zugeklappt;

	@Deprecated public StrukturierterSchrittModel_V001() { // For Jackson only
		zugeklappt = false;
	}

	public StrukturierterSchrittModel_V001(SchrittID id, TextMitAenderungsmarkierungen_V001 inhalt, int farbe, boolean zugeklappt) {
		super(id, inhalt, farbe);
		this.zugeklappt = zugeklappt;
	}
}
