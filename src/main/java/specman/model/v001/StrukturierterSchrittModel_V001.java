package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;

public class StrukturierterSchrittModel_V001 extends AbstractSchrittModel_V001 {
	public final boolean zugeklappt;

	@Deprecated public StrukturierterSchrittModel_V001() { // For Jackson only
		zugeklappt = false;
	}

	public StrukturierterSchrittModel_V001(SchrittID id, TextMitAenderungsmarkierungen_V001 inhalt, int farbe, Aenderungsart aenderungsart, boolean zugeklappt, SchrittID quellschrittID) {
		super(id, inhalt, farbe, aenderungsart, quellschrittID);
		this.zugeklappt = zugeklappt;
	}
}
