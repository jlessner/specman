package specman.view;

import com.jgoodies.forms.layout.FormLayout;
import specman.EditorI;
import specman.SchrittID;
import specman.SpaltenContainerI;
import specman.Specman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;

/** Basisklasse für If, If/Else und Case */
abstract public class VerzweigungSchrittView extends AbstractSchrittView implements ComponentListener, SpaltenContainerI {
	JPanel panel;
	KlappButton klappen;
	FormLayout panelLayout;

	public VerzweigungSchrittView(EditorI editor, String initialerText, SchrittID id, FormLayout panelLayout) {
		super(editor, initialerText, id);
		this.panelLayout = panelLayout;
		panel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				dreieckUndTrennerZeichnen((Graphics2D)g);
			}
		};
		panel.setBackground(Color.black);
		//createPanelLayout(caseInitialtexte.length);
		panel.setLayout(panelLayout);
		panel.addComponentListener(this);
		
		klappen = new KlappButton(this, text, panelLayout, 4);
		klappen.addComponentListener(new ComponentAdapter() {
			// Kleine Sch�nheitsgeschichte: Der Klapp-Button liegt �ber der linken Dreieckslinie.
			// Wenn der Button durch Mausbewegungen verschwindet, m�ssen wir daf�r sorgen, das dort,
			// wo der Button war, die Linie nachgezogen wird. Sonst bleibt da eine h�ssliche wei�e
			// L�cke stehen.
			@Override public void componentHidden(ComponentEvent e) {
				panel.repaint();
			}
		});
		
	}

	protected static String layoutRowSpec1() {
		int aktuellerZoomfaktor = Specman.instance().zoomFaktor();
		return "fill:[" + (30 * aktuellerZoomfaktor / 100) + "dlu,pref]";
	}
	
	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public boolean isStrukturiert() {
		return true;
	}

	protected void initialeSchritteAnhaengen(EditorI editor) {
		unterSequenzen().forEach(sequenz -> sequenz.einfachenSchrittAnhaengen(editor));
	}
	
	@Override
	public void componentResized(ComponentEvent e) {
		double textEinrueckung = texteinrueckungNeuberechnen();
//		berechneHoeheFuerVollstaendigUnberuehrtenText();
		text.setMargin(new Insets(0, (int)textEinrueckung, 0, (int)textEinrueckung));
		panel.repaint(); // Sorgt daf�r, dass das umplazierte Textfeld und alles andere auf dem Panel sofort neu gezeichnet wird
	}

	@Override public void componentMoved(ComponentEvent e) {}
	@Override public void componentShown(ComponentEvent e) {}
	@Override public void componentHidden(ComponentEvent e) {}

	@Override
	public void focusLost(FocusEvent e) {
		super.focusLost(e);
		panel.repaint(); // Zeichnet Dreieck und Case-Trenner nach, wenn man mit Editieren der Texte fertig ist
	}

	protected Point dreieckUndTrennerZeichnen(Graphics2D g) {
		int breite = panel.getWidth();
		Point dreieckSpitze = berechneDreieckspitze();
		g.setStroke(new BasicStroke(LINIENBREITE));
		g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawLine(0,  0,  dreieckSpitze.x,  dreieckSpitze.y);
		g.drawLine(dreieckSpitze.x,  dreieckSpitze.y, breite, 0);
		
		// Dis folgenden beiden Zeilen stellen sicher, dass *nach* dem Zeichnen des Dreiecks
		// die evt. �ber den Linien liegenden Grafikkomponenten noch einmal gezeichnet werden.
		// Dann werden sie auf jeden Fall nicht von den Linien �berdeckt. Das passiert n�mlich
		// sonst manchmal, wobei ich noch nicht verstanden habe, unter welchen Bedingungen
		// das passiert. Es geht also vmtl. auch eleganter
		klappen.repaint();
		text.repaintSchrittId();
		
		return dreieckSpitze;
	}

	abstract protected Point berechneDreieckspitze();

	abstract protected int texteinrueckungNeuberechnen();
	
}
