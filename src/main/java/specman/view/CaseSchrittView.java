package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import specman.Aenderungsart;
import specman.EditorI;
import specman.SchrittID;
import specman.SpaltenResizer;
import specman.Specman;
import specman.model.v001.CaseSchrittModel_V001;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.ZweigSchrittSequenzModel_V001;
import specman.textfield.Indentions;
import specman.textfield.TextfieldShef;

import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static specman.Specman.initialtext;


public class CaseSchrittView extends VerzweigungSchrittView implements ComponentListener, FocusListener {
	// Alle Components kriegen am Anfang einen sinnfreien Dummy und werden dann
	// über die Methode layoutCojnstraintsSetzen korrekt plaziert. Damit verhindern
	// wir, dass die relativ komplizierte Positionsvergabe redundant im Code auftaucht
	private static final CellConstraints INITIAL_DUMMY = CC.xy(1, 1);
	
	ZweigSchrittSequenzView sonstSequenz;
	List<ZweigSchrittSequenzView> caseSequenzen;
	JPanel lueckenFueller; //neu
	JPanel panelCase; //neu
	JPanel panelSonst; //neu
	JPanel panelFall1; //neu

	public CaseSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id, Aenderungsart aenderungsart, int numCases) {
		super(editor, parent, initialerText, id, aenderungsart, createPanelLayout(numCases));
		panel.add(text.asJComponent(), INITIAL_DUMMY);
		/** @author PVN */
		lueckenFueller = new JPanel();
		lueckenFueller.setBackground(Specman.schrittHintergrund());
		panel.add(lueckenFueller, CC.xy(1, 1));

		panelCase = new JPanel();
		panelCase.setBackground(Specman.schrittHintergrund());
		panelCase.setLayout(createSpalteLinks());
		panel.add(panelCase, CC.xy(3, 1));

		panelSonst = new JPanel();
		panelSonst.setBackground(Specman.schrittHintergrund());
		panelSonst.setLayout(createSpalteRechts());
		panel.add(panelSonst, CC.xy(1, 3));

		panelFall1 = new JPanel();
		panelFall1.setBackground(Specman.schrittHintergrund());
		panelFall1.setLayout(createSpalteLinks());
		panel.add(panelFall1, CC.xy(3, 3));
	}

	protected void initCases(EditorI editor, ZweigSchrittSequenzView sonstSequenz, List<ZweigSchrittSequenzView> caseSequenzen) {
		sonstFallAnlegen(sonstSequenz);
		casesAnlegen(caseSequenzen);
		layoutConstraintsSetzen();
		spaltenResizerAnlegen(editor);
	}

	public CaseSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id, Aenderungsart aenderungsart) {
		this(editor, parent, initialerText, id, aenderungsart, 2);
		initCases(
				editor,
				new ZweigSchrittSequenzView(editor, this, id.naechsteEbene(), aenderungsart, initialtext("Sonst")),
				new ArrayList<ZweigSchrittSequenzView>(Arrays.asList(
						new ZweigSchrittSequenzView(editor, this, id.naechsteID().naechsteEbene(), aenderungsart, initialtext("Fall 1")),
						new ZweigSchrittSequenzView(editor, this, id.naechsteID().naechsteID().naechsteEbene(), aenderungsart, initialtext("Fall 2")))));
	}
	
	private List<ZweigSchrittSequenzView> caseSequenzenAufbauen(EditorI editor, List<ZweigSchrittSequenzModel_V001> model) {
		return model.stream()
				.map(sequenzModel -> new ZweigSchrittSequenzView(editor, this, sequenzModel))
				.collect(Collectors.toList());
	}
	
	public CaseSchrittView(EditorI editor, SchrittSequenzView parent, CaseSchrittModel_V001 model) {
		this(editor, parent, model.inhalt.text, model.id, model.aenderungsart, model.caseSequenzen.size());
		initCases(
				editor,
				new ZweigSchrittSequenzView(editor, this, model.sonstSequenz),
				caseSequenzenAufbauen(editor, model.caseSequenzen));
		setBackground(new Color(model.farbe));
		spaltenbreitenAnteileSetzen(model.spaltenbreitenAnteile);
		klappen.init(model.zugeklappt);
	}

	private void spaltenResizerAnlegen(EditorI editor) {
		for (int i = 0; i < caseSequenzen.size(); i++) {
			panel.add(new SpaltenResizer(this, i, editor), CC.xywh(2 + 2*i, 4, 1, 2)); /**@author PVN */
		}
	}
	
	private void spaltenResizerEntfernen() {
		for (MouseListener ml: panel.getMouseListeners()) {
			if (ml instanceof SpaltenResizer)
				panel.removeMouseListener(ml);
		}
	}
	/** @author PVN */
	private static FormLayout createPanelLayout(int anzahlCases) {
		String spaltenSpec = "10px:grow, " + FORMLAYOUT_GAP + ", 10px:grow, " + FORMLAYOUT_GAP;
		for (int i = 2; i < anzahlCases; i++)
			spaltenSpec += ", 10px:grow, " + FORMLAYOUT_GAP;
		spaltenSpec += ", 10px:grow";
		return new FormLayout(spaltenSpec,
				layoutRowSpec1() + ", " + FORMLAYOUT_GAP + ", fill:pref, " + FORMLAYOUT_GAP + ", " + ZEILENLAYOUT_INHALT_SICHTBAR);
	}

	/** @author PVN */
	public static int spalteUmrechnen(int prozentNeu) {
		int breiteSpaltenLayout = 20*prozentNeu/100;
		return breiteSpaltenLayout;
	}

	@Override
	public void skalieren(int prozentNeu, int prozentAktuell) {
		super.skalieren(prozentNeu, prozentAktuell);
		// Wenn der Kopftext einzeilig ist, bleibt der entsprechende Bereich sehr schmal und die
		// flachen Dreieckslinien ragen rechts und linke initial weit in den Text hinein. Deswegen
		// w�hlen wir das Layout so, dass die Layoutzeile so gro� wird, wie das Textfeld Platz
		// braucht, jedoch mindestens 30 dlu, multipliziert mit dem aktuellen Zoomfaktor.
		// Cool, was das FormLayout so alles kann ;-)
		// Syntaxtricks von hier: http://manual.openestate.org/extern/forms-1.2.1/reference/variables.html
		panelLayout.setRowSpec(1, RowSpec.decode(layoutRowSpec1()));
		int neueSpaltenbreite = spalteUmrechnen(prozentNeu); /** @author PVN */
		panelCase.setLayout(new FormLayout(neueSpaltenbreite + ", 10px:grow", "fill:pref:grow")); /**@author SD */
		panelSonst.setLayout(new FormLayout("10px:grow, " + neueSpaltenbreite, "fill:pref:grow")); /**@author SD*/
		panelFall1.setLayout(new FormLayout(neueSpaltenbreite + ", 10px:grow", "fill:pref:grow")); /**@author SD*/ 
		panelCase.add(text.asJComponent(), CC.xy(2, 1)); //siehe Methode layoutConstraintsSetzen
		panelSonst.add(sonstSequenz.ueberschrift.asJComponent(), CC.xy(1, 1)); //siehe Methode layoutConstraintsSetzen
		panelFall1.add(caseSequenzen.get(0).ueberschrift.asJComponent(), CC.xy(2, 1)); //siehe Methode layoutConstraintsSetzen
	}

	@Override
	public SchrittID newStepIDInSameSequence(RelativeStepPosition direction) {
		SchrittID id = super.newStepIDInSameSequence(direction);
		for (int i = 0; i < caseSequenzen.size(); i++)
			id = id.naechsteID();
		return id;
	}

	@Override
	public void setId(SchrittID id) {
		super.setId(id);
		sonstSequenz.renummerieren(id.naechsteEbene());
		SchrittID naechsteId = id.naechsteID();
		for (SchrittSequenzView caseSequenz: caseSequenzen) {
			caseSequenz.renummerieren(naechsteId.naechsteEbene());
			naechsteId = naechsteId.naechsteID();
		}
	}

	protected void sonstFallAnlegen(ZweigSchrittSequenzView sonstSequenz) {
		this.sonstSequenz = sonstSequenz;
		sonstSequenz.ueberschrift.addFocusListener(this);
		panel.add(sonstSequenz.ueberschrift.asJComponent(), INITIAL_DUMMY);
		panel.add(sonstSequenz.getContainer(), INITIAL_DUMMY);
	}

	private void casesAnlegen(List<ZweigSchrittSequenzView> caseSequenzen) {
		this.caseSequenzen = caseSequenzen;
		for (int i = 0; i < caseSequenzen.size(); i++) {
			caseSequenzen.get(i).ueberschrift.addFocusListener(this);
			panel.add(caseSequenzen.get(i).ueberschrift.asJComponent(), INITIAL_DUMMY);
			panel.add(caseSequenzen.get(i).getContainer(), INITIAL_DUMMY);
		}
	}

	protected int texteinrueckungNeuberechnen() {
		return 0; /**@author PVN */
	}

	protected Point berechneRautenmittelpunkt() { //umbenannt
		return new Point(
				sonstSequenz.getContainer().getWidth(),
				sonstSequenz.ueberschrift.getY());
	}

	@Override
	public int spaltenbreitenAnpassenNachMausDragging(int vergroesserung, int spalte) {
		ArrayList<Integer> spaltenBreiten = spaltenbreitenErmitteln();
		if (spaltenBreiten.get(spalte) + vergroesserung < 0) {
			vergroesserung = -spaltenBreiten.get(spalte);
		}
		else if (spaltenBreiten.get(spalte+1) - vergroesserung < 0) {
			vergroesserung = spaltenBreiten.get(spalte+1);
		}
		spaltenBreiten.set(spalte, spaltenBreiten.get(spalte) + vergroesserung);
		spaltenBreiten.set(spalte+1, spaltenBreiten.get(spalte+1) - vergroesserung);
		List<Float> breitenAnteile = spaltenbreitenAnteileBerechnen(spaltenBreiten);
		spaltenbreitenAnteileSetzen(breitenAnteile);
		Specman.instance().diagrammAktualisieren(null);
		return vergroesserung;
	}
	
	private void spaltenbreitenAnteileSetzen(List<Float> breitenAnteile) {
		for (int i = 0; i < breitenAnteile.size(); i++) {
			panelLayout.setColumnSpec(1 + 2*i, ColumnSpec.decode("0px:grow(" + breitenAnteile.get(i) + ")"));
		}
	}

	private ArrayList<Integer> spaltenbreitenErmitteln() {
		ArrayList<Integer> spaltenBreiten = new ArrayList<Integer>();
		spaltenBreiten.add(sonstSequenz.getContainer().getWidth());
		caseSequenzen.forEach(sequenz -> spaltenBreiten.add(sequenz.getContainer().getWidth()));
		return spaltenBreiten;
	}

	private List<Float> spaltenbreitenAnteileBerechnen(ArrayList<Integer> spaltenBreiten) {
		int summe = spaltenBreiten.stream().mapToInt(i -> i).sum();
		final float summeInklusiveGaps = summe + caseSequenzen.size() * LINIENBREITE;
		return spaltenBreiten.stream().map(breite -> (float)breite / summeInklusiveGaps).collect(Collectors.toList());
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		sonstSequenz.ueberschrift.setBackground(bg);
		lueckenFueller.setBackground(bg); //neu
		panelCase.setBackground(bg); //neu
		panelSonst.setBackground(bg); //neu
		panelFall1.setBackground(bg); //neu
		caseSequenzen.forEach(sequenz -> sequenz.ueberschrift.setBackground(bg));
		panel.repaint(); // Damit die Linien nachgezeichnet werden
	}

	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		CaseSchrittModel_V001 model = new CaseSchrittModel_V001(
			id,
			getTextMitAenderungsmarkierungen(formatierterText),
			getBackground().getRGB(),
			aenderungsart,
			klappen.isSelected(),
			sonstSequenz.generiereZweigSchrittSequenzModel(formatierterText),
			new ArrayList<Float>(spaltenbreitenAnteileBerechnen(spaltenbreitenErmitteln())),
			getQuellschrittID(),
			getDecorated());
		caseSequenzen.forEach(sequenz -> model.caseHinzufuegen(sequenz.generiereZweigSchrittSequenzModel(formatierterText)));
		return model;
	}

	@Override
	protected List<SchrittSequenzView> unterSequenzen() {
		return sequenzenAuflisten(caseSequenzen, sonstSequenz);
	}

	@Override
	public boolean enthaelt(JTextComponent textComponent) {
		return super.enthaelt(textComponent) ||
			istZweigUeberschrift(textComponent) != null;
	}

	public ZweigSchrittSequenzView istZweigUeberschrift(JTextComponent textComponent) {
		if (sonstSequenz.hatUeberschrift(textComponent))
			return sonstSequenz;
		for (ZweigSchrittSequenzView caseSequenz: caseSequenzen) {
			if (caseSequenz.hatUeberschrift(textComponent))
				return caseSequenz;
		}
		return null;
	}

	public int zweigEntfernen(EditorI editor, ZweigSchrittSequenzView zweig) {
		if (zweig == sonstSequenz) {
			System.err.println("Noch nicht fertig: Sonst-Sequenz entfernen");
			return -1;
		}
		/** @author PVN */
		if (zweig == caseSequenzen.get(0) && caseSequenzen.size() <=2 ) {
			System.err.println("Es m\u00FCssen mindestens 2 F\u00E4lle bestehen bleiben");
			return -1;
		}
		/**@author PVN */
		if (zweig == caseSequenzen.get(1) && caseSequenzen.size() <=2) {
			System.err.println("Es m\u00FCssen mindestens 2 F\u00E4lle bestehen bleiben");
			return -1;
		}
		/**@author PVN */ 
		if (zweig == caseSequenzen.get(0)) {
			panelFall1.remove(caseSequenzen.get(0).ueberschrift.asJComponent());
		}
		int caseIndex = caseSequenzen.indexOf(zweig);
		zweigAusListeUndPanelEntfernen(zweig);
		zweigAnzahlAenderungAbschliessen(editor, spaltenbreitenErmitteln());
		return caseIndex+1; // 0 ist Indikator f�r Sonst-Zweig, ab 1 beginnen die Cases
	}

	public void zweigHinzufuegen(EditorI editor, ZweigSchrittSequenzView zweig, int zweigIndex) {
		if (zweigIndex == 0) {
			System.err.println("Noch nicht fertig: Sonst-Sequenz hinzuf�gen");
			return;
		}
		/**@author PVN */
		if (zweigIndex == 1) {
//			System.out.println("index=1");
			panel.add(caseSequenzen.get(0).ueberschrift.asJComponent(), CC.xy(5, 3));
		}
		ArrayList<Integer> spaltenBreiten = zweigbreiteInSpaltenbreitenEinpassen(zweig, zweigIndex);
		caseSequenzen.add(zweigIndex-1, zweig); // 0 ist Indikator f�r Sonst-Zweig, ab 1 beginnen die Cases
		panel.add(zweig.getContainer(), INITIAL_DUMMY);
		panel.add(zweig.ueberschrift.asJComponent(), INITIAL_DUMMY);
	
		zweigAnzahlAenderungAbschliessen(editor, spaltenBreiten);
	}

	private void zweigAnzahlAenderungAbschliessen(EditorI editor, ArrayList<Integer> spaltenBreiten) {
		spaltenResizerEntfernen();
		panelLayout = createPanelLayout(caseSequenzen.size());
		panel.setLayout(panelLayout);
		layoutConstraintsSetzen();
		spaltenbreitenAnteileSetzen(spaltenbreitenAnteileBerechnen(spaltenBreiten));
		spaltenResizerAnlegen(editor);
		
		setId(id);
		parent.folgeschritteRenummerieren(this);
		
		editor.diagrammAktualisieren(null);
	}
	
	public ZweigSchrittSequenzView neuenZweigHinzufuegen(EditorI editor, ZweigSchrittSequenzView linkerNachbar) {
		int linkerNachbarIndex = caseSequenzen.indexOf(linkerNachbar);
		ZweigSchrittSequenzView neuerZweig = new ZweigSchrittSequenzView(editor, this, linkerNachbar.naechsteNachbarSequenzID(), aenderungsart, initialtext("Fall " + (linkerNachbarIndex+2)));
		neuerZweig.einfachenSchrittAnhaengen(editor);
		zweigHinzufuegen(editor, neuerZweig, linkerNachbarIndex+2);
		if (neuerZweig == caseSequenzen.get(0)) {
			panelFall1.setBackground(Specman.schrittHintergrund());
		}
		return neuerZweig;
	}
	private ArrayList<Integer> zweigbreiteInSpaltenbreitenEinpassen(ZweigSchrittSequenzView zweig, int zweigIndex) {
		ArrayList<Integer> spaltenBreiten = spaltenbreitenErmitteln();
		int summeSpaltenbreiten = spaltenBreiten.stream().mapToInt(i -> i).sum();
		int zweigbreite = zweig.getContainer().getWidth();
		if (zweigbreite == 0) {
			zweigbreite = summeSpaltenbreiten / (spaltenBreiten.size() + 1);
		}
		for (int i = 0; i < spaltenBreiten.size(); i++) {
			int spaltenBreite = spaltenBreiten.get(i);
			int abgabe = (int) ((float)spaltenBreite / (float)summeSpaltenbreiten * zweigbreite);
			spaltenBreiten.set(i, spaltenBreite-abgabe);
		}
		spaltenBreiten.add(zweigIndex, zweigbreite);
		return spaltenBreiten;
	}

	private void zweigAusListeUndPanelEntfernen(ZweigSchrittSequenzView zweig) {
		caseSequenzen.remove(zweig);
		panel.remove(zweig.getContainer());
		panel.remove(zweig.ueberschrift.asJComponent());
	}

	@Override public void resyncSchrittnummerStil() {
		super.resyncSchrittnummerStil();
		getSonstSequenz().resyncSchrittnummerStil();
		for (ZweigSchrittSequenzView caseSequenz : caseSequenzen) {
			caseSequenz.resyncSchrittnummerStil();
		}

	}

	@Override public void viewsNachinitialisieren() {
		super.viewsNachinitialisieren();
		sonstSequenz.viewsNachinitialisieren();
		for (ZweigSchrittSequenzView caseSequenz : caseSequenzen) {
			caseSequenz.viewsNachinitialisieren();
		}
	}

	@Override public void alsGeloeschtMarkieren() {
		super.alsGeloeschtMarkieren();
		sonstSequenz.alsGeloeschtMarkieren();
		for (ZweigSchrittSequenzView caseSequenz : caseSequenzen) {
			caseSequenz.alsGeloeschtMarkieren();
		}
	}

	@Override public void aenderungsmarkierungenEntfernen() {
		super.aenderungsmarkierungenEntfernen();
		panelFall1.setBackground(TextfieldShef.Hintergrundfarbe_Standard);
		sonstSequenz.aenderungsmarkierungenEntfernen();
		for (ZweigSchrittSequenzView caseSequenz : caseSequenzen) {
			caseSequenz.aenderungsmarkierungenEntfernen();
		}
	}

	@Override public AbstractSchrittView findeSchrittZuId(SchrittID id) {
		AbstractSchrittView result = findeSchrittZuIdIncludingSubSequences(
				id, caseSequenzen.toArray(ZweigSchrittSequenzView[]::new));
		return (result != null) ? result : sonstSequenz.findeSchrittZuId(id);
	}

	private void layoutConstraintsSetzen() {
		/** @author PVN */
		panelLayout.setConstraints(lueckenFueller, CC.xy(1, 1));
		panelLayout.setConstraints(panelCase, CC.xywh(3, 1, (1 + caseSequenzen.size()*2)-2, 1));
		panelCase.add(text.asJComponent(), CC.xy(2, 1));
		panelLayout.setConstraints(panelSonst, CC.xy(1, 3));
		panelSonst.add(sonstSequenz.ueberschrift.asJComponent(), CC.xy(1, 1));
		panelLayout.setConstraints(sonstSequenz.getContainer(), CC.xy(1, 5));
		panelLayout.setConstraints(panelFall1, CC.xy(3, 3));
		panelFall1.add(caseSequenzen.get(0).ueberschrift.asJComponent(), CC.xy(2, 1));
		for (int i = 1; i < caseSequenzen.size(); i++) {
			panelLayout.setConstraints(caseSequenzen.get(i).ueberschrift.asJComponent(), CC.xy(5 + (i*2)-2, 3));
		}
		for (int j = 0; j < caseSequenzen.size(); j++) {
			panelLayout.setConstraints(caseSequenzen.get(j).getContainer(), CC.xy(3 + j*2, 5));
		}
	}

	@Override
	protected void updateTextfieldDecorationIndentions(Indentions indentions) {
		super.updateTextfieldDecorationIndentions(indentions);
		Indentions defaultCaseIndentions = indentions.withRight(false).withTop(false);
		sonstSequenz.updateTextfieldDecorationIndentions(defaultCaseIndentions);
		Indentions caseIndentions = defaultCaseIndentions.withLeft(false);
		caseSequenzen
			.stream()
			.forEach(caseSequence -> caseSequence.updateTextfieldDecorationIndentions(caseIndentions));
	}

	//TODO
	public ZweigSchrittSequenzView getSonstSequenz() {
		return sonstSequenz;
	}

	//TODO
	public List<ZweigSchrittSequenzView> getCaseSequenzen() {
		return caseSequenzen;
	}
	//TODO
	public int getRautenHeight() {
		return berechneRautenmittelpunkt().y;
	}

	public JPanel getPanelFall1() {
		return panelFall1;
	}

	@Override protected void textAenderungenUebernehmen() {
		super.textAenderungenUebernehmen();
		sonstSequenz.ueberschriftAenderungenUebernehmen();
		for (ZweigSchrittSequenzView caseSequenz : caseSequenzen) {
			caseSequenz.ueberschriftAenderungenUebernehmen();
		}
	}

	@Override public void aenderungenUebernehmen(EditorI editor) {
		super.aenderungenUebernehmen(editor);
		panelFall1.setBackground(TextfieldShef.Hintergrundfarbe_Standard);
		setBackground(TextfieldShef.Hintergrundfarbe_Standard);
		sonstSequenz.aenderungenUebernehmen(editor);
		List<ZweigSchrittSequenzView> caseSequenzen = new CopyOnWriteArrayList<ZweigSchrittSequenzView>(this.caseSequenzen);
		for (ZweigSchrittSequenzView caseSequenz : caseSequenzen) {
			if (caseSequenz.getAenderungsart() == Aenderungsart.Geloescht) {
				zweigEntfernen(editor, caseSequenz);
			}
			else {
				caseSequenz.aenderungenUebernehmen(editor);
			}
		}
	}
}
