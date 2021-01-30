package specman.model.v001;

import specman.SchrittID;
import specman.view.RoundedBorderDecorationStyle;

import java.util.ArrayList;
import java.util.List;

public class CaseSchrittModel_V001 extends StrukturierterSchrittModel_V001 {
	public final ZweigSchrittSequenzModel_V001 sonstSequenz;
	public final List<ZweigSchrittSequenzModel_V001> caseSequenzen;
	public final ArrayList<Float> spaltenbreitenAnteile;

	public CaseSchrittModel_V001(
			SchrittID id,
			TextMitAenderungsmarkierungen_V001 inhalt,
			int farbe,
			RoundedBorderDecorationStyle decorationStyle,
			boolean zugeklappt,
			ZweigSchrittSequenzModel_V001 sonstSequenz,
			ArrayList<Float> spaltenbreitenAnteile) {
		super(id, inhalt, farbe, decorationStyle, zugeklappt);
		this.sonstSequenz = sonstSequenz;
		this.caseSequenzen = new ArrayList<ZweigSchrittSequenzModel_V001>();
		this.spaltenbreitenAnteile = spaltenbreitenAnteile;
	}

	@Deprecated public CaseSchrittModel_V001() { // For Jackson only
		sonstSequenz = null;
		caseSequenzen = null;
		spaltenbreitenAnteile = null;
	}

	public void caseHinzufuegen(ZweigSchrittSequenzModel_V001 sequenz) {
		caseSequenzen.add(sequenz);
	}

}
