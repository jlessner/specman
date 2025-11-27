package specman;

import specman.editarea.TableEditAreaSelectionTracker;
import specman.pdf.LineShape;
import specman.pdf.Shape;
import specman.undo.UndoableSpaltenbreiteAngepasst;
import specman.view.AbstractSchrittView;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import static specman.view.AbstractSchrittView.LINIENBREITE;

public class SpaltenResizer extends JPanel {
	Integer dragX;
	final SpaltenContainerI container;
	final int spalte;
	static Cursor leftRightCursor;

	public SpaltenResizer(SpaltenContainerI container, EditorI editor) {
		this(container, 0, editor);
	}

	public SpaltenResizer(SpaltenContainerI container, int spalte, EditorI editor) {
		Cursor leftRightCursor = createLeftRightCursor();
		this.container = container;
		this.spalte = spalte;
		setOpaque(false);
		setCursor(leftRightCursor);
		addMouseListener(new MouseAdapter() {
			@Override public void mouseReleased(MouseEvent e) {
				if (dragX != null) {
					int ermoeglichteVeraenderung = container.spaltenbreitenAnpassenNachMausDragging(e.getX(), spalte);
					if (ermoeglichteVeraenderung != 0) {
						editor.addEdit(new UndoableSpaltenbreiteAngepasst(container, ermoeglichteVeraenderung, spalte));
					}
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

	private Cursor createLeftRightCursor() {
		if (leftRightCursor == null) {
			leftRightCursor = CursorFactory.createCursor("left-right-cursor");
		}
		return leftRightCursor;
	}

	public Shape getShape() {
		return new LineShape(getX(), getY() + LINIENBREITE, getX(), getY() + getHeight() - LINIENBREITE);
	}
}