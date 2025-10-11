package specman.editarea;

import specman.editarea.document.WrappedDocument;
import specman.editarea.document.WrappedElement;
import specman.editarea.document.WrappedPosition;

import javax.swing.text.AttributeSet;
import javax.swing.text.html.CSS;

import static specman.editarea.TextStyles.INDIKATOR_GELB;
import static specman.editarea.TextStyles.changedStepnumberLinkHTMLColor;

public class MarkedChar {
  public final char c;
  public final boolean changed;

  public MarkedChar(WrappedDocument doc, WrappedPosition p) {
    c = doc.getChar(p);
    WrappedElement element = doc.getCharacterElement(p);
    changed = elementHasChangeBackground(element);
  }

  public MarkedChar(char c, boolean changed) {
    this.c = c;
    this.changed = changed;
  }

  private static boolean elementHasChangeBackground(WrappedElement element) {
    String backgroundColorValue = getBackgroundColorFromElement(element);
    if (backgroundColorValue != null) {
      return backgroundColorValue.equals(INDIKATOR_GELB) || backgroundColorValue.equals(changedStepnumberLinkHTMLColor);
    }
    return false;
  }

  private static String getBackgroundColorFromElement(WrappedElement element) {
    Object backgroundColorValue = element.getAttributes().getAttribute(CSS.Attribute.BACKGROUND_COLOR);
    return backgroundColorValue != null ? backgroundColorValue.toString() : null;
  }

  private static boolean stepnumberLinkNormalStyleSet(WrappedElement element) {
    String color = getBackgroundColorFromElement(element);
    return color != null && color.equalsIgnoreCase(TextStyles.stepnumberLinkStyleHTMLColor);
  }

}
