package specman;

import java.util.ArrayList;
import java.util.List;

/**
 * Die ID eines Schrittes ist aus mehreren laufenden Nummern aufgebaut, die der üblichen Notation wie 1.19.7 entsprechen
 */
public class SchrittID {
	public final List<Integer> nummern = new ArrayList<Integer>();

	public SchrittID() {} // For Jackson only

	public SchrittID(Integer... nummern) {
		for (int nummer: nummern) {
			this.nummern.add(nummer);
		}
	}
	
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		for (Integer nummer: nummern) {
			b.append(nummer);
			b.append('.');
		}
		return b.substring(0, b.length() - 1);
	}

	public SchrittID naechsteID() {
		Integer[] naechsteNummern = nummern.toArray(new Integer[0]);
		naechsteNummern[naechsteNummern.length - 1]++;
		return new SchrittID(naechsteNummern);
	}

	public SchrittID vorhergehendeID() {
		Integer[] vorhergehendeNummern = nummern.toArray(new Integer[0]);
		vorhergehendeNummern[vorhergehendeNummern.length - 1]--;
		return new SchrittID(vorhergehendeNummern);
	}

	public SchrittID sameID() {
		return new SchrittID(nummern.toArray(new Integer[0]));
	}
	public SchrittID naechsteEbene() {
		Integer[] naechsteNummern = nummern.toArray(new Integer[nummern.size()+1]);
		naechsteNummern[naechsteNummern.length-1] = 0;
		return new SchrittID(naechsteNummern);
	}

}
