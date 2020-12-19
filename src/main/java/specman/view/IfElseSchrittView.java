package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import specman.EditorI;
import specman.SchrittID;
import specman.SpaltenContainerI;
import specman.SpaltenResizer;
import specman.Specman;
import specman.model.v001.IfElseSchrittModel_V001;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.textfield.Indentions;
import specman.textfield.TextfieldShef;

import javax.swing.JPanel; //neu
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Line2D;
import java.util.List;

import static specman.Specman.initialtext;

public class IfElseSchrittView extends VerzweigungSchrittView implements ComponentListener, SpaltenContainerI {
	ZweigSchrittSequenzView ifSequenz;
	ZweigSchrittSequenzView elseSequenz;
	
	boolean MittelPunktRaute = true;
//	boolean dreieckBisUnten = true;
	
	protected IfElseSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id, boolean withDefaultContent) {
		super(editor, parent, initialerText, id, createPanelLayout());
		/** @author PVN */
		JPanel leeresFeld = new JPanel(); 
		leeresFeld.setBackground(Color.WHITE);
		panel.add(leeresFeld, CC.xywh(1, 1, 1, 1));
		/** @author PVN */		
		JPanel panelBedingung = new JPanel(); 
		panelBedingung.setBackground(Color.WHITE);
		panelBedingung.setLayout(new FormLayout(20 * Specman.instance().getZoomFactor()/100 + ", 10px:grow", "fill:pref"));
		panelBedingung.add(text.asJComponent(), "2,1");
		panel.add(panelBedingung, CC.xywh(3, 1, 1, 1));
		panel.add(leeresFeld, CC.xywh(1, 1, 1, 1));
		panel.add(new SpaltenResizer(this, editor), CC.xy(2, 5));
		text.addFocusListener(new FocusAdapter() {
			@Override public void focusLost(FocusEvent e) {
				berechneHoeheFuerVollstaendigUnberuehrtenText();
			}
		});
		if(withDefaultContent) {
			initIfSequenz(new ZweigSchrittSequenzView(editor, this, id.naechsteEbene(), initialtext("Ja")));
			initElseSequenz(new ZweigSchrittSequenzView(editor, this, id.naechsteID().naechsteEbene(), TextfieldShef.right("Nein")));
		}
	}
	

	public IfElseSchrittView(EditorI editor, SchrittSequenzView parent, IfElseSchrittModel_V001 model) {
		this(editor, parent, model.inhalt.text, model.id, false);
		initIfSequenz(new ZweigSchrittSequenzView(editor, this, model.ifSequenz));
		initElseSequenz(new ZweigSchrittSequenzView(editor, this, model.elseSequenz));
		setBackground(new Color(model.farbe));
		ifBreitenanteilSetzen(model.ifBreitenanteil);
		klappen.init(model.zugeklappt);
	}

	public IfElseSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id) {
		this(editor, parent, initialerText, id, true);
	}

	protected void initIfSequenz(ZweigSchrittSequenzView pIfSequenz) {
		this.ifSequenz = pIfSequenz;
		ifBedingungAnlegen(ifSequenz);
//		panel.add(ifSequenz.getContainer(), CC.xy(1, 4));
		/** @author PVN */
		panel.add(ifSequenz.getContainer(), CC.xy(1, 5)); 
	}

	protected void initElseSequenz(ZweigSchrittSequenzView pElseSequenz) {
		this.elseSequenz = pElseSequenz;
		elseBedingungAnlegen(elseSequenz);
//		panel.add(elseSequenz.getContainer(), CC.xy(3, 4));
		/** @author PVN */
		panel.add(elseSequenz.getContainer(), CC.xy(3, 5)); 
	}

	protected static FormLayout createPanelLayout() {
		return new FormLayout(
				"10px:grow, " + FORMLAYOUT_GAP + ", 10px:grow",
//				layoutRowSpec1() + ", fill:pref, " + FORMLAYOUT_GAP + ", " + ZEILENLAYOUT_INHALT_SICHTBAR);
				/** @author PVN */
				layoutRowSpec1() + ", " + FORMLAYOUT_GAP + ", fill:pref:grow, " + FORMLAYOUT_GAP + ", " + ZEILENLAYOUT_INHALT_SICHTBAR);
	}

	protected void elseBedingungAnlegen(ZweigSchrittSequenzView elseSequenz) {
		elseSequenz.ueberschrift.addFocusListener(this);
		/** @author PVN */
		JPanel panelElse = new JPanel(); 
		panelElse.setBackground(Color.WHITE);
		panelElse.setLayout(new FormLayout(20 * Specman.instance().getZoomFactor()/100 + ", 10px:grow", "fill:pref:grow"));
		panelElse.add(elseSequenz.ueberschrift.asJComponent(), CC.xywh(2, 1, 1, 1));
		panel.add(panelElse, CC.xywh(3, 3, 1, 1));  
		elseSequenz.ueberschrift.addFocusListener(new FocusAdapter() {
//			@Override
//			public void focusLost(FocusEvent e) {
//				textueberschneidungenMitDreiecksliniePruefen();
//			}
		});
	}

