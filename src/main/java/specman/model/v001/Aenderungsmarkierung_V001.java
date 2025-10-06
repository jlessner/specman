package specman.model.v001;

import java.util.Objects;

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
		return bis - von + 1;
	}

	public Aenderungsmarkierung_V001 shiftright() { return new Aenderungsmarkierung_V001(von+1, bis+1); }

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Aenderungsmarkierung_V001 that = (Aenderungsmarkierung_V001) o;
		return von == that.von && bis == that.bis;
	}

	@Override
	public int hashCode() {
		return Objects.hash(von, bis);
	}

	@Override
	public String toString() {
		return von + ".." + bis;
	}
}
