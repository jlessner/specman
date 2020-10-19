package specman.model.v001;

import specman.SchrittID;

public class ZweigSchrittSequenzModel_V001 extends SchrittSequenzModel_V001 {
	public TextMitAenderungsmarkierungen_V001 ueberschrift;

	@Deprecated public ZweigSchrittSequenzModel_V001() {} // For Jackson only

	public ZweigSchrittSequenzModel_V001(
		SchrittID id,
		boolean catchBloeckeZugeklappt,
		int catchBloeckeUmgehungBreite,
		TextMitAenderungsmarkierungen_V001 ueberschrift) {
		super(id, catchBloeckeZugeklappt, catchBloeckeUmgehungBreite);
		this.ueberschrift = ueberschrift;
	}
}
