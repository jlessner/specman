package specman.model.v001;

import specman.Aenderungsart;
import specman.Specman;

import java.util.ArrayList;
import java.util.List;

public class TextEditAreaModel_V001 extends AbstractEditAreaModel_V001 {
	public final String text;
	public final String plainText;
	public final List<Markup_V001> markups;
	public final Aenderungsart aenderungsart;

	@Deprecated public TextEditAreaModel_V001() { // For Jackson only
		text = null;
		plainText = null;
		markups = null;
		aenderungsart = null;
	}

	public TextEditAreaModel_V001(String text) { this(text, text, new ArrayList<>(), Specman.initialArt()); }

  public TextEditAreaModel_V001(String text, String plainText, List<Markup_V001> markups, Aenderungsart aenderungsart) {
		this.text = text;
		this.plainText = plainText;
		this.markups = markups;
		this.aenderungsart = aenderungsart;
	}
}
