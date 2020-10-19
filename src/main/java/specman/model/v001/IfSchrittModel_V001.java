package specman.model.v001;

import specman.SchrittID;

public class IfSchrittModel_V001 extends StrukturierterSchrittModel_V001 {
	public final ZweigSchrittSequenzModel_V001 ifSequenz;
	public final int leerBreite;

	@Deprecated public IfSchrittModel_V001() { // For Jackson only
		this.ifSequenz = new ZweigSchrittSequenzModel_V001();
		this.leerBreite = 0;
	}

	public IfSchrittModel_V001(
		SchrittID id,
		TextMitAenderungsmarkierungen_V001 inhalt,
		int farbe,
		boolean zugeklappt,
		ZweigSchrittSequenzModel_V001 ifSequenz,
		int leerBreite) {
		super(id, inhalt, farbe, zugeklappt);
		this.ifSequenz = ifSequenz;
		this.leerBreite = leerBreite;
	}
}
