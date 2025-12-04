package specman;

import java.util.Properties;

public class SpecmanVersion {
  public static final String UNKNOWN_VERSION = "unknown";
  static Properties versionProperties;

  public static String getVersion() {
    if (versionProperties == null) {
      versionProperties = new Properties();
      try {
        versionProperties.load(SpecmanVersion.class.getResourceAsStream("/specman-version.properties"));
      }
      catch (Exception e) {
        e.printStackTrace();
        return UNKNOWN_VERSION;
      }
    }
    return versionProperties.getProperty("version", UNKNOWN_VERSION);
  }

}
