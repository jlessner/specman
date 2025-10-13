package specman.editarea.markups;

import specman.editarea.document.WrappedPosition;

import java.util.ArrayList;
import java.util.List;

public class MarkedCharSequence {
  final List<MarkedChar> chars = new ArrayList<MarkedChar>();

  public void add(final MarkedChar mc) {
    chars.add(mc);
  }

  public Integer findRight(int pos, CharType charType) {
    while (pos < chars.size()) {
      MarkedChar mc = chars.get(pos);
      if (charType.is(mc.c)) {
        return pos;
      }
      pos++;
    }
    return null;
  }

  public MarkedChar get(int pos) {
    return chars.get(pos);
  }

  public MarkupType type(int pos) {
    return get(pos).markupType;
  }

  public boolean isVisibleChar(int pos) {
    char c = get(pos).c;
    return CharType.NonWhitespace.is(c);
  }

  public void insertParagraphBoundaryAt(WrappedPosition pos, boolean changed) {
    int modelPos = pos.toModel();
    chars.add(modelPos, new MarkedChar('\n', changed ? MarkupType.Changed : null));
  }

  public void append(MarkedCharSequence changemarks) {
    chars.addAll(changemarks.chars);
  }

  public int size() { return chars.size(); }
}
