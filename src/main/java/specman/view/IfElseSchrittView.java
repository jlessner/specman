package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;

import specman.Aenderungsart;
import specman.EditorI;
import specman.SchrittID;
import specman.SpaltenContainerI;
import specman.SpaltenResizer;
import specman.Specman;
import specman.model.v001.IfElseSchrittModel_V001;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.textfield.Indentions;
import specman.textfield.TextfieldShef;

import javax.swing.JPanel;
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
	boolean mittelpunktRaute = true;
	JPanel leeresFeld; 
	JPanel panelBedingung;
	JPanel panelElse;
	JPanel panelIf;
	
	protected IfElseSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id,Aenderungsart anderungsart,  boolean withDefaultContent) {
		super(editor, parent, initialerText, id, anderungsart, createPanelLayout());
		/** @author PVN */
		leeresFeld = new JPanel();
		leeresFeld.setBackground(Specman.schrittHintergrund());
		panelBedingung = new JPanel();
		panelBedingung.setBackground(Specman.schrittHintergrund());
		panelBedingung.setLayout(createSpalteLinks());
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
			initIfSequenz(new ZweigSchrittSequenzView(editor, this, id.naechsteEbene(), aenderungsart, initialtext("Ja")));
			initElseSequenz(new ZweigSchrittSequenzView(editor, this, id.naechsteID().naechsteEbene(), aenderungsart, TextfieldShef.right("Nein")));
		}
	}

	public IfElseSchrittView(EditorI editor, SchrittSequenzView parent, IfElseSchrittModel_V001 model) {
		this(editor, parent, model.inhalt.text, model.id, model.aenderungsart, false);
		initIfSequenz(new ZweigSchrittSequenzView(editor, this, model.ifSequenz));
		initElseSequenz(new ZweigSchrittSequenzView(editor, this, model.elseSequenz));
		setBackground(new Color(model.farbe));
		ifBreitenanteilSetzen(model.ifBreitenanteil);
		klappen.init(model.zugeklappt);
	}

	public IfElseSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id, Aenderungsart aenderungsart) {
		this(editor, parent, initialerText, id, aenderungsart, true);
	}

	protected void initIfSequenz(ZweigSchrittSequenzView pIfSequenz) {
		this.ifSequenz = pIfSequenz;
		ifBedingungAnlegen(ifSequenz);
		panel.add(ifSequenz.getContainer(), CC.xy(1, 5)); /**@author PVN */
	}

	protected void initElseSequenz(ZweigSchrittSequenzView pElseSequenz) {
		this.elseSequenz = pElseSequenz;
		elseBedingungAnlegen(elseSequenz);
		panel.add(elseSequenz.getContainer(), CC.xy(3, 5)); /**@author PVN */
	}

	protected static FormLayout createPanelLayout() {
		return new FormLayout(
				"10px:grow, " + FORMLAYOUT_GAP + ", 10px:grow",
				layoutRowSpec1() + ", " + FORMLAYOUT_GAP + ", fill:pref:grow, " + FORMLAYOUT_GAP + ", " + ZEILENLAYOUT_INHALT_SICHTBAR); /**@author PVN */
	}

	protected void elseBedingungAnlegen(ZweigSchrittSequenzView elseSequenz) {
		elseSequenz.ueberschrift.addFocusListener(this);
		/** @author PVN */
		panelElse = new JPanel();
		panelElse.setBackground(Specman.schrittHintergrund());
		panelElse.setLayout(createSpalteLinks());
		panelElse.add(elseSequenz.ueberschrift.asJComponent(), CC.xywh(2, 1, 1, 1));
		panel.add(panelElse, CC.xywh(3, 3, 1, 1));
	}
	
	protected void ifBedingungAnlegen(ZweigSchrittSequenzView ifSequenz) {
		ifSequenz.ueberschrift.addFocusListener(this);
		/**@author PVN */
		panelIf = new JPanel();
		panelIf.setBackground(Specman.schrittHintergrund());
		panelIf.setLayout(createSpalteRechts());
		panelIf.add(ifSequenz.ueberschrift.asJComponent(), CC.xy(1,1));
		panel.add(panelIf, CC.xy(1, 3));
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

	public SchrittID newStepIDInSameSequence(RelativeStepPosition direction) {
		return super.newStepIDInSameSequence(direction).naechsteID();
	}

	protected Point berechneRautenmittelpunkt() { //umbenannt
		return berechneRautenmittelpunkt(mittelpunktRaute);
	}

	protected Point berechneRautenmittelpunkt(boolean bisUnten) { //umbenannt
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
	
	/** @author PVN */
	public static int spalteUmrechnen(int prozentNeu) {
		int breiteSpaltenLayout = 20*prozentNeu/100; 
		return breiteSpaltenLayout;
	}
	
	@Override
	public void skalieren(int prozentNeu, int prozentAktuell) {
		super.skalieren(prozentNeu, prozentAktuell);
		int neueSpaltenbreite = spalteUmrechnen(prozentNeu); /** @author PVN */
		/**@author SD */
		panelBedingung.setLayout(new FormLayout(neueSpaltenbreite + ", 10px:grow", "fill:pref:grow")); 
		panelElse.setLayout(new FormLayout(neueSpaltenbreite + ", 10px:grow", "fill:pref:grow")); 
		panelIf.setLayout(new FormLayout("10px:grow, " + neueSpaltenbreite, "fill:pref:grow")); 
		panelBedingung.add(text.asJComponent(), CC.xy(2, 1));
		panelElse.add(elseSequenz.ueberschrift.asJComponent(), CC.xy(2, 1));
		panelIf.add(ifSequenz.ueberschrift.asJComponent(), CC.xy(1,1));
	}

	protected int texteinrueckungNeuberechnen() {
		return 0; /**@author PVN */
	}

	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		IfElseSchrittModel_V001 model = new IfElseSchrittModel_V001(
			id,
			getTextMitAenderungsmarkierungen(formatierterText),
			getBackground().getRGB(),
			klappen.isSelected(),
			aenderungsart,
			ifSequenz.generiereZweigSchrittSequenzModel(formatierterText),
			elseSequenz.generiereZweigSchrittSequenzModel(formatierterText),
			ifBreitenanteil(ifSequenz.ueberschrift.getWidth(), elseSequenz.ueberschrift.getWidth()));
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

	@Override
	protected void updateTextfieldDecorationIndentions(Indentions indentions) {
		super.updateTextfieldDecorationIndentions(indentions);
		Indentions ifIndentions = indentions.withRight(false).withTop(false);
		ifSequenz.updateTextfieldDecorationIndentions(ifIndentions);
		Indentions elseIndentions = ifIndentions.withLeft(false);
		elseSequenz.updateTextfieldDecorationIndentions(elseIndentions);
	}

	public ZweigSchrittSequenzView getIfSequenz() {
        return ifSequenz;
    }

    public ZweigSchrittSequenzView getElseSequenz() {
        return elseSequenz;
    }
	public int getRautenHeight() {
		// TODO Auto-generated method stub
		return berechneRautenmittelpunkt().y;
	}


}
