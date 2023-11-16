package specman.model.v001;

import specman.Aenderungsart;
import specman.Specman;

import java.util.ArrayList;
import java.util.List;

public class TextEditAreaModel_V001 extends AbstractEditAreaModel_V001 {
	public final String text;
	public final String plainText;
	public final List<Aenderungsmarkierung_V001> aenderungen;
	public final Aenderungsart aenderungsart;

	@Deprecated public TextEditAreaModel_V001() { // For Jackson only
		text = null;
		plainText = null;
		aenderungen = null;
		aenderungsart = null;
	}

	public TextEditAreaModel_V001(String text) { this(text, text, new ArrayList<>(), Specman.initialArt()); }

  public TextEditAreaModel_V001(String text, String plainText, List<Aenderungsmarkierung_V001> aenderungen, Aenderungsart aenderungsart) {
		this.text = text;
		this.plainText = plainText;
		this.aenderungen = aenderungen;
		this.aenderungsart = aenderungsart;
	}
}
