package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import specman.EditorI;
import specman.SchrittID;
import specman.SpaltenContainerI;
import specman.SpaltenResizer;
import specman.Specman;
import specman.TextfeldShef;
import specman.model.IfElseSchrittModel;
import specman.model.SchrittModel;

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
	boolean dreieckBisUnten = true;
	
	public IfElseSchrittView(EditorI editor, String initialerText, SchrittID id,
			ZweigSchrittSequenzView pIfSequenz, ZweigSchrittSequenzView pElseSequenz) {
		super(editor, initialerText, id, createPanelLayout());
		this.ifSequenz = pIfSequenz;
		this.elseSequenz = pElseSequenz;
		
		panel.add(text, CC.xywh(1, 1, 3, 1));

		ifBedingungAnlegen(ifSequenz);
		elseBedingungAnlegen(elseSequenz);

		panel.add(ifSequenz.getContainer(), CC.xy(1, 4));
		panel.add(elseSequenz.getContainer(), CC.xy(3, 4));

		panel.add(new SpaltenResizer(this, editor), CC.xy(2, 4));
		
		text.addFocusListener(new FocusAdapter() {
			@Override public void focusLost(FocusEvent e) {
				berechneHoeheFuerVollstaendigUnberuehrtenText();
			}
		});
	}

	public IfElseSchrittView(EditorI editor, IfElseSchrittModel model) {
		this(editor, model.inhalt.text, model.id,
			new ZweigSchrittSequenzView(editor, model.ifSequenz),
			new ZweigSchrittSequenzView(editor, model.elseSequenz));
		setBackground(new Color(model.farbe));
		ifBreitenanteilSetzen(model.ifBreitenanteil);
		klappen.init(model.zugeklappt);
	}

	public IfElseSchrittView(EditorI editor, String initialerText, SchrittID id) {
		this(editor, initialerText, id,
			new ZweigSchrittSequenzView(editor, id.naechsteEbene(), initialtext("Ja")),
			new ZweigSchrittSequenzView(editor, id.naechsteID().naechsteEbene(), TextfeldShef.right("Nein")));
	}
	
	protected static FormLayout createPanelLayout() {
		return new FormLayout(
				"10px:grow, " + FORMLAYOUT_GAP + ", 10px:grow",
				layoutRowSpec1() + ", fill:pref, " + FORMLAYOUT_GAP + ", " + ZEILENLAYOUT_INHALT_SICHTBAR);
	}

	protected void elseBedingungAnlegen(ZweigSchrittSequenzView elseSequenz) {
		elseSequenz.ueberschrift.addFocusListener(this);
		panel.add(elseSequenz.ueberschrift, CC.xywh(2, 2, 2, 1));
		elseSequenz.ueberschrift.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				textueberschneidungenMitDreiecksliniePruefen();
			}
		});
	}

	private void textueberschneidungenMitDreiecksliniePruefen() {
		Point dreieckSpitze = berechneDreieckspitze(true);
		Line2D.Double abfallendeLinie = new Line2D.Double
				(0, 0, dreieckSpitze.getX(), dreieckSpitze.getY());
		Line2D.Double aufsteigendeLinie = new Line2D.Double
				(dreieckSpitze.getX(), dreieckSpitze.getY(), panel.getWidth(), 0);
		boolean volleBreiteBenoetigt =
			textUeberschneidetDreieckslinie(elseSequenz.ueberschrift.getLinkeZeilenraender(), elseSequenz.ueberschrift.getBounds(), aufsteigendeLinie) ||
			textUeberschneidetDreieckslinie(ifSequenz.ueberschrift.getRechteZeilenraender(), ifSequenz.ueberschrift.getBounds(), abfallendeLinie);
;
		layoutAnTexteFuerIfElseBedingungenAnpassen(volleBreiteBenoetigt);
	}

	private boolean textUeberschneidetDreieckslinie(List<Line2D.Double> textRaender, Rectangle offset, Line2D.Double dreieckslinie) {
		for (Line2D.Double rand: textRaender) {
			// Randlinie des Textfelds auf die Koordinates des Panels umrechnen und bis zum unteren Rand des Kopfbereichs verl�ngern
			Line2D.Double senkrechte = new Line2D.Double(
					rand.x1 + offset.getX(),
					rand.y1 + offset.getY(),
					rand.x2 + offset.getX(),
					panel.getHeight());
			if (senkrechte.intersectsLine(dreieckslinie)) {
				return true;
			}
		}
		return false;
	}
	
	private void layoutAnTexteFuerIfElseBedingungenAnpassen(boolean volleBreiteBenoetigt) {
		dreieckBisUnten = !volleBreiteBenoetigt;
		panelLayout.setConstraints(elseSequenz.ueberschrift,
				volleBreiteBenoetigt ? CC.xywh(3, 2, 1, 1) : CC.xywh(2, 2, 2, 1));
		Specman.instance().diagrammAktualisieren(null);
	}
	
	protected void ifBedingungAnlegen(ZweigSchrittSequenzView ifSequenz) {
		ifSequenz.ueberschrift.addFocusListener(this);
		panel.add(ifSequenz.ueberschrift, CC.xy(1, 2));
		ifSequenz.ueberschrift.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				textueberschneidungenMitDreiecksliniePruefen();
			}
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

	private void ifBreitenanteilSetzen(float ifBreitenanteil) {
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

	@Override
	public SchrittID folgeIDInGleicherSequenz() {
		return super.folgeIDInGleicherSequenz().naechsteID();
	}

	protected Point berechneDreieckspitze() {
		return berechneDreieckspitze(dreieckBisUnten);
	}

	protected Point berechneDreieckspitze(boolean bisUnten) {
		return new Point(
				ifSequenz.getContainer().getWidth(),
				ifSequenz.ueberschrift.getY() + (bisUnten ? ifSequenz.ueberschrift.getHeight() : 0));
	}

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
		return ifSequenz.ueberschrift.getWidth() / 2;
	}

	@Override
	public SchrittModel generiereModel(boolean formatierterText) {
		IfElseSchrittModel model = new IfElseSchrittModel();
		model.id = id;
		model.inhalt = getTextMitAenderungsmarkierungen(formatierterText);
		model.farbe = getBackground().getRGB();
		model.ifSequenz = ifSequenz.generiereZweigSchrittSequenzModel(formatierterText);
		model.elseSequenz = elseSequenz.generiereZweigSchrittSequenzModel(formatierterText);
		model.ifBreitenanteil = ifBreitenanteil(ifSequenz.ueberschrift.getWidth(), elseSequenz.ueberschrift.getWidth());
		model.zugeklappt = klappen.isSelected();
		return model;
	}
	
	/**
	 * Wenn der Text etwas gr��er ist, neigen die Dreieckslinien dazu, die Textbox zu schneiden. Also
	 * rechnen wir aus, wie hoch der Kopfbereich sein muss, damit das nicht passiert. Und das geht so:
	 * <ol>
	 * <li>Wir stellen die unteren beiden Eckpunkte des Textfeldes fest
	 * <li>Wir berechnen den Winkel, der sich ergibt, wenn man eine Linie von den oberen Bereichsecken
	 *   zu den unteren Textfeldecken zieht.
	 * <li>Anschlie�end suchen wir den Punkt, in dem sich diese beiden Diagonalen schneiden, wenn man
	 *   sie nach unten verl�ngert.
	 * <li>Die H�he dieses Punktes ist die Minimalh�he des Kopfbereiches, damit die Dreieckslinie den
	 *   Text nicht schneidet.
	 * <li>Danach m�ssen wir schauen, ob dieser Punkt tief genug liegt, um If- und Else-Bedingung gen�gend
	 *   Platz zu lassen. Ist das nicht der Fall, rechnen wir die Differenz noch dazu. �ber diese
	 *   Strecke muss dann noch eine senkrechte Verbindungslinie von der Dreiecksspitze bis zur Basis des
	 *   Kofbereichs gezogen werden. Das sieht gef�lliger aus, als wenn man den Kopfbereich noch tiefer
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

	
}
