package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;
import specman.view.RoundedBorderDecorationStyle;

public class CatchSchrittModel_V001 extends StrukturierterSchrittModel_V001 {
	public final SchrittSequenzModel_V001 handlingSequenz;
	public final boolean breakAngekoppelt;

	@Deprecated public CatchSchrittModel_V001() { // For Jackson only
		handlingSequenz = null;
		breakAngekoppelt = false;
	}

	public CatchSchrittModel_V001(
		SchrittID id,
		EditorContentModel_V001 inhalt,
		int farbe,
		boolean zugeklappt,
		Aenderungsart aenderungsart,
		SchrittSequenzModel_V001 handlingSequenz,
		boolean breakAngekoppelt,
		SchrittID quellschrittID,
		RoundedBorderDecorationStyle decorationStyle) {
		super(id, inhalt, farbe, aenderungsart, zugeklappt, quellschrittID, decorationStyle);
		this.handlingSequenz = handlingSequenz;
		this.breakAngekoppelt = breakAngekoppelt;
	}
}