//	private void textueberschneidungenMitDreiecksliniePruefen() {
//		Point dreieckSpitze = berechneDreieckspitze(true);
//
//		
//		Line2D.Double abfallendeLinie = new Line2D.Double
//				(0, 0, dreieckSpitze.getX(), dreieckSpitze.getY());
//		Line2D.Double aufsteigendeLinie = new Line2D.Double
//				(dreieckSpitze.getX(), dreieckSpitze.getY(), panel.getWidth(), 0);
//		boolean volleBreiteBenoetigt =
//			textUeberschneidetDreieckslinie(elseSequenz.ueberschrift.getLinkeZeilenraender(), elseSequenz.ueberschrift.getBounds(), aufsteigendeLinie) ||
//			textUeberschneidetDreieckslinie(ifSequenz.ueberschrift.getRechteZeilenraender(), ifSequenz.ueberschrift.getBounds(), abfallendeLinie);
//;
//		layoutAnTexteFuerIfElseBedingungenAnpassen(volleBreiteBenoetigt);
//	}

//	private boolean textUeberschneidetDreieckslinie(List<Line2D.Double> textRaender, Rectangle offset, Line2D.Double dreieckslinie) {
//		for (Line2D.Double rand: textRaender) {
//			// Randlinie des Textfelds auf die Koordinates des Panels umrechnen und bis zum unteren Rand des Kopfbereichs verlï¿½ngern
//			Line2D.Double senkrechte = new Line2D.Double(
//					rand.x1 + offset.getX(),
//					rand.y1 + offset.getY(),
//					rand.x2 + offset.getX(),
//					panel.getHeight());
//			if (senkrechte.intersectsLine(dreieckslinie)) {
//				return true;
//			}
//		}
//		return false;
//	}
	
	private void layoutAnTexteFuerIfElseBedingungenAnpassen(boolean volleBreiteBenoetigt) {
		MittelPunktRaute = !volleBreiteBenoetigt;

//		dreieckBisUnten = !volleBreiteBenoetigt;
		panelLayout.setConstraints(elseSequenz.ueberschrift.asJComponent(),
//				volleBreiteBenoetigt ? CC.xywh(3, 2, 1, 1) : CC.xywh(2, 2, 2, 1));
				/** @author PVN */
				volleBreiteBenoetigt ? CC.xywh(3, 1, 1, 1) : CC.xywh(2, 2, 1, 1));
		Specman.instance().diagrammAktualisieren(null);
	}
	
	protected void ifBedingungAnlegen(ZweigSchrittSequenzView ifSequenz) {
		ifSequenz.ueberschrift.addFocusListener(this);
		/** @author PVN */
		JPanel panelIf = new JPanel(); 
		panelIf.setBackground(Color.WHITE);
		panelIf.setLayout(new FormLayout("10px:grow, " + 20 * Specman.instance().getZoomFactor()/100, "fill:pref:grow"));
		panelIf.add(ifSequenz.ueberschrift.asJComponent(), CC.xy(1,1));
		panel.add(panelIf, CC.xy(1, 3));
		ifSequenz.ueberschrift.addFocusListener(new FocusAdapter() {
//			@Override
//			public void focusLost(FocusEvent e) {
//				textueberschneidungenMitDreiecksliniePruefen();
//			}
		});
	}

	@Override
	public int spaltenbreitenAnpassenNachMausDragging(int vergroesserung, int spalte) {
		float angepassteIfBreite = ifSequenz.ueberschrift.getWidth() + vergroesserung;
		float angepassteElseBreite = elseSequenz.ueberschrift.getWidth() - vergroesserung;
		float angepassterIfBreitenanteil = ifBreitenanteil(angepassteIfBreite, angepassteElseBreite);
		ifBreitenanteilSetzen(angepassterIfBreitenanteil);
		Specman.instance().diagrammAktualisieren(null);
		return vergroesserung;
	}
	
	private float ifBreitenanteil(float ifBreite, float elseBreite) {
		return ifBreite / (ifBreite + elseBreite);
	}

	private void ifBreitenanteilSetzen(float ifBreitenanteil) { //in () % Anteil für die Breite 
		float elseBreitenanteil = 1.0f - ifBreitenanteil;
		panelLayout.setColumnSpec(1, ColumnSpec.decode("10px:grow(" + ifBreitenanteil + ")"));
		panelLayout.setColumnSpec(3, ColumnSpec.decode("10px:grow(" + elseBreitenanteil + ")"));
		componentResized(null);
	}

	@Override
	public void setId(SchrittID id) {
		super.setId(id);
		SchrittID ifID = id.naechsteEbene();
		SchrittID elseID = id.naechsteID().naechsteEbene();
		ifSequenz.renummerieren(ifID);
		elseSequenz.renummerieren(elseID);
	}

	public SchrittID newStepIDInSameSequence(RelativeStepPosition direction) {
		return super.newStepIDInSameSequence(direction).naechsteID();
	}

	protected Point berechneMittelPunktfuerRaute() {
		return berechneMittelPunktfuerRaute(MittelPunktRaute);
	}

