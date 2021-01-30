package specman.model.v001;

import specman.SchrittID;
import specman.view.RoundedBorderDecorationStyle;

public class IfElseSchrittModel_V001 extends StrukturierterSchrittModel_V001 {
	public final ZweigSchrittSequenzModel_V001 ifSequenz;
	public final ZweigSchrittSequenzModel_V001 elseSequenz;
	public final float ifBreitenanteil;

	@Deprecated public IfElseSchrittModel_V001() {  // For Jackson only
		this.ifSequenz = new ZweigSchrittSequenzModel_V001();
		this.elseSequenz = new ZweigSchrittSequenzModel_V001();
		this.ifBreitenanteil = 0.0f;
	}

	public IfElseSchrittModel_V001(
		SchrittID id,
		TextMitAenderungsmarkierungen_V001 inhalt,
		int farbe,
		RoundedBorderDecorationStyle decorationStyle,
		boolean zugeklappt,
		ZweigSchrittSequenzModel_V001 ifSequenz,
		ZweigSchrittSequenzModel_V001 elseSequenz,
		float ifBreitenanteil) {
		super(id, inhalt, farbe, decorationStyle, zugeklappt);
		this.ifSequenz = ifSequenz;
		this.elseSequenz = elseSequenz;
		this.ifBreitenanteil = ifBreitenanteil;
	}
}
