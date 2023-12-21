package specman.pdf;

public class ListItemPromptFactory {
  public static final int LISTITEM_IDENTION = 50;

  public String createPrompt(String lineHtml, Integer liIndex) {
    if (liIndex != null) {
      // Don't check for complete list item tag "<li>" here, because it might be equipped with strange
      // (irrelevant) styling information when it was copied over from MS Word documents. So we need
      // some tolerance here. See also FormattedShapeText#removeLinebreakingElementsFromHtmlLine
      if (lineHtml.contains("<li")) {
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
