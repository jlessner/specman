package specman.model.v001;

import specman.Aenderungsart;
import specman.SchrittID;

import java.util.ArrayList;
import java.util.List;

public class SchrittSequenzModel_V001 {
	public final SchrittID id;
	public final Aenderungsart aenderungsart;
	public final List<AbstractSchrittModel_V001> schritte;
	public final List<AbstractSchrittModel_V001> catchBloecke;
	public final boolean catchBloeckeZugeklappt;
	public final int catchBloeckeUmgehungBreite;

	@Deprecated public SchrittSequenzModel_V001() { // For Jackson only
		this.id = null;
		this.aenderungsart = null;
		this.schritte = null;
		this.catchBloecke = null;
		this.catchBloeckeZugeklappt = false;
		this.catchBloeckeUmgehungBreite = 0;
	}

	public SchrittSequenzModel_V001(
		SchrittID id,
		Aenderungsart aenderungsart,
		boolean catchBloeckeZugeklappt,
		int catchBloeckeUmgehungBreite) {
		this.id = id;
		this.aenderungsart = aenderungsart;
		this.schritte = new ArrayList<AbstractSchrittModel_V001>();
		this.catchBloecke = new ArrayList<AbstractSchrittModel_V001>();
		this.catchBloeckeZugeklappt = catchBloeckeZugeklappt;
		this.catchBloeckeUmgehungBreite = catchBloeckeUmgehungBreite;
	}

	public void addStepsRecursively(List<AbstractSchrittModel_V001> allSteps) {
		for (AbstractSchrittModel_V001 schritt: schritte) {
			schritt.addStepRecursively(allSteps);
		}
	}
}
