package specman;

import java.awt.*;

import static specman.editarea.TextStyles.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE;
import static specman.editarea.TextStyles.BACKGROUND_COLOR_STANDARD;

public enum Aenderungsart {
	Untracked, Hinzugefuegt, Geloescht, Quellschritt, Zielschritt;

	public Color toBackgroundColor() {
		return (this == Hinzugefuegt || this == Geloescht)
			? AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE
			: BACKGROUND_COLOR_STANDARD;
	}

	public boolean istAenderung() { return this != Untracked; }

	public int asNumChanges() { return istAenderung() ? 1 : 0; }
}
