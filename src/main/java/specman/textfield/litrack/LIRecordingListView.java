package specman.textfield.litrack;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.ListView;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** This is an extension of class {@link ListView} being responsible for the rendering of ordered and
 * unordered HTML lists in a JEditorPane. This derived class overrides {@link ListView#paintChild(Graphics, Rectangle, int)}
 * to record the positions of list item prompts (bullet points and numbers) which are required to render
 * these prompts in PDF.
 * <p>
 * The PDF rendering must split the content of edit areas line by line for a correct
 * placement. But from an extracted line of an HTML list you can't tell anymore which index this line originally
 * had in the list (especially required for numbering items in ordered list) and if a line represents the first
 * line of a line item at all or if it is just a follow-up line of a <i>wrapped</i> line item. This class here
 * solves this by keeping track of line item prompt positions which can be queried by the PDF renderer.
 * <p>
 * Recording must explicitly be switched on right before PDF rendering and switched off afterward. This
 * guarantees to record the most recent positions, triggered by forcing a complete repaint of the whole
 * diagramm content. */
public class LIRecordingListView extends ListView {
  private static Map<Document, List<LIRecord>> liPrompts;

  public LIRecordingListView(Element elem) {
    super(elem);
  }

  public static Integer isLILine(Document doc, float y) {
    List<LIRecord> liPromptsForDoc = liPrompts.get(doc);
    if (liPromptsForDoc != null) {
      return liPromptsForDoc.stream()
        .filter(li -> li.getYPosition() == y)
        .map(li -> li.getLiIndex())
        .findFirst()
        .orElse(null);
    }
    return null;
  }

  public static void startRecording() { liPrompts = new HashMap<>(); }

  public static void stopRecording() { liPrompts = null; }

  @Override
  /** This method is called by the parent views for every rendering of line item prompts
   * and for nothing else. So any call is an indicator that we have to record a prompt. */
  protected void paintChild(Graphics g, Rectangle alloc, int index) {
    if (isTracking()) {
      recordLiPrompt(alloc, index);
    }
    super.paintChild(g, alloc, index);
  }

  private boolean isTracking() { return liPrompts != null; }

  private void recordLiPrompt(Rectangle alloc, int index) {
    //System.out.println("recordLiPrompt " + alloc + " / " + index);
    Document doc = getElement().getDocument();
    List<LIRecord> liPromptsForDoc = liPrompts.get(doc);
    if (liPromptsForDoc == null) {
      liPromptsForDoc = new ArrayList<>();
      liPrompts.put(doc, liPromptsForDoc);
    }
    liPromptsForDoc.add(new LIRecord(alloc.y, index));
  }

}
