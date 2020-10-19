package specman.model.v001;

import specman.SchrittID;

public abstract class AbstractSchrittModel_V001 {
	public SchrittID id;
	public TextMitAenderungsmarkierungen_V001 inhalt;
	public int farbe;

	@Deprecated AbstractSchrittModel_V001() {} // For Jackson only

	AbstractSchrittModel_V001(SchrittID id, TextMitAenderungsmarkierungen_V001 inhalt, int farbe) {
		this.id = id;
		this.inhalt = inhalt;
		this.farbe = farbe;

	}
}
