package specman.model;

import specman.SchrittID;

import java.util.ArrayList;
import java.util.List;

public class SchrittSequenzModel {
	public SchrittID id;
	public List<AbstractSchrittModel> schritte;
	public List<AbstractSchrittModel> catchBloecke;
	public boolean catchBloeckeZugeklappt;
	public int catchBloeckeUmgehungBreite;

	public SchrittSequenzModel() {
		this.schritte = new ArrayList<AbstractSchrittModel>();
		this.catchBloecke = new ArrayList<AbstractSchrittModel>();
	}

}
