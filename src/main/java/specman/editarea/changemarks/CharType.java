package specman.editarea.changemarks;

import specman.editarea.document.WrappedPosition;

public enum CharType {
  ParagraphBoundary, NonWhitespace, Whitespace;

  public boolean at(WrappedPosition pos) {
    return is(pos.charAt());
  }

  public boolean is(char ch) {
    if (this == ParagraphBoundary) {
      return ch == '\n';
    }
    else if (this == NonWhitespace) {
      return !isWhitespace(ch) && ch != '\n';
    }
    else if (this == Whitespace) {
      return isWhitespace(ch);
    }
    return true;
  }

  public boolean isWhitespace(char ch) {
    return ch == ' ' || ch == '\t';
  }

}
