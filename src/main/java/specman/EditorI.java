package specman;

import specman.view.AbstractSchrittView;
import specman.view.SchrittSequenzView;

import java.awt.event.FocusListener;

public interface EditorI extends FocusListener {
	void schrittFuerNachinitialisierungRegistrieren(AbstractSchrittView schritt);

	void vertikalLinieSetzen(int x, SpaltenResizer spaltenResizer);

	SchrittSequenzView findeElternSequenz(AbstractSchrittView schrittView);
}
