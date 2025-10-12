package specman.editarea.markups;

import specman.editarea.TextStyles;
import specman.editarea.document.WrappedElement;

import javax.swing.text.AttributeSet;
import javax.swing.text.html.CSS;

import static specman.editarea.TextStyles.INDIKATOR_GELB;
import static specman.editarea.TextStyles.INDIKATOR_GRAU;
import static specman.editarea.TextStyles.changedStepnumberLinkHTMLColor;
import static specman.editarea.TextStyles.changedStepnumberLinkStyle;
import static specman.editarea.TextStyles.geaendertTextBackground;
import static specman.editarea.TextStyles.stepnumberLinkStyle;
import static specman.editarea.TextStyles.stepnumberLinkStyleHTMLColor;

public enum MarkupType {
  Changed, Steplink, ChangedSteplink;

  public boolean marksChange() { return this == Changed || this == ChangedSteplink; }

  public static MarkupType fromBackground(WrappedElement element) {
    String backgroundColorValue = getBackgroundColorFromElement(element);
    if (backgroundColorValue != null) {
      if (backgroundColorValue.equals(changedStepnumberLinkHTMLColor)) {
        return MarkupType.ChangedSteplink;
      }
      else if (backgroundColorValue.equals(INDIKATOR_GELB)) {
        return MarkupType.Changed;
      }
      else if (backgroundColorValue.equals(stepnumberLinkStyleHTMLColor)) {
        return MarkupType.Steplink;
      }
    }
    return null;
  }

  private static String getBackgroundColorFromElement(WrappedElement element) {
    Object backgroundColorValue = element.getAttributes().getAttribute(CSS.Attribute.BACKGROUND_COLOR);
    return backgroundColorValue != null ? backgroundColorValue.toString() : null;
  }

  public AttributeSet toBackground() {
    switch(this) {
      case Changed:
        return geaendertTextBackground;
      case Steplink:
        return stepnumberLinkStyle;
      case ChangedSteplink:
        return changedStepnumberLinkStyle;
    }
    return null;
  }
}
