package specman;

import java.awt.event.FocusListener;

public interface EditorI extends FocusListener {
	void schrittFuerNachinitialisierungRegistrieren(SchrittView schritt);

	void vertikalLinieSetzen(int x, SpaltenResizer spaltenResizer);

	SchrittSequenzView findeElternSequenz(SchrittView schrittView);
}