//	protected Point berechneDreieckspitze() {
//		return berechneDreieckspitze(dreieckBisUnten);
//	}

	
	protected Point berechneMittelPunktfuerRaute(boolean bisUnten) {
		return new Point(
				ifSequenz.getContainer().getWidth(),
				ifSequenz.ueberschrift.getY() + (bisUnten ? ifSequenz.ueberschrift.getHeight() : 0));
	}

//	protected Point berechneDreieckspitze(boolean bisUnten) {
//		return new Point(
//				ifSequenz.getContainer().getWidth(),
//				ifSequenz.ueberschrift.getY() + (bisUnten ? ifSequenz.ueberschrift.getHeight() : 0));
//	}
//	
	
	
	
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		ifSequenz.ueberschrift.setBackground(bg);
		elseSequenz.ueberschrift.setBackground(bg);
		panel.repaint(); // Damit die Linien nachgezeichnet werden
	}

	@Override
	void schrittnummerSichtbarkeitSetzen(boolean sichtbar) {
		super.schrittnummerSichtbarkeitSetzen(sichtbar);
		ifSequenz.schrittnummerSichtbarkeitSetzen(sichtbar);
		elseSequenz.schrittnummerSichtbarkeitSetzen(sichtbar);
	}

	
	@Override
	protected List<SchrittSequenzView> unterSequenzen() {
		return sequenzenAuflisten(ifSequenz, elseSequenz);
	}

	@Override
	public void geklappt(boolean auf) {
		ifSequenz.setVisible(auf);
		elseSequenz.setVisible(auf);
	}

	@Override
	public void zusammenklappenFuerReview() {
		if (!enthaeltAenderungsmarkierungen()) {
			klappen.init(true);
		}
		ifSequenz.zusammenklappenFuerReview();
		elseSequenz.zusammenklappenFuerReview();
	}
	
	@Override
	public void skalieren(int prozentNeu, int prozentAktuell) {
		super.skalieren(prozentNeu, prozentAktuell);
	}

	protected int texteinrueckungNeuberechnen() {
//		return ifSequenz.ueberschrift.getWidth() / 2;
		return 0; 
	}

	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		IfElseSchrittModel_V001 model = new IfElseSchrittModel_V001(
			id,
			getTextMitAenderungsmarkierungen(formatierterText),
			getBackground().getRGB(),
			klappen.isSelected(),
			ifSequenz.generiereZweigSchrittSequenzModel(formatierterText),
			elseSequenz.generiereZweigSchrittSequenzModel(formatierterText),
			ifBreitenanteil(ifSequenz.ueberschrift.getWidth(), elseSequenz.ueberschrift.getWidth()));
		return model;
	}
	
	/**
	 * Wenn der Text etwas grï¿½ï¿½er ist, neigen die Dreieckslinien dazu, die Textbox zu schneiden. Also
	 * rechnen wir aus, wie hoch der Kopfbereich sein muss, damit das nicht passiert. Und das geht so:
	 * <ol>
	 * <li>Wir stellen die unteren beiden Eckpunkte des Textfeldes fest
	 * <li>Wir berechnen den Winkel, der sich ergibt, wenn man eine Linie von den oberen Bereichsecken
	 *   zu den unteren Textfeldecken zieht.
	 * <li>Anschlieï¿½end suchen wir den Punkt, in dem sich diese beiden Diagonalen schneiden, wenn man
	 *   sie nach unten verlï¿½ngert.
	 * <li>Die Hï¿½he dieses Punktes ist die Minimalhï¿½he des Kopfbereiches, damit die Dreieckslinie den
	 *   Text nicht schneidet.
	 * <li>Danach mï¿½ssen wir schauen, ob dieser Punkt tief genug liegt, um If- und Else-Bedingung genï¿½gend
	 *   Platz zu lassen. Ist das nicht der Fall, rechnen wir die Differenz noch dazu. ï¿½ber diese
	 *   Strecke muss dann noch eine senkrechte Verbindungslinie von der Dreiecksspitze bis zur Basis des
	 *   Kofbereichs gezogen werden. Das sieht gefï¿½lliger aus, als wenn man den Kopfbereich noch tiefer
	 *   macht.
	 * </ol>
	 */
	private void berechneHoeheFuerVollstaendigUnberuehrtenText() {
		
	}

	@Override
	public boolean enthaelt(JTextComponent textComponent) {
		return super.enthaelt(textComponent) ||
			ifSequenz.hatUeberschrift(textComponent) ||
			elseSequenz.hatUeberschrift(textComponent);
	}

	@Override
	protected void updateTextfieldDecorationIndentions(Indentions indentions) {
		super.updateTextfieldDecorationIndentions(indentions);
		Indentions ifIndentions = indentions.withRight(false).withTop(false);
		ifSequenz.updateTextfieldDecorationIndentions(ifIndentions);
		Indentions elseIndentions = ifIndentions.withLeft(false);
		elseSequenz.updateTextfieldDecorationIndentions(elseIndentions);
	}
}
