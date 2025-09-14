package specman.editarea;

import javax.swing.text.BadLocationException;

public class WrappedBadLocationException extends RuntimeException {
  public WrappedBadLocationException(BadLocationException blx) {
    super(blx);
  }
}
