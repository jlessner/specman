package specman.model.v001;

public class StruktogrammModel_V001 {
	public String name;
	public int breite;
	public int zoomFaktor;
	public SchrittSequenzModel_V001 hauptSequenz;
	public String intro, outro;

	@Deprecated public StruktogrammModel_V001() {} // For Jackson only

	public StruktogrammModel_V001(String name, int breite, int zoomFaktor, SchrittSequenzModel_V001 hauptSequenz, String intro, String outro) {
		this.name = name;
		this.breite = breite;
		this.zoomFaktor = zoomFaktor;
		this.hauptSequenz = hauptSequenz;
		this.intro = intro;
		this.outro = outro;
	}
}
