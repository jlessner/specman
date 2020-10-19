package specman.model.v001;

import specman.SchrittID;

public abstract class AbstractSchrittModel_V001 {
	public final SchrittID id;
	public final TextMitAenderungsmarkierungen_V001 inhalt;
	public final int farbe;

	@Deprecated AbstractSchrittModel_V001() { // For Jackson only
		this(null, null, 0);
	}

	AbstractSchrittModel_V001(SchrittID id, TextMitAenderungsmarkierungen_V001 inhalt, int farbe) {
		this.id = id;
		this.inhalt = inhalt;
		this.farbe = farbe;

	}
}
