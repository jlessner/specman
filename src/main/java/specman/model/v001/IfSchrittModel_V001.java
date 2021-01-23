package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;
import specman.Specman;

public class IfSchrittModel_V001 extends StrukturierterSchrittModel_V001 {
	public final ZweigSchrittSequenzModel_V001 ifSequenz;
	public final int leerBreite;

	@Deprecated public IfSchrittModel_V001() { // For Jackson only
		this.ifSequenz = new ZweigSchrittSequenzModel_V001();
//		this.leerBreite = 0;
		this.leerBreite = 20*Specman.instance().getZoomFactor()/100; /**@author PVN */
	}

	public IfSchrittModel_V001(
		SchrittID id,
		TextMitAenderungsmarkierungen_V001 inhalt,
		int farbe,
		boolean zugeklappt,
		Aenderungsart aenderungsart,
		ZweigSchrittSequenzModel_V001 ifSequenz,
		int leerBreite) {
		super(id, inhalt, farbe, aenderungsart, zugeklappt);
		this.ifSequenz = ifSequenz;
//		this.leerBreite = leerBreite;
		this.leerBreite = 20*Specman.instance().getZoomFactor()/100; /**@author PVN */
	}
}
