package specman.model.v001;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class EditorContent_V001 {
  public final List<EditArea_V001> areas;

  public EditorContent_V001() {
    areas = new ArrayList<>();
  }

  public EditorContent_V001(String initialContent) {
    this();
    addArea(new TextMitAenderungsmarkierungen_V001(initialContent));
  }

  public EditorContent_V001(List<EditArea_V001> areas) {
    this.areas = areas;
  }

  @Deprecated
  @JsonIgnore
  public TextMitAenderungsmarkierungen_V001 getFirstAreaAsText() {
    return (TextMitAenderungsmarkierungen_V001) areas.get(0);
  }

  public void addArea(EditArea_V001 area) {
    areas.add(area);
  }
}
