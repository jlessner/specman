package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;

import java.util.ArrayList;
import java.util.List;

public class SchrittSequenzModel_V001 {
	public final SchrittID id;
	public final Aenderungsart aenderungsart;
	public final List<AbstractSchrittModel_V001> schritte;
	public final CatchBereichModel_V001 catchBereich;

	@Deprecated public SchrittSequenzModel_V001() { // For Jackson only
		this.id = null;
		this.aenderungsart = null;
		this.schritte = null;
		this.catchBereich = null;
	}

	public SchrittSequenzModel_V001(
		SchrittID id,
		Aenderungsart aenderungsart,
		CatchBereichModel_V001 catchBereich) {
		this.id = id;
		this.aenderungsart = aenderungsart;
		this.schritte = new ArrayList<>();
		this.catchBereich = catchBereich;
	}

	public void addStepsRecursively(List<AbstractSchrittModel_V001> allSteps) {
		for (AbstractSchrittModel_V001 schritt: schritte) {
			schritt.addStepRecursively(allSteps);
		}
	}
}
