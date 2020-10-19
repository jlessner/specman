package specman.model.v001;

import specman.SchrittID;

public class SubsequenzSchrittModel_V001 extends StrukturierterSchrittModel_V001 {
	public final SchrittSequenzModel_V001 subsequenz;
	
	@Deprecated public SubsequenzSchrittModel_V001() { // For Jackson only
		subsequenz = null;
	}

	public SubsequenzSchrittModel_V001(
		SchrittID id,
		TextMitAenderungsmarkierungen_V001 inhalt,
		int farbe,
		boolean zugeklappt,
		SchrittSequenzModel_V001 subsequenz) {
		super(id, inhalt, farbe, zugeklappt);
		this.subsequenz = subsequenz;
	}
}
