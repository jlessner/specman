package specman.model.v001;

import specman.Aenderungsart;

import java.util.List;

public class ListItemEditAreaModel_V001 extends AbstractEditAreaModel_V001 {
  public final EditorContentModel_V001 content;
  public final boolean ordered;
  public final Aenderungsart aenderungsart;

  public ListItemEditAreaModel_V001() {  // For Jackson only
    this.content = null;
    this.ordered = false;
    this.aenderungsart = null;
  }

  public ListItemEditAreaModel_V001(EditorContentModel_V001 content, boolean ordered, Aenderungsart aenderungsart) {
    this.content = content;
    this.ordered = ordered;
    this.aenderungsart = aenderungsart;
  }

}
