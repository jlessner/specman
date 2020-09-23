package specman.view;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Dieser Button dient dazu, unterstrukturierte Schritte auf und zuzuklappen.
 * Dafür würde man normalerweise von JToggleButton ableiten, aber leider hat diese Klasse
 * die Macke, dass man die Hintergrundfarbe im selektierten Zustand nicht individuell festlegen
 * kann. Die wollen wir aber als Indikator verwenden, ob ein Zusammenklappen entwaige Änderungen
 * verbirgt oder nicht. Also basteln wir uns aus einem JButton selber einen Toggle-Button.
 * 
 * @author less02
 */
class KlappButton extends JButton implements ActionListener, MouseMotionListener, MouseListener {
	final FormLayout layout;
	final int klappzeile;
	final KlappbarerBereichI klappbarerBereich;
	final JComponent parent;
	
	public KlappButton(KlappbarerBereichI klappbarerBereich, JComponent parent, FormLayout layout, int klappzeile) {
		super(new ImageIcon("images/minus.png"));
		this.parent = parent;
		this.layout = layout;
		this.klappzeile = klappzeile;
		this.klappbarerBereich = klappbarerBereich;
		setSelectedIcon(new ImageIcon("images/plus.png"));
		setMargin(new Insets(0, 0, 0, 0));
		setOpaque(true);
		hintergrundfarbeVonParentUebernehmen();
		setBorder(new MatteBorder(0, 0, 1, 1, Color.BLACK));
		setBounds(0, 0, getPreferredSize().width, getPreferredSize().height);
		setVisible(false);
		addActionListener(this);
		addMouseListener(this);
		parent.addMouseMotionListener(this);
		parent.add(this);
	}

	public void init(boolean zugeklappt) {
		if (zugeklappt && !isSelected()) {
			doClick();
			setVisible(isSelected());
		}
	}
	
	/**
	 * Diese etwas eigenartige Übernahme der Hintergrundfarbe stellt sicher, dass es nicht
	 * zu unerwarteten Farbeffekten kommt. Ist der Parent nämlich z.B. nur weis, weil die
	 * Farbe aus dem Farbschema des Look & Feels kommt, dann wird der Button auf Basis
	 * dieser Farbe nicht unbedingt in gleicher Farbe angezeigt. Also bauen wir eine neue
	 * Farbe aus der Übernahme der RGB-Werte. Dann klappt es auf jeden Fall
	 */
	private void hintergrundfarbeVonParentUebernehmen() {
		setBackground(new Color(parent.getBackground().getRGB()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setSelected(!isSelected());
		if (isSelected()) {
			boolean zuklappenVerbirgtAenderungen = klappbarerBereich.enthaeltAenderungsmarkierungen();
			if (zuklappenVerbirgtAenderungen) {
				setBackground(Color.yellow);
			}
		}
		else {
			hintergrundfarbeVonParentUebernehmen();
		}
		String benoetigtesZeilenLayout = isSelected() ? AbstractSchrittView.ZEILENLAYOUT_INHALT_VERBORGEN : AbstractSchrittView.ZEILENLAYOUT_INHALT_SICHTBAR;
		layout.setRowSpec(klappzeile, RowSpec.decode(benoetigtesZeilenLayout));
		klappbarerBereich.geklappt(!isSelected());
	}
	
	@Override public void mouseDragged(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e) {
		// Wenn die Sequenz zugeklappt ist, lassen wir den Aufklapp-Button dauerhaft angezeigt
		// Der unbedarfte User erkennt auf diese Weise leichter, wo er drücken muss, um den
		// Inhalt zu sehen. Wenn die Sequenz aufgeklappt ist, zeigen wir den Button nur an,
		// wenn die Maus an der richtigen Stelle steht. Sonst stören im aufgeklappten Zustand
		// die vielen Button-Icons das Erscheinungsbild des Diagramms
		if (!isSelected()) {
			boolean mausUeberKlappenButton = getBounds().contains(e.getPoint());
			setVisible(mausUeberKlappenButton);
		}
	}

	/** Sorgt dafür, dass der Button auch verschwindet, wenn man ihn (und seinen Container)
	 * über den oberen oder linken Rand verlässt. Dann kriegt man nämlich kein mouseMoved
	 * mehr mit der Info, dass die Maus nicht mehr über dem Button steht */
	@Override public void mouseExited(MouseEvent e) {
		if (!isSelected())
			setVisible(false);
	}

	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mousePressed(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	
}
