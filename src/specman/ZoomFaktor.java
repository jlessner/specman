package specman;

public enum ZoomFaktor {
	Faktor_50(50),
	Faktor_100(100),
	Faktor_120(120),
	Faktor_150(150),
	Faktor_200(200);
	
	private final int prozent;
	
	ZoomFaktor(int prozent) {
		this.prozent = prozent;
	}

	@Override
	public String toString() {
		return prozent + "%";
	}
	
	public int getProzent() {
		return prozent;
	}
}
