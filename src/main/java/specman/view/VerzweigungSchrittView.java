package specman.view;

import com.jgoodies.forms.layout.FormLayout;

import specman.Aenderungsart;
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

	public VerzweigungSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id, Aenderungsart aenderungsart, FormLayout panelLayout) {
		super(editor, parent, initialerText, id, aenderungsart);
		this.panelLayout = panelLayout;
		panel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				rauteZeichnen((Graphics2D)g);
				restoreDecorationGraphics(g);
			}
		};

		panel.setBackground(Color.black);
		//createPanelLayout(caseInitialtexte.length);
		panel.setLayout(panelLayout);
		panel.addComponentListener(this);
		panel.setEnabled(false);
		// TODO JL: Die Platzierung des Klappbuttons muss neu gebaut werden. Weder das Textfeld noch
		// das Hauptpanel sind geeignet, weil diese ein Formlayout haben. Allerdings dürfte sich das
		// Problem erledigen, wenn die Aktogramm-Darstellung kommt. Dann ist links oben gar kein Text
		// mehr und somit Platz für den Klapp-Button.
		klappen = new KlappButton(this, text.getTextComponent(), panelLayout, 4);
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

	/** If the step is placed into a {@link RoundedBorderDecorator}, painting the step
	 * sometimes causes a corruption of the rounded corners. So we repaint the decorator
	 * afterwards. However: we can't just call its repaint method which causes a
	 * <i>recursive</i> repaint and thus an endless loop. So we only run a decoration
	 * redraw with an appropriate translated graphics context. */
	private void restoreDecorationGraphics(Graphics g) {
		if (roundedBorderDecorator != null) {
			g.translate(-panel.getX(), -panel.getY());
			roundedBorderDecorator.drawDecoration(g);
		}
	}

	protected static String layoutRowSpec1() {
		int aktuellerZoomfaktor = Specman.instance().zoomFaktor();
		return "fill:[" + (1 * aktuellerZoomfaktor / 100) + "dlu,pref]"; /**@author PVN */
	}
	
	@Override
	public JComponent getComponent() { return decorated(panel); }

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
		text.setLeftInset((int)textEinrueckung);
		text.setRightInset((int)textEinrueckung);
		panel.repaint(); // Sorgt dafür, dass das umplazierte Textfeld und alles andere auf dem Panel sofort neu gezeichnet wird
	}

	@Override public void componentMoved(ComponentEvent e) {}
	@Override public void componentShown(ComponentEvent e) {}
	@Override public void componentHidden(ComponentEvent e) {}

	@Override
	public void focusLost(FocusEvent e) {
		super.focusLost(e);
		panel.repaint(); // Zeichnet Dreieck und Case-Trenner nach, wenn man mit Editieren der Texte fertig ist
	}
	
	/** @author PVN */ 
	public static FormLayout createSpalteLinks() {
		return new FormLayout(breiteLayoutspalteBerechnen() + ", 10px:grow", "fill:pref:grow");
	}
	
	/** @author PVN */
	public static FormLayout createSpalteRechts() {
		return new FormLayout("10px:grow, " + breiteLayoutspalteBerechnen(), "fill:pref:grow");
	}
	
	/** @author PVN */
	public static double breiteLayoutspalteBerechnen() {
		double breiteSpaltenLayout = 20*Specman.instance().getZoomFactor()/100; 
		return breiteSpaltenLayout;
	}

	protected Point rauteZeichnen(Graphics2D g) { //umbenannt
		Point mittelpunktRaute = berechneRautenmittelpunkt(); //umbenannt
		/** @author PVN */ 
		int[] polygonXinnen = {(mittelpunktRaute.x-(int)breiteLayoutspalteBerechnen()), mittelpunktRaute.x+1, (mittelpunktRaute.x+2+(int)breiteLayoutspalteBerechnen()), mittelpunktRaute.x+1};
		int[] polygonYinnen = {text.getHeight()+1, (text.getHeight()-1-(int)breiteLayoutspalteBerechnen()), text.getHeight()+1, (text.getHeight()+1+(int)breiteLayoutspalteBerechnen())}; /** @author PVN, SD */
		g.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fillPolygon(polygonXinnen, polygonYinnen, 4); //innere weisse Raute, ausgefuellt
		
		int[] polygonXaussen = {(mittelpunktRaute.x-(int)breiteLayoutspalteBerechnen()), mittelpunktRaute.x+1, (mittelpunktRaute.x+2+(int)breiteLayoutspalteBerechnen()), mittelpunktRaute.x+1};
		int[] polygonYausssen = {text.getHeight()+1, (text.getHeight()-1-(int)breiteLayoutspalteBerechnen()), text.getHeight()+1, (text.getHeight()+1+(int)breiteLayoutspalteBerechnen()+1)}; /** @author PVN, SD */ 
		g.setStroke(new BasicStroke(LINIENBREITE));
		g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.drawPolygon(polygonXaussen, polygonYausssen, 4); //aeussere schwarze Raute, nicht ausgefuellt
		
		
		// Dis folgenden beiden Zeilen stellen sicher, dass *nach* dem Zeichnen des Dreiecks
		// die evt. �ber den Linien liegenden Grafikkomponenten noch einmal gezeichnet werden.
		// Dann werden sie auf jeden Fall nicht von den Linien �berdeckt. Das passiert n�mlich
		// sonst manchmal, wobei ich noch nicht verstanden habe, unter welchen Bedingungen
		// das passiert. Es geht also vmtl. auch eleganter
		klappen.repaint();
		text.repaintSchrittId();
		
		return mittelpunktRaute;
	}
	
	abstract protected Point berechneRautenmittelpunkt(); //umbenannt

	abstract protected int texteinrueckungNeuberechnen();
	
	//TODO get Panel
	public JPanel getPanel() {
		return panel;
	}
}
