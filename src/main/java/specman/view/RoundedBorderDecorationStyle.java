package specman.view;

/** Definiert für einen Schritt, ob er innerhalb seiner Sequenz mit einer eigenen abgesetzten Umrandung mit
 * abgerundeten Ecken dargestellt wird oder seinen Bereich in der Sequenz randlos ausfüllt. Die abgesetzte
 * Darstellung lehnt sich an die Repräsentation von Aktivitäten in einem Aktivitätsdiagramm an. Der User
 * kann das für jeden Schritt selber wählen. */
public enum RoundedBorderDecorationStyle {
  /** Keine abgesetzte Darstellung */
  None,

  /** Vollständig abgesetzte Darstellung mit einem Randabstand in alle Richtungen */
  Full,

  /** Abgesetzte Darstellung ohne Abstand nach oben, die Verwendung findet, wenn
   * ein Schritt mit abgesetzter Darstellung einem anderen Schritt mit dieser Darstellung
   * nachfolgt. Andernfalls käme es ja zu einer Verdopplung des Abstands. */
  Co;

  public boolean withTopInset() {
    return this == Full;
  }
}
