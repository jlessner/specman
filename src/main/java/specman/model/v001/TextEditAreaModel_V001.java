package specman.model.v001;

import java.util.ArrayList;
import java.util.List;

public class TextEditAreaModel_V001 extends AbstractEditAreaModel_V001 {
	public final String text;
	public final List<Aenderungsmarkierung_V001> aenderungen;

	@Deprecated public TextEditAreaModel_V001() { // For Jackson only
		text = null;
		aenderungen = null;
	}

	public TextEditAreaModel_V001(String text) {
		this(text, new ArrayList<>());
	}

  public TextEditAreaModel_V001(String text, List<Aenderungsmarkierung_V001> aenderungen) {
		this.text = text;
		this.aenderungen = aenderungen;
	}
}
