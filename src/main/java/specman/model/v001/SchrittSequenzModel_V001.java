package specman.model.v001;

import specman.SchrittID;

import java.util.ArrayList;
import java.util.List;

public class SchrittSequenzModel_V001 {
	public SchrittID id;
	public List<AbstractSchrittModel_V001> schritte;
	public List<AbstractSchrittModel_V001> catchBloecke;
	public boolean catchBloeckeZugeklappt;
	public int catchBloeckeUmgehungBreite;

	@Deprecated public SchrittSequenzModel_V001() { // For Jackson only
		this.schritte = new ArrayList<AbstractSchrittModel_V001>();
		this.catchBloecke = new ArrayList<AbstractSchrittModel_V001>();
	}

	public SchrittSequenzModel_V001(
		SchrittID id,
		boolean catchBloeckeZugeklappt,
		int catchBloeckeUmgehungBreite) {
		this();
		this.id = id;
		this.catchBloeckeZugeklappt = catchBloeckeZugeklappt;
		this.catchBloeckeUmgehungBreite = catchBloeckeUmgehungBreite;
	}
}
