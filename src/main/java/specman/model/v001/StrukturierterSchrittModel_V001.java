package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;
import specman.view.RoundedBorderDecorationStyle;

public class StrukturierterSchrittModel_V001 extends AbstractSchrittModel_V001 {
	public final boolean zugeklappt;

	@Deprecated public StrukturierterSchrittModel_V001() { // For Jackson only
		zugeklappt = false;
	}

	public StrukturierterSchrittModel_V001(
			SchrittID id,
			EditorContentModel_V001 inhalt,
			int farbe,
			Aenderungsart aenderungsart,
			boolean zugeklappt,
			SchrittID quellschrittID,
			RoundedBorderDecorationStyle decorationStyle) {
		super(id, inhalt, farbe, aenderungsart, quellschrittID, decorationStyle);
		this.zugeklappt = zugeklappt;
	}
}
