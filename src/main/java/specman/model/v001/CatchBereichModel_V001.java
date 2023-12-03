package specman.model.v001;

import java.util.ArrayList;
import java.util.List;

public class CatchBereichModel_V001 {
  public final boolean zugeklappt;
  public final List<CatchSchrittSequenzModel_V001> catchSequences;

  public CatchBereichModel_V001() {
    zugeklappt = false;
    catchSequences = null;
  }

  public CatchBereichModel_V001(boolean zugeklappt) {
    this.zugeklappt = zugeklappt;
    this.catchSequences = new ArrayList<>();
  }
}
