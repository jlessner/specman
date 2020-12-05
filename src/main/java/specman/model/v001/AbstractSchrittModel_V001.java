package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;

public abstract class AbstractSchrittModel_V001 {
	public final SchrittID id;
	public final TextMitAenderungsmarkierungen_V001 inhalt;
	public final int farbe;
	public final Aenderungsart aenderungsart;

	@Deprecated AbstractSchrittModel_V001() { // For Jackson only
		this(null, null, 0, null);
	}

	AbstractSchrittModel_V001(SchrittID id, TextMitAenderungsmarkierungen_V001 inhalt, int farbe, Aenderungsart aenderungsart) {
		this.id = id;
		this.inhalt = inhalt;
		this.farbe = farbe;
		this.aenderungsart = aenderungsart;

	}
}
