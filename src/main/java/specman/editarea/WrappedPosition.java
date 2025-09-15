package specman.editarea;

import specman.model.v001.Aenderungsmarkierung_V001;

import javax.swing.text.Document;
import java.util.Objects;

public class WrappedPosition {
  final int position;
  final WrappedDocument document;

  /** This constructor is package-visible by intention as it is only allowed to
   * be used from {@link WrappedDocument} to make sure that the passed position
   * integer is always properly converted to a logical value. If it comes from
   * a model object like {@link Aenderungsmarkierung_V001}, the value is supposed
   * to be used as is, while integers from a {@link Document} might require adaption. */
  WrappedPosition(int position, WrappedDocument document) {
    this.document = document;
    this.position = position;
  }

  public WrappedPosition(int delta, WrappedPosition from) {
    this.document = from.document;
    this.position = from.position + delta;
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
    return document.fromUI(Math.max(unwrap(), rhs.unwrap()));
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
    return document.fromUI(Math.min(unwrap(), rhs.unwrap()));
  }

  @Override
  public String toString() {
    return "" + position;
  }

  public int toModel() { return position; }
}
