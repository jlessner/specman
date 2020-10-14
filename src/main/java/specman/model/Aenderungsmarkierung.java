package specman.model;

public class Aenderungsmarkierung {
	int von, bis;
	
	public Aenderungsmarkierung() {
	}
	
	public Aenderungsmarkierung(int von, int bis) {
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
