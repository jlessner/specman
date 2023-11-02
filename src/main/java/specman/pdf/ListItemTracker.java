package specman.pdf;

public class ListItemTracker {
  public static final int LISTITEM_IDENTION = 50;

  int num = 1;

  public String nextLine(String lineHtml) {
    if (lineHtml.contains("<li>")) {
      String prompt = "<div style=\"text-align:right;width:100%\">" + num + ".</div>";
      num += 9;
      return prompt;
    }
    return null;
  }
}
