package specman.model.v001;

import specman.SchrittID;
import specman.view.RoundedBorderDecorationStyle;

public class WhileSchrittModel_V001 extends StrukturierterSchrittModel_V001 {
	public final SchrittSequenzModel_V001 wiederholSequenz;
	public final int balkenbreite;

	@Deprecated public WhileSchrittModel_V001() { // For Jackson only
		wiederholSequenz = null;
		balkenbreite = 0;
	}

	public WhileSchrittModel_V001(
		SchrittID id,
		TextMitAenderungsmarkierungen_V001 inhalt,
		int farbe,
		RoundedBorderDecorationStyle decorationStyle,
		boolean zugeklappt,
		SchrittSequenzModel_V001 wiederholSequenz,
		int balkenbreite) {
		super(id, inhalt, farbe, decorationStyle, zugeklappt);
		this.wiederholSequenz = wiederholSequenz;
		this.balkenbreite = balkenbreite;
	}
}
