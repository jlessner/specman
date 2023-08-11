package specman.model.v001;

import java.util.ArrayList;
import java.util.List;

public class TextMitAenderungsmarkierungen_V001 extends EditArea_V001 {
	public final String text;
	public final List<Aenderungsmarkierung_V001> aenderungen;

	@Deprecated public TextMitAenderungsmarkierungen_V001() { // For Jackson only
		text = null;
		aenderungen = null;
	}

	public TextMitAenderungsmarkierungen_V001(String text) {
		this(text, new ArrayList<>());
	}

  public TextMitAenderungsmarkierungen_V001(String text, List<Aenderungsmarkierung_V001> aenderungen) {
		this.text = text;
		this.aenderungen = aenderungen;
	}
}
