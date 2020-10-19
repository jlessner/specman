package specman.model.v001;

public class Aenderungsmarkierung_V001 {
	final int von, bis;
	
	@Deprecated public Aenderungsmarkierung_V001() { // For Jackson only
		von = bis = 0;
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
