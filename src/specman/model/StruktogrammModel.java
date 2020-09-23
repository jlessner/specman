package specman.model;

public class StruktogrammModel {
	public String name;
	public int breite;
	public int zoomFaktor;
	public SchrittSequenzModel hauptSequenz;
	public String intro, outro;
	
	public StruktogrammModel() {
		this.hauptSequenz = new SchrittSequenzModel();
	}

}
