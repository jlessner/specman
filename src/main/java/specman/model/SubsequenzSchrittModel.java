package specman.model;

public class SubsequenzSchrittModel extends StrukturierterSchrittModel {
	public SchrittSequenzModel subsequenz;
	
	public SubsequenzSchrittModel() {
		this.subsequenz = new SchrittSequenzModel();
	}
}
