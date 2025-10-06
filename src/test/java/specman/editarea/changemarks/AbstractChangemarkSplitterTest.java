package specman.editarea.changemarks;

import specman.editarea.document.TestWrappedDocument;
import specman.editarea.document.WrappedPosition;
import specman.model.v001.Aenderungsmarkierung_V001;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

abstract class AbstractChangemarkSplitterTest {
  protected void assertMarked(List<Aenderungsmarkierung_V001> marks, Integer... fromTo) {
    assertNotNull(marks);
    assertEquals(fromTo.length / 2, marks.size());
    List<Aenderungsmarkierung_V001> expected = marks(fromTo);
    assertEquals(expected, marks);
  }

  protected List<Aenderungsmarkierung_V001> split(String s1, String s2) {
    String documentContent = s1
      .replace("|", "")
      .replace(" ", "")
      .replace("_", " ");
    TestWrappedDocument doc = new TestWrappedDocument(documentContent);
    WrappedPosition cut = doc.fromModel(s1.indexOf('|') / 2);
    List<Aenderungsmarkierung_V001> marks = marksString2Marks(s2);
    return new ChangemarkSplitter(doc, cut, marks).split();
  }

  private List<Aenderungsmarkierung_V001> marksString2Marks(String markstring) {
    List<Aenderungsmarkierung_V001> marks = new ArrayList<>();
    String gapsRemoved = "";
    for (int i = 0; i < markstring.length()-1; i += 2) {
      gapsRemoved += markstring.substring(i+1, i+2);
    }
    Integer markstart = null;
    int marklength = 0;
    for (int i = 0; i < gapsRemoved.length(); i++) {
      if (gapsRemoved.charAt(i) == 'x') {
        if (markstart == null) {
          markstart = i;
          marklength = 1;
        }
        else {
          marklength++;
        }
      }
      else {
        if (markstart != null) {
          marks.add(new Aenderungsmarkierung_V001(markstart, markstart + marklength - 1));
          markstart = null;
        }
      }
    }
    if (markstart != null) {
      marks.add(new Aenderungsmarkierung_V001(markstart, markstart + marklength - 1));
    }
    return marks;
  }

  protected List<Aenderungsmarkierung_V001> marks(Integer... fromTo) {
    List<Aenderungsmarkierung_V001> marks = new ArrayList<>();
    for (int i = 0; i < fromTo.length; i += 2) {
      Aenderungsmarkierung_V001 mark = new Aenderungsmarkierung_V001(fromTo[i], fromTo[i+1]);
      marks.add(mark);
    }
    return marks;
  }

}
