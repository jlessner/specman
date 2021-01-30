package specman.model.v001;

import specman.SchrittID;
import specman.view.RoundedBorderDecorationStyle;

import static specman.view.RoundedBorderDecorationStyle.None;

public abstract class AbstractSchrittModel_V001 {
	public final SchrittID id;
	public final TextMitAenderungsmarkierungen_V001 inhalt;
	public final int farbe;
	public final RoundedBorderDecorationStyle decorationStyle;

	@Deprecated AbstractSchrittModel_V001() { // For Jackson only
		this(null, null, 0, None);
	}

	AbstractSchrittModel_V001(
			SchrittID id,
			TextMitAenderungsmarkierungen_V001 inhalt,
			int farbe,
			RoundedBorderDecorationStyle decorationStyle) {
		this.id = id;
		this.inhalt = inhalt;
		this.farbe = farbe;
		this.decorationStyle = decorationStyle;
	}
}
