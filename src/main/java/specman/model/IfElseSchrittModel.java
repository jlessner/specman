package specman.model;

public class IfElseSchrittModel extends StrukturierterSchrittModel {
	public ZweigSchrittSequenzModel ifSequenz;
	public ZweigSchrittSequenzModel elseSequenz;
	public float ifBreitenanteil;
	
	public IfElseSchrittModel() {
		this.ifSequenz = new ZweigSchrittSequenzModel();
		this.elseSequenz = new ZweigSchrittSequenzModel();
	}
}
