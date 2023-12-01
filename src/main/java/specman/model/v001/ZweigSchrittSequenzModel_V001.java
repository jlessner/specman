package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;

public class ZweigSchrittSequenzModel_V001 extends SchrittSequenzModel_V001 {
	public final EditorContentModel_V001 ueberschrift;

	@Deprecated public ZweigSchrittSequenzModel_V001() { // For Jackson only
		ueberschrift = null;
	}

	public ZweigSchrittSequenzModel_V001(
		SchrittID id,
		Aenderungsart aenderungsart,
		boolean catchBloeckeZugeklappt,
		EditorContentModel_V001 ueberschrift) {
		super(id, aenderungsart, catchBloeckeZugeklappt);
		this.ueberschrift = ueberschrift;
	}
}
