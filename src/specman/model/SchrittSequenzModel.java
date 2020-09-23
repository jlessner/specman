package specman.model;

import specman.SchrittID;

import java.util.ArrayList;
import java.util.List;

public class SchrittSequenzModel {
	public SchrittID id;
	public List<SchrittModel> schritte;
	public List<SchrittModel> catchBloecke;
	public boolean catchBloeckeZugeklappt;
	public int catchBloeckeUmgehungBreite;

	public SchrittSequenzModel() {
		this.schritte = new ArrayList<SchrittModel>();
		this.catchBloecke = new ArrayList<SchrittModel>();
	}

}
