package specman.editarea.changemarks;

import specman.editarea.document.WrappedDocumentI;
import specman.editarea.document.WrappedPosition;

import java.util.List;
import java.util.Objects;

public class ObsoleteWhitespaceSequence {
  final WrappedPosition start;
  final WrappedPosition end;

  public ObsoleteWhitespaceSequence(WrappedPosition start, WrappedPosition end) {
    this.start = start;
    this.end = end;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ObsoleteWhitespaceSequence that = (ObsoleteWhitespaceSequence) o;
    return Objects.equals(start, that.start) && Objects.equals(end, that.end);
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, end);
  }

  public static List<ObsoleteWhitespaceSequence> findAll(WrappedDocumentI document, WrappedPosition cutPosition) {
    return new ObsoleteWhitespaceSequenceFinder(document, cutPosition).findAll();
  }

  public int length() {
    return end.distance(start) + 1;
  }

}
