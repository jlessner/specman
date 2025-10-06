package specman.editarea.changemarks;

import specman.editarea.TextEditArea;
import specman.editarea.document.WrappedDocument;
import specman.model.v001.Aenderungsmarkierung_V001;

import javax.swing.text.MutableAttributeSet;

import java.util.ArrayList;
import java.util.List;

import static specman.editarea.TextStyles.geaendertTextBackground;
import static specman.editarea.TextStyles.standardTextBackground;

/** The yellow background of text sections being marked as changed, is <i>not</i> included
 * in the HTML content of a text area. The styling must therefore be initialized from the
 * change list in the model. Important detail: text without change-style, following a
 * change-styled section must <i>explicitely</i> be "un-styled", otherwise the change-style
 * applies to the succeeding text too. The styles being used here are "overlays", only focussed
 * on text background. So they do not destroy any foreground styling, font sizing and so on. */
public class ChangeBackgroundStyleInitializer {
  private final WrappedDocument doc;
  private final List<Aenderungsmarkierung_V001> model;

  public ChangeBackgroundStyleInitializer(TextEditArea textEditArea, List<Aenderungsmarkierung_V001> model) {
    // TODO JL: This is not yet complete as it erases the background styling of step references.
    this.doc = textEditArea.getWrappedDocument();
    this.model = model;
  }

  private List<StyledSection> model2StyledSections(List<Aenderungsmarkierung_V001> model) {
    List<StyledSection> stylings = new ArrayList<>();
    for (int i = 0; i < model.size(); i++) {
      Aenderungsmarkierung_V001 change = model.get(i);
      StyledSection changeSection = new StyledSection(change.getVon(), change.laenge(), geaendertTextBackground);
      stylings.add(changeSection);
      StyledSection standardSection = followingStandardSection(model, i);
      if (standardSection != null) {
        stylings.add(standardSection);
      }
    }
    return stylings;
  }

  private StyledSection followingStandardSection(List<Aenderungsmarkierung_V001> model, int i) {
    Aenderungsmarkierung_V001 lastChange = model.get(i);
    int resetStart = lastChange.getVon() + lastChange.laenge() + 1;
    int resetLength;
    if (model.size() > i+1) {
      Aenderungsmarkierung_V001 nextChange = model.get(i+1);
      resetLength = nextChange.getVon() - 1 - resetStart;
    }
    else {
      resetLength = doc.getLength() - resetStart;
    }
    return resetLength > 0
      ? new StyledSection(resetStart, resetLength, standardTextBackground)
      : null;
  }

  public void styleChangedTextSections() {
    List<StyledSection> stylings = model2StyledSections(model);
    for (StyledSection styling: stylings) {
      doc.setCharacterAttributes(doc.fromModel(styling.start), styling.length, styling.style, false);
    }
  }

  private static class StyledSection {
    final int start;
    final int length;
    final MutableAttributeSet style;

    public StyledSection(int start, int length, MutableAttributeSet style) {
      this.start = start;
      this.length = length;
      this.style = style;
    }

  }
}
