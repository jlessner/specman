package specman.model;

public class WhileSchrittModel extends StrukturierterSchrittModel {
	public SchrittSequenzModel wiederholSequenz;
	public int balkenbreite;
	
	public WhileSchrittModel() {
		this.wiederholSequenz = new SchrittSequenzModel();
	}
}
