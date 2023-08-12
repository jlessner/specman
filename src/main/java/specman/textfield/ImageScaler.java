package specman.textfield;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resizing of image tags in HTML by adapting height and width attribute values
 * The image tags are identified by a regular expression and so are the
 * two relevant attribute values within the tag. If height or width of the
 * image are not specified, there is no scaling applied.
 * <p>
 * Deprecated, weil Bilder nicht mehr im HTML dargestellt werden.
 */
@Deprecated
public class ImageScaler {
  Pattern IMAGE_LINK_PATTERN = Pattern.compile("<img ([^>]+)>");
  Pattern SIZE_ATTRIBUTE_PATTERN = Pattern.compile("(height|width)=\"([\\d]+)\"");

  private final int percentNew;
  private final int percentCurrent;
  private final float scaleFactor;

  public ImageScaler(int percentNew, int percentCurrent) {
    this.percentNew = percentNew;
    this.percentCurrent = percentCurrent;
    this.scaleFactor = (float)percentNew / (float)percentCurrent;
  }

  public String scaleImages(String html) {
    Matcher matcher = IMAGE_LINK_PATTERN.matcher(html);
    return matcher.replaceAll(this::scaleSizeAttributesInImageTag);
  }

  private String scaleSizeAttributesInImageTag(MatchResult matchResult) {
    String imageTag = matchResult.group();
    Matcher sizeMatcher = SIZE_ATTRIBUTE_PATTERN.matcher(imageTag);
    String resizedImageTag = sizeMatcher.replaceAll(this::scaleSizeAttribute);
    return resizedImageTag;
  }

  private String scaleSizeAttribute(MatchResult matchResult) {
    String attribute = matchResult.group(1);
    int attributeValue = Integer.parseInt(matchResult.group(2));
    int scaledValue = (int)(attributeValue * scaleFactor);
    return String.format("%s=\"%d\"", attribute, scaledValue);
  }
}
