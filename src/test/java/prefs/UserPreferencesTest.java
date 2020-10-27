package prefs;

import org.junit.jupiter.api.Test;
import specman.Specman;

import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserPreferencesTest {

    @Test
    void testWriteAndReadPreferences() {
        String PREF_NAME = "pref";
        String PREF_VALUE = "value";
        Preferences prefsWrite = Preferences.userNodeForPackage(Specman.class);
        prefsWrite.put(PREF_NAME, PREF_VALUE);
        Preferences prefsRead = Preferences.userNodeForPackage(Specman.class);
        assertEquals(PREF_VALUE, prefsRead.get(PREF_NAME, null));
    }
}
