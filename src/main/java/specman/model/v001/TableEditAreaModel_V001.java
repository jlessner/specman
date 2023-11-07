package specman.model.v001;

import specman.Aenderungsart;
import specman.editarea.EditContainer;

import java.util.List;

public class TableEditAreaModel_V001 extends AbstractEditAreaModel_V001 {
  public final List<List<EditorContentModel_V001>> cells;
  public final int tableWidthPercent;
  public final List<Integer> columnsWidthPercent;
  public final Aenderungsart aenderungsart;

  public TableEditAreaModel_V001() {  // For Jackson only
    this.cells = null;
    this.tableWidthPercent = 0;
    this.columnsWidthPercent = null;
    this.aenderungsart = null;
  }

  public TableEditAreaModel_V001(List<List<EditorContentModel_V001>> cells, int tableWidthPercent, List<Integer> columnsWidthPercent, Aenderungsart aenderungsart) {
    this.cells = cells;
    this.tableWidthPercent = tableWidthPercent;
    this.columnsWidthPercent = columnsWidthPercent;
    this.aenderungsart = aenderungsart;
  }

}
