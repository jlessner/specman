package specman.model.v001;

import java.util.ArrayList;
import java.util.List;

public class CatchBereichModel_V001 {
  public final List<CatchSchrittSequenzModel_V001> catchSequences;
  public final List<Integer> sequencesWidthPercent;
  public final boolean zugeklappt;

  public CatchBereichModel_V001() {
    catchSequences = null;
    sequencesWidthPercent = null;
    zugeklappt = false;
  }

  public CatchBereichModel_V001(List<Integer> sequencesWidthPercent, boolean zugeklappt) {
    this.sequencesWidthPercent = sequencesWidthPercent;
    this.zugeklappt = zugeklappt;
    this.catchSequences = new ArrayList<>();
  }
}
