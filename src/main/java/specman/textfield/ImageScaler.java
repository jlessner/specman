package specman.textfield;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Aufbau einer Image-Referenz in HTML, Reihenfolge der Attribute ist beliebig
 * <img height="300" vspace="1" border="1" hspace="1" width="300" alt="alt text" src="image.png" align="top">
 * Regulärer Ausdruck zum Suchen: "<img [^>]+>"
 * Darin lassen sich Höhe und Breite wiederum durch reguläre Ausdrücke finden.
 * Wenn Höhe und/oder Breite nicht angegeben sind, machen wir keine Skalierung
 */
public class ImageScaler {
  private final int percentNew;
  private final int percentCurrent;

  public ImageScaler(int percentNew, int percentCurrent) {
    this.percentNew = percentNew;
    this.percentCurrent = percentCurrent;
  }

  public String scaleImages(String html) {
    Pattern imageLinkPattern = Pattern.compile("(<img ([^>]+)>)");
    Matcher matcher = imageLinkPattern.matcher(html);
    return matcher.replaceAll(this::scalingReplacer);
  }

  private String scalingReplacer(MatchResult matchResult) {
    System.out.println(matchResult.group(0));
    return null;
  }

}
