package specman.model.v001;

public class Aenderungsmarkierung_V001 {
	int von, bis;
	
	public Aenderungsmarkierung_V001() {
	}
	
	public Aenderungsmarkierung_V001(int von, int bis) {
		this.von = von;
		this.bis = bis;
	}

	public int getVon() {
		return von;
	}

	public int getBis() {
		return bis;
	}

	public int laenge() {
		return bis - von;
	}

	
}
