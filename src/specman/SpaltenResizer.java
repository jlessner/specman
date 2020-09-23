package specman;

import specman.undo.UndoableSpaltenbreiteAngepasst;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class SpaltenResizer extends JPanel {
	Integer dragX;
	final SpaltenContainerI container;
	final int spalte;

	public SpaltenResizer(SpaltenContainerI container, EditorI editor) {
		this(container, 0, editor);
	}

	public SpaltenResizer(SpaltenContainerI container, int spalte, EditorI editor) {
		this.container = container;
		this.spalte = spalte;
		setOpaque(false);
		setCursor(new Cursor(com.sun.glass.ui.Cursor.CURSOR_RESIZE_LEFTRIGHT));
		addMouseListener(new MouseAdapter() {
			@Override public void mouseReleased(MouseEvent e) {
				if (dragX != null) {
					int ermoeglichteVeraenderung = container.spaltenbreitenAnpassenNachMausDragging(e.getX(), spalte);
					Specman.addEdit(new UndoableSpaltenbreiteAngepasst(container, ermoeglichteVeraenderung, spalte));
					dragX = null;
					editor.vertikalLinieSetzen(0, null);
				}
			}

		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override public void mouseDragged(MouseEvent e) {
				dragX = e.getX();
				editor.vertikalLinieSetzen(dragX, SpaltenResizer.this);
			}
		});
	}

}
