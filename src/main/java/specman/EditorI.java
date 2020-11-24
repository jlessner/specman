package specman;

import specman.view.AbstractSchrittView;
import specman.view.SchrittSequenzView;

import javax.swing.*;
import javax.swing.undo.UndoableEdit;
import java.awt.event.FocusListener;
import java.io.File;

/**
 * This interface represents the current struktogramm editor and is supposed to
 * substitute the older direct access by {@link Specman#instance()}. This will
 * e.g. allow to run multiple editors windows within a single editor application.
 */
public interface EditorI extends FocusListener {
	void schrittFuerNachinitialisierungRegistrieren(AbstractSchrittView schritt);

	void vertikalLinieSetzen(int x, SpaltenResizer spaltenResizer);

	SchrittSequenzView findeElternSequenz(AbstractSchrittView schrittView);

	void diagrammLaden(File diagramFile);

	int getZoomFactor();

	void instrumentWysEditor(JEditorPane ed, String initialText, Integer orientation);

	void addEdit(UndoableEdit edit);

	void diagrammAktualisieren(AbstractSchrittView schrittImFokus);
}