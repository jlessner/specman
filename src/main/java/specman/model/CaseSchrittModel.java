package specman.model;

import java.util.ArrayList;
import java.util.List;

public class CaseSchrittModel extends StrukturierterSchrittModel {
	public ZweigSchrittSequenzModel sonstSequenz;
	public List<ZweigSchrittSequenzModel> caseSequenzen;
	public ArrayList<Float> spaltenbreitenAnteile;
	
	public CaseSchrittModel() {
		this.sonstSequenz = new ZweigSchrittSequenzModel();
		this.caseSequenzen = new ArrayList<ZweigSchrittSequenzModel>();
	}

	public void caseHinzufuegen(ZweigSchrittSequenzModel sequenz) {
		caseSequenzen.add(sequenz);
	}

}
