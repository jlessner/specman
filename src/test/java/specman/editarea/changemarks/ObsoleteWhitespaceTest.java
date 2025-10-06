package specman.editarea.changemarks;

import org.junit.jupiter.api.Test;
import specman.model.v001.Aenderungsmarkierung_V001;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** The caret position is always at the end of the paragraph so that does
 * not cause any splitting of change marks. The test is focussed on the
 * removal of obsolete whitespaces. */
public class ObsoleteWhitespaceTest extends AbstractChangemarkSplitterTest {

  @Test
  void testLeadingWhitespacesNoMarks() {
    List<Aenderungsmarkierung_V001> marksAfterSplit = split(
      " _ _ _ 3 4 5 6 7 8|",
      "                   "
    );
    assertMarked(marksAfterSplit);
  }

  @Test
  void testLeadingWhitespacesMarksInFollowingText() {
    List<Aenderungsmarkierung_V001> marksAfterSplit = split(
      " _ _ _ 3 4 5 6 7 8|",
      "         x x       "
    );
    assertMarked(marksAfterSplit, 1, 2);
  }

  @Test
  void testTrailingWhitespacesMarksInEarlierText() {
    List<Aenderungsmarkierung_V001> marksAfterSplit = split(
      " 0 1 2 3 4 5 6 _ _|",
      "         x x       "
    );
    assertMarked(marksAfterSplit, 4, 5);
  }

  @Test
  /** The inner double whitespace sequence is not completely removed but only reduced to
   * a single blank. As a consequence, the change marks for following text are only moved
   * by 1 rather than 2. */
  void testReducedWhitespacesMarksInFollowinText() {
    List<Aenderungsmarkierung_V001> marksAfterSplit = split(
      " 0 _ _ 3 4 5 6 7 8|",
      "         x x       "
    );
    assertMarked(marksAfterSplit, 3, 4);
  }

  @Test
  void testReducedChangemarkByIntersectionWithObsoleteWhitespaces() {
    List<Aenderungsmarkierung_V001> marksAfterSplit = split(
      " 0 _ _ _ 4 5 6 7 8|",
      "     x x x x x     "
    );
    assertMarked(marksAfterSplit, 2, 4);
  }

  @Test
  void testJoinedChangemarksByIntersectionWithObsoleteWhitespaces() {
    List<Aenderungsmarkierung_V001> marksAfterSplit = split(
      " 0 _ _ _ 4 5 6 7 8|",
      " x x     x x x     "
    );
    assertMarked(marksAfterSplit, 0, 4);
  }

  @Test
  void testVanishedChangemarksByIntersectionWithObsoleteWhitespaces() {
    List<Aenderungsmarkierung_V001> marksAfterSplit = split(
      " 0 _ _ _ 4 5 6 7 8|",
      "     x x           "
    );
    assertMarked(marksAfterSplit);
  }

  @Test
  void testMultipleWhitespaceLosses() {
    List<Aenderungsmarkierung_V001> marksAfterSplit = split(
      " _ 1 _ _ 4 _ 6 _|",
      " x x x x x x x x "
    );
    assertMarked(marksAfterSplit, 0, 4);
  }

}
