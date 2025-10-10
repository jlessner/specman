package specman.editarea.changemarks;

import specman.editarea.MarkedChar;

import java.util.ArrayList;
import java.util.List;

public class MarkedCharSequence {
  final List<MarkedChar> chars = new ArrayList<MarkedChar>();

  public void add(final MarkedChar mc) {
    chars.add(mc);
  }
}
