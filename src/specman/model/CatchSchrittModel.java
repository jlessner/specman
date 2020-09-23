package specman.model;

public class CatchSchrittModel extends StrukturierterSchrittModel {
	public SchrittSequenzModel handlingSequenz;
	public boolean breakAngekoppelt;
	
	public CatchSchrittModel() {
		this.handlingSequenz = new SchrittSequenzModel();
	}
}
