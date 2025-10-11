package specman.editarea.changemarks;

import org.junit.jupiter.api.Test;
import specman.model.v001.Aenderungsmarkierung_V001;

import java.util.List;

public class ChangemarkSplitTest extends AbstractChangemarkRecoveryTest {

  @Test
  void testSimpleSplit() {
    List<Aenderungsmarkierung_V001> marksAfterSplit = split(
      " 0 1|2 3 ",
      " x x x x "
    );
    assertMarked(marksAfterSplit,
      0, 2,
      3, 4);
  }

  @Test
  void testSplitAfterSingleBlank() {
    List<Aenderungsmarkierung_V001> marksAfterSplit = split(
      " 0 1 _|2 3 4 ",
      "   x x x x   "
    );
    assertMarked(marksAfterSplit,
      1, 3,
      4, 5);
  }

  @Test
  void testCompleteWhitespaceRemovalOfCaretSequence() {
    List<Aenderungsmarkierung_V001> marksAfterSplit = split(
      " 0 1 2 _|_ _ 6 7 8",
      "             x    "
    );
    assertMarked(marksAfterSplit, 4, 4);
  }

}
