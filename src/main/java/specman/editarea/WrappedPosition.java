package specman.editarea;

import specman.editarea.ChangeBackgroundStyleInitializer.StyledSection;
import specman.model.v001.Aenderungsmarkierung_V001;

import java.util.Objects;

public class WrappedPosition {
  final int position;
  final WrappedDocument document;

  public WrappedPosition(int position, WrappedDocument document) {
    this.document = document;
    this.position = position - document.getVisibleTextStart();
  }

  public WrappedPosition(int delta, WrappedPosition from) {
    this.document = from.document;
    this.position = from.position + delta;
  }

  public WrappedPosition(StyledSection styling, WrappedDocument document) {
    this.document = document;
    this.position = styling.start;
  }

  public boolean greater(WrappedPosition than) {
    return position > than.position;
  }

  public int unwrap() {
    return position + document.getVisibleTextStart();
  }

  public WrappedPosition dec() {
    return dec(1);
  }

  public WrappedPosition dec(int length) {
    return new WrappedPosition(-length, this);
  }

  public WrappedPosition max(WrappedPosition rhs) {
    return new WrappedPosition(Math.max(unwrap(), rhs.unwrap()), document);
  }

  public int distance(WrappedPosition lhs) {
    return position - lhs.position;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    WrappedPosition that = (WrappedPosition) o;
    return position == that.position;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(position);
  }

  public boolean less(WrappedPosition than) {
    return position < than.position;
  }

  public WrappedPosition inc(int length) {
    return new WrappedPosition(length, this);
  }

  public WrappedPosition inc() {
    return inc(1);
  }

  public WrappedPosition min(WrappedPosition rhs) {
    return new WrappedPosition(Math.min(unwrap(), rhs.unwrap()), document);
  }

  @Override
  public String toString() {
    return "" + position;
  }
}
