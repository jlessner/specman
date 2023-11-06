package specman.model.v001;

import specman.Aenderungsart;
import specman.editarea.EditContainer;

import java.util.List;

public class TableEditAreaModel_V001 extends AbstractEditAreaModel_V001 {
  public final Aenderungsart aenderungsart;
  public final List<List<EditorContentModel_V001>> cells;

  public TableEditAreaModel_V001() {  // For Jackson only
    this.cells = null;
    this.aenderungsart = null;
  }

  public TableEditAreaModel_V001(List<List<EditorContentModel_V001>> cells, Aenderungsart aenderungsart) {
    this.cells = cells;
    this.aenderungsart = aenderungsart;
  }

}
