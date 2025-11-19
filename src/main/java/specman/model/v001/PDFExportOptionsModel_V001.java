package specman.model.v001;

/** The selection of page size, orientation etc. might vary from model
 * to model, so we associate the last selection to the model itself. Otherwise
 * it may become quite annoying to keep the exports clean when rapidly changing
 * between multiple models being in work in parallel. All these options except
 * the file name are also stored in the user preferences. So when starting with
 * a completely new model, there is hopefully a reasonable default available. */
public class PDFExportOptionsModel_V001 {
  public final String filename;
  public final String pageSize;
  public final boolean portrait;
  public final boolean paging;

  @Deprecated public PDFExportOptionsModel_V001() { // For Jackson only
    filename = pageSize = null;
    portrait = paging = false;
  }

  public PDFExportOptionsModel_V001(String filename, String pageSize, boolean portrait, boolean paging) {
    this.filename = filename;
    this.pageSize = pageSize;
    this.portrait = portrait;
    this.paging = paging;
  }
}
