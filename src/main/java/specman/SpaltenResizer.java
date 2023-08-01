package specman;

import specman.undo.UndoableSpaltenbreiteAngepasst;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

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
		//setBackground(Color.RED);
		setCursor(leftRightCursor);
		addMouseListener(new MouseAdapter() {
			@Override public void mouseReleased(MouseEvent e) {
				if (dragX != null) {
					int ermoeglichteVeraenderung = container.spaltenbreitenAnpassenNachMausDragging(e.getX(), spalte);
					editor.addEdit(new UndoableSpaltenbreiteAngepasst(container, ermoeglichteVeraenderung, spalte));
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

	/** Einen eigenen Cursor bauen ist etwas komplizierter als man denkt, wenn man vermeiden möchte, dass Java das
	 * vorgefertigte Icon wild skaliert. Man muss also vorher über {@link Toolkit#getBestCursorSize(int, int)}
	 * feststellen, wie groß ein Cursorbild sein muss (meistens 32x32 oder 64x64). Dann legt man sich ein entsprechend
	 * großes, leeres, transparentes Bild an und schreibt das Cursor-Icon dort oben rechts hinein. Den Hotspot
	 * bilden wir aus Höhe und Breite des Icons. Wir haben für alle Fälle auch noch zwei verschieden große Bilder parat.
	 * <p>
	 * Der Tipp stammt im Kern aus https://stackoverflow.com/questions/2620188/how-to-set-custom-size-for-cursor-in-swing */
	private Cursor createLeftRightCursor() {
		if (leftRightCursor == null) {
			Dimension bestCursorSize = Toolkit.getDefaultToolkit().getBestCursorSize(0, 0);
			try {
				ImageIcon icon = Specman.readImageIcon("left-right-cursor");
				if (icon.getIconWidth() > bestCursorSize.width) {
					icon = Specman.readImageIcon("left-right-cursor-32");
				}
				System.out.println(bestCursorSize);
				final BufferedImage bufferedImage = new BufferedImage( bestCursorSize.width, bestCursorSize.height, BufferedImage.TYPE_INT_ARGB );
				final Graphics graphic = bufferedImage.getGraphics();
				graphic.drawImage(icon.getImage(), 0, 0, null);
				Point hotSpot = new Point(icon.getIconWidth()/2, icon.getIconHeight()/2);
				leftRightCursor = Toolkit.getDefaultToolkit().createCustomCursor(bufferedImage, hotSpot, "Left-Right-Cursor");
			}
			catch(Exception x) {
				leftRightCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
			}
		}
		return leftRightCursor;
	}

}
