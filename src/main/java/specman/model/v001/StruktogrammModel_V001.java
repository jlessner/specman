package specman.model.v001;

import java.util.ArrayList;
import java.util.List;

public class StruktogrammModel_V001 {
	public final String name;
	public final int breite;
	public final int zoomFaktor;
	public final boolean changeModeenabled;
	public final SchrittSequenzModel_V001 hauptSequenz;
	public final EditorContentModel_V001 intro, outro;

	@Deprecated public StruktogrammModel_V001() { // For Jackson only
		this.name = null;
		this.breite = 0;
		this.zoomFaktor = 0;
		this.changeModeenabled = false;
		this.hauptSequenz = null;
		this.intro = null;
		this.outro = null;
	}

	public StruktogrammModel_V001(String name, int breite, int zoomFaktor, boolean changeModeenabled, SchrittSequenzModel_V001 hauptSequenz, EditorContentModel_V001 intro, EditorContentModel_V001 outro) {
		this.name = name;
		this.breite = breite;
		this.changeModeenabled = changeModeenabled;
		this.zoomFaktor = zoomFaktor;
		this.hauptSequenz = hauptSequenz;
		this.intro = intro;
		this.outro = outro;
	}

	public List<AbstractSchrittModel_V001> queryAllSteps() {
		List<AbstractSchrittModel_V001> allSteps = new ArrayList<>();
		hauptSequenz.addStepsRecursively(allSteps);
		return allSteps;
	}
}