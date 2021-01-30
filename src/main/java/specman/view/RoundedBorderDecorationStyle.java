package specman.view;

public enum RoundedBorderDecorationStyle {
  None, Full, Co;

  public boolean withTopInset() {
    return this == Full;
  }
}
