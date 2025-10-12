package specman.editarea.document;

import javax.swing.text.AttributeSet;

public interface WrappedDocumentI {
  void remove(WrappedPosition offs, int len);
  default void removeFrom(WrappedPosition offs) { remove(offs, getLength() - offs.toModel() - 1); }
  default void remove(int len) { remove(fromModel(0), len); }
  void insertString(WrappedPosition offset, String str, AttributeSet a);
  String getText(WrappedPosition offset, int length);
  int getLength();
  WrappedPosition fromUI(int position);
  WrappedPosition fromModel(int position);
  int getVisibleTextStart();
  default char getChar(WrappedPosition position) { return getText(position, 1).charAt(0); }
}
