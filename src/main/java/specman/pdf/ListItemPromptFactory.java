package specman.pdf;

public class ListItemPromptFactory {
  public static final int LISTITEM_IDENTION = 50;

  public String createPrompt(String lineHtml, Integer liIndex) {
    if (liIndex != null) {
      if (lineHtml.contains("<li>")) {
        String promptText;
        if (lineHtml.contains("<ol>")) {
          promptText = (liIndex+1) + ".";
        }
        else {
          promptText = "<b>&bull;</b>";
        }
        return "<div style=\"text-align:right;width:100%\">" + promptText + "</div>";
      }
    }
    return null;
  }
}
