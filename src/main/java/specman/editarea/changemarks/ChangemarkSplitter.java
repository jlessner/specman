package specman.editarea.changemarks;

import specman.editarea.document.WrappedDocumentI;
import specman.editarea.document.WrappedPosition;
import specman.model.v001.Aenderungsmarkierung_V001;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** Splitting a paragraph in two by pressing ENTER will remove all changemarks in the paragraph's text
 * being represented by background-coloring. This is due to the fact that the coloring os only realized
 * by a volatile styling which is not permanent in the resulting HTML content. So the changemarks must
 * be restored after the split. This is a bit more complicated than expected, as the split will also
 * cause the removal of whitespaces. So we have to find the whitespaces which are about to be removed
 * and adapt the changemarks accordingly. The following whitespaces will be removed by the JEditorPane
 * by the paragraph split:
 * <ul>
 *   <li>All whitespaces before the first visible character of the paragraph</li>
 *   <li>All whitespaces after the last visible character of the paragraph</li>
 *   <li>All whitespaces before and the after the cut position</li>
 *   <li>Any other sequence of whitespaces within the paragraph will be reduced to a single blank</li>
 * </ul>
 */
public class ChangemarkSplitter {
  final WrappedDocumentI document;
  final WrappedPosition cutPosition;
  final List<Aenderungsmarkierung_V001> changemarks;

  public ChangemarkSplitter(WrappedDocumentI document, WrappedPosition cutPosition, List<Aenderungsmarkierung_V001> changemarks) {
    this.document = document;
    this.cutPosition = cutPosition;
    this.changemarks = changemarks;
  }

  public List<Aenderungsmarkierung_V001> split() {
    WrappedPosition cutAfterRemovals = removeObsoleteWhitespacesFromChangemarks();
    List<Aenderungsmarkierung_V001> result = new ArrayList<>();
    result.addAll(marksBeforeCut(cutAfterRemovals));
    result.addAll(splitMark(cutAfterRemovals));
    result.addAll(marksAfterCut(cutAfterRemovals));
    return result;
  }

  /** The upcoming parameter split will cause the removal of whitespace sequences
   * <ul>
   *   <li>Before the first visible character of the paragraph</li>
   *   <li>After the last visible character of the paragraph</li>
   *   <li>Before and after the cutPosition</li>
   *   <li>Any other sequence of whitespaces within the paragraph will be reduced to a single blank</li>
   * </ul>
   * Changemarks which intersect with these sequences must be reduced accordingly.
   * The method returns the adapted cut position, taking into account the number of
   * obsolete whitespaces <i>in front</i> if the current cut position. The position
   * index must be reduced accordingly before actually splitting the change marks at
   * this position. */
  private WrappedPosition removeObsoleteWhitespacesFromChangemarks() {
    List<ObsoleteWhitespaceSequence> whitespaces = ObsoleteWhitespaceSequence.findAll(document, cutPosition);
    removeFromChangemarks(whitespaces);
    return reduceCutPositionByObsoleteWhitespacesInFront(whitespaces);
  }

  private WrappedPosition reduceCutPositionByObsoleteWhitespacesInFront(List<ObsoleteWhitespaceSequence> whitespaces) {
    int delta = 0;
    for (ObsoleteWhitespaceSequence ws : whitespaces) {
      if (ws.end.less(cutPosition)) {
        delta += ws.length();
      }
      else if (ws.start.greater(cutPosition)) {
        break;
      }
      else {
        delta += cutPosition.distance(ws.start);
      }
    }
    return cutPosition.dec(delta);
  }

  /** The removal of whitespaces might cause changemarks to be reduced in their length,
   * vanish completely, and become a direct neighbour of another changemark so that
   * they should be joined. To make this tricky intersection operation easy to understand,
   * we perform it in a kind of "graphical" way:
   * <ol>
   *   <li>We build up a boolean array as long as the document</li>
   *   <li>We set all those booleans which belong to a change mark</li>
   *   <li>We remove all those boolean belonging to an obsolete whitespace sequence,
   *     beginning from the end, so that we don't have to struggle with different indexes</li>
   *   <li>We create new change marks from the remaining booleans</li>
   * </ol> */
  private void removeFromChangemarks(List<ObsoleteWhitespaceSequence> whitespaces) {
    boolean[] marksPerChar = new boolean[document.getLength()];
    for (Aenderungsmarkierung_V001 mark : changemarks) {
      Arrays.fill(marksPerChar, mark.getVon(), mark.getBis()+1, true);
    }
    for (int i = whitespaces.size() - 1; i >= 0; i--) {
      marksPerChar = removeRange(marksPerChar, whitespaces.get(i));
    }
    reassembleChangemarksFromBooleans(marksPerChar);
  }

  private void reassembleChangemarksFromBooleans(boolean[] marksPerChar) {
    changemarks.clear();
    int markStart = -1;
    for (int i = 0; i < marksPerChar.length; i++) {
      if (marksPerChar[i]) {
        if (markStart < 0) {
          markStart = i;
        }
      }
      else {
        if (markStart >= 0) {
          changemarks.add(new Aenderungsmarkierung_V001(markStart, i - 1));
          markStart = -1;
        }
      }
    }
    if (markStart >= 0) {
      changemarks.add(new Aenderungsmarkierung_V001(markStart, marksPerChar.length - 1));
    }
  }

  private boolean[] removeRange(boolean[] changemarked, ObsoleteWhitespaceSequence seq) {
    int seqStart = seq.start.toModel();
    int seqEnd = seq.end.toModel();
    boolean[] result = new boolean[changemarked.length - seq.length()];
    System.arraycopy(changemarked, 0, result, 0, seqStart);
    System.arraycopy(changemarked, seqEnd+1, result, seqStart, changemarked.length-seqEnd-1);

    return result;
  }

  private List<Aenderungsmarkierung_V001> marksBeforeCut(WrappedPosition cutPosition) {
    return changemarks
      .stream()
      .filter(mark -> mark.getBis() < cutPosition.toModel())
      .toList();
  }

  private Collection<? extends Aenderungsmarkierung_V001> marksAfterCut(WrappedPosition cutPosition) {
    return changemarks
      .stream()
      .filter(mark -> mark.getVon() > cutPosition.toModel())
      .map(mark -> mark.shiftright())
      .toList();
  }

  private Collection<? extends Aenderungsmarkierung_V001> splitMark(WrappedPosition cutPosition) {
    List<Aenderungsmarkierung_V001> splitting = new ArrayList<>();
    Aenderungsmarkierung_V001 markToSplit = findMarkToSplit(cutPosition);
    if (markToSplit != null) {
      splitting.add(markHead(markToSplit, cutPosition));
      splitting.add(markTail(markToSplit, cutPosition));
    }
    return splitting;
  }

  private Aenderungsmarkierung_V001 markHead(Aenderungsmarkierung_V001 mark, WrappedPosition cutPosition) {
    return new Aenderungsmarkierung_V001(mark.getVon(), cutPosition.toModel());
  }

  private Aenderungsmarkierung_V001 markTail(Aenderungsmarkierung_V001 mark, WrappedPosition cutPosition) {
    return new Aenderungsmarkierung_V001(cutPosition.toModel(), mark.getBis()).shiftright();
  }

  private Aenderungsmarkierung_V001 findMarkToSplit(WrappedPosition cutPosition) {
    return changemarks
      .stream()
      .filter(mark -> cutPosition.isInChangeMark(mark))
      .findFirst()
      .orElse(null);
  }

}
