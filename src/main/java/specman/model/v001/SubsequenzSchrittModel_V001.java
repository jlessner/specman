package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;
import specman.view.RoundedBorderDecorationStyle;

import java.util.List;

public class SubsequenzSchrittModel_V001 extends StrukturierterSchrittModel_V001 {
	public final SchrittSequenzModel_V001 subsequenz;
	
	@Deprecated public SubsequenzSchrittModel_V001() { // For Jackson only
		subsequenz = null;
	}

	public SubsequenzSchrittModel_V001(
		SchrittID id,
		EditorContent_V001 inhalt,
		int farbe,
		Aenderungsart aenderungsart,
		boolean zugeklappt,
		SchrittSequenzModel_V001 subsequenz,
		SchrittID quellschrittID,
		RoundedBorderDecorationStyle decorationStyle) {
		super(id, inhalt, farbe, aenderungsart, zugeklappt, quellschrittID, decorationStyle);
		this.subsequenz = subsequenz;
	}

	@Override public void addStepRecursively(List<AbstractSchrittModel_V001> allSteps) {
		super.addStepRecursively(allSteps);
		subsequenz.addStepsRecursively(allSteps);
	}
}
