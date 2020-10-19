package specman.model.v001;

import specman.SchrittID;

public class CatchSchrittModel_V001 extends StrukturierterSchrittModel_V001 {
	public SchrittSequenzModel_V001 handlingSequenz;
	public boolean breakAngekoppelt;

	@Deprecated public CatchSchrittModel_V001() {} // For Jackson only

	public CatchSchrittModel_V001(
		SchrittID id,
		TextMitAenderungsmarkierungen_V001 inhalt,
		int farbe,
		boolean zugeklappt,
		SchrittSequenzModel_V001 handlingSequenz,
		boolean breakAngekoppelt) {
		super(id, inhalt, farbe, zugeklappt);
		this.handlingSequenz = handlingSequenz;
		this.breakAngekoppelt = breakAngekoppelt;
	}
}
