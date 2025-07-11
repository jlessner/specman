package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;
import specman.view.RoundedBorderDecorationStyle;

import java.util.List;

import static specman.view.RoundedBorderDecorationStyle.None;

public abstract class AbstractSchrittModel_V001 {
	public final SchrittID id;
	public final String nummer;
	public final EditorContentModel_V001 inhalt;
	public final int farbe;
	public final Aenderungsart aenderungsart;
	public final SchrittID quellschrittID;
	public final RoundedBorderDecorationStyle decorationStyle;

	@Deprecated AbstractSchrittModel_V001() { // For Jackson only
		this(null, null, 0, null, null, None);
	}

	AbstractSchrittModel_V001(
			SchrittID id,
			EditorContentModel_V001 inhalt,
			int farbe,
			Aenderungsart aenderungsart,
			SchrittID quellschrittID,
			RoundedBorderDecorationStyle decorationStyle) {
		this.id = id;
		this.nummer = id != null ? id.toString() : null;
		this.inhalt = inhalt;
		this.farbe = farbe;
		this.aenderungsart = aenderungsart;
		this.quellschrittID=quellschrittID;
		this.decorationStyle = decorationStyle;
	}

	public void addStepRecursively(List<AbstractSchrittModel_V001> allSteps) {
		allSteps.add(this);
	}
}
