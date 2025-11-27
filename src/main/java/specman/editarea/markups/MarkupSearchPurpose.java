package specman.editarea.markups;

public enum MarkupSearchPurpose {
  All, FirstChangeOnly;

  public boolean stopAfterFirstMatch() {
    return this == FirstChangeOnly;
  }
}
