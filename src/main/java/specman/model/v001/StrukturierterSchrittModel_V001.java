package specman.model.v001;

import specman.SchrittID;
import specman.view.RoundedBorderDecorationStyle;

public class StrukturierterSchrittModel_V001 extends AbstractSchrittModel_V001 {
	public final boolean zugeklappt;

	@Deprecated public StrukturierterSchrittModel_V001() { // For Jackson only
		zugeklappt = false;
	}

	public StrukturierterSchrittModel_V001(
			SchrittID id,
			TextMitAenderungsmarkierungen_V001 inhalt,
			int farbe,
			RoundedBorderDecorationStyle decorationStyle,
			boolean zugeklappt) {
		super(id, inhalt, farbe, decorationStyle);
		this.zugeklappt = zugeklappt;
	}
}
