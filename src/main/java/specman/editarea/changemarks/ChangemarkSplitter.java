package specman.editarea.changemarks;

import specman.editarea.WrappedPosition;
import specman.model.v001.Aenderungsmarkierung_V001;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChangemarkSplitter {
  final List<Aenderungsmarkierung_V001> changemarks;

  public ChangemarkSplitter(List<Aenderungsmarkierung_V001> changemarks) {
    this.changemarks = changemarks;
  }

  public List<Aenderungsmarkierung_V001> split(WrappedPosition cutPosition) {
    List<Aenderungsmarkierung_V001> result = new ArrayList<>();
    int cutPositionInt = cutPosition.toModel();
    result.addAll(marksBeforeCut(cutPositionInt));
    result.addAll(splitMark(cutPositionInt));
    result.addAll(marksAfterCut(cutPositionInt));
    return result;
  }

  private List<Aenderungsmarkierung_V001> marksBeforeCut(int cutPosition) {
    return changemarks
      .stream()
      .filter(mark -> mark.getBis() < cutPosition)
      .toList();
  }

  private Collection<? extends Aenderungsmarkierung_V001> marksAfterCut(int cutPosition) {
    return changemarks
      .stream()
      .filter(mark -> mark.getVon() > cutPosition)
      .map(mark -> shiftright(mark))
      .toList();
  }

  private Aenderungsmarkierung_V001 shiftright(Aenderungsmarkierung_V001 mark) {
    return new Aenderungsmarkierung_V001(mark.getVon() + 1, mark.getBis() + 1);
  }

  private Collection<? extends Aenderungsmarkierung_V001> splitMark(int cutPositionInt) {
    List<Aenderungsmarkierung_V001> splitting = new ArrayList<>();
    Aenderungsmarkierung_V001 markToSplit = findMarkToSplit(cutPositionInt);
    if (markToSplit != null) {
      splitting.add(markHead(markToSplit, cutPositionInt));
      splitting.add(markTail(markToSplit, cutPositionInt));
    }
    return splitting;
  }

  private Aenderungsmarkierung_V001 markHead(Aenderungsmarkierung_V001 mark, int cutPositionInt) {
    return new Aenderungsmarkierung_V001(mark.getVon(), cutPositionInt);
  }

  private Aenderungsmarkierung_V001 markTail(Aenderungsmarkierung_V001 mark, int cutPositionInt) {
    return shiftright(new Aenderungsmarkierung_V001(cutPositionInt, mark.getBis()));
  }

  private Aenderungsmarkierung_V001 findMarkToSplit(int cutPositionInt) {
    return changemarks
      .stream()
      .filter(mark -> mark.getVon() <= cutPositionInt && mark.getBis() >= cutPositionInt)
      .findFirst()
      .orElse(null);
  }

}
