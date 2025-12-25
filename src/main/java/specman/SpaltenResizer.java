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
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import static java.awt.Cursor.DEFAULT_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;
import static specman.view.AbstractSchrittView.LINIENBREITE;

public class SpaltenResizer extends JPanel implements MouseListener {
	Integer dragX;
	final SpaltenContainerI container;
	final int spalte;
	static Cursor leftRightCursor;

	public SpaltenResizer(SpaltenContainerI container) {
		this(container, 0);
	}

	public SpaltenResizer(SpaltenContainerI container, int spalte) {
		createLeftRightCursor();
		this.container = container;
		this.spalte = spalte;
		setOpaque(false);
    addMouseListener(this);

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override public void mouseDragged(MouseEvent e) {
				dragX = e.getX();
				Specman.instance().vertikalLinieSetzen(dragX, SpaltenResizer.this);
			}
		});
	}

	private Cursor createLeftRightCursor() {
		if (leftRightCursor == null) {
			leftRightCursor = CursorFactory.createCursor("left-right-cursor");
		}
		return leftRightCursor;
	}

  @Override public void mouseClicked(MouseEvent e) {}
  @Override public void mousePressed(MouseEvent e) {}

  @Override
  public void mouseReleased(MouseEvent e) {
      if (dragX != null) {
        int ermoeglichteVeraenderung = container.spaltenbreitenAnpassenNachMausDragging(e.getX(), spalte);
        if (ermoeglichteVeraenderung != 0) {
          Specman.instance().addEdit(new UndoableSpaltenbreiteAngepasst(container, ermoeglichteVeraenderung, spalte));
        }
        dragX = null;
        Specman.instance().vertikalLinieSetzen(0, null);
      }
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    Specman.instance().setCursor(leftRightCursor);
  }

  @Override
  public void mouseExited(MouseEvent e) {
    Specman.instance().setCursor(Cursor.getDefaultCursor());
  }

  public Shape getShape() {
		return new LineShape(getX(), getY() + LINIENBREITE, getX(), getY() + getHeight() - LINIENBREITE);
	}
}