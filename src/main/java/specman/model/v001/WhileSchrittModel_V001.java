package specman.model.v001;

import specman.SchrittID;

public class WhileSchrittModel_V001 extends StrukturierterSchrittModel_V001 {
	public SchrittSequenzModel_V001 wiederholSequenz;
	public int balkenbreite;

	@Deprecated
	public WhileSchrittModel_V001() {} // For Jackson only

	public WhileSchrittModel_V001(
		SchrittID id,
		TextMitAenderungsmarkierungen_V001 inhalt,
		int farbe,
		boolean zugeklappt,
		SchrittSequenzModel_V001 wiederholSequenz,
		int balkenbreite) {
		super(id, inhalt, farbe, zugeklappt);
		this.wiederholSequenz = wiederholSequenz;
		this.balkenbreite = balkenbreite;
	}
}
