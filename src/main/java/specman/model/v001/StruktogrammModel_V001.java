package specman.model.v001;

import java.util.ArrayList;
import java.util.List;

public class StruktogrammModel_V001 {
	public final String name;
	public final int breite;
	public final int zoomFaktor;
	public final SchrittSequenzModel_V001 hauptSequenz;
	public final EditorContent_V001 intro, outro;

	@Deprecated public StruktogrammModel_V001() { // For Jackson only
		this.name = null;
		this.breite = 0;
		this.zoomFaktor = 0;
		this.hauptSequenz = null;
		this.intro = null;
		this.outro = null;
	}

	public StruktogrammModel_V001(String name, int breite, int zoomFaktor, SchrittSequenzModel_V001 hauptSequenz, EditorContent_V001 intro, EditorContent_V001 outro) {
		this.name = name;
		this.breite = breite;
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
