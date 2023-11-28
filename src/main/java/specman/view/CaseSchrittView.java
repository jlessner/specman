package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import specman.Aenderungsart;
import specman.EditException;
import specman.EditorI;
import specman.SchrittID;
import specman.SpaltenResizer;
import specman.Specman;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.CaseSchrittModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.ZweigSchrittSequenzModel_V001;
import specman.pdf.Shape;
import specman.editarea.Indentions;
import specman.editarea.InteractiveStepFragment;
import specman.undo.UndoableZweigEntfernt;
import specman.undo.props.UDBL;

import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static specman.Specman.initialtext;
import static specman.editarea.TextStyles.BACKGROUND_COLOR_STANDARD;
import static specman.pdf.Shape.GAP_COLOR;
import static specman.editarea.TextStyles.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE;


public class CaseSchrittView extends VerzweigungSchrittView {
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

	public CaseSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialerText, SchrittID id, Aenderungsart aenderungsart, int numCases) {
		super(editor, parent, initialerText, id, aenderungsart, createPanelLayout(numCases));
		panel.add(editContainer, INITIAL_DUMMY);
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

	public CaseSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialerText, SchrittID id, Aenderungsart aenderungsart) {
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
		this(editor, parent, model.inhalt, model.id, model.aenderungsart, model.caseSequenzen.size());
		initCases(
				editor,
				new ZweigSchrittSequenzView(editor, this, model.sonstSequenz),
				caseSequenzenAufbauen(editor, model.caseSequenzen));
		setBackgroundUDBL(new Color(model.farbe));
		spaltenbreitenAnteileSetzen(model.spaltenbreitenAnteile);
		klappen.init(model.zugeklappt);
	}

	@Override
	public void geklappt(boolean auf) {
		// It's not really clear why we have to set these component's visibility at all as the KlappButton
		// turns the layout row of all of them to 0px which should be enough. However, when recreating
		// the resizers on adding or removing a case sequence, there remains a little space even with a
		// 0px layout row.
		sonstSequenz.setVisible(auf);
		caseSequenzen.forEach(seq -> seq.setVisible(auf));
		getSpaltenResizers().forEach(resizer -> resizer.setVisible(auf));
	}

	private void spaltenResizerAnlegen(EditorI editor) {
		for (int i = 0; i < caseSequenzen.size(); i++) {
			panel.add(new SpaltenResizer(this, i, editor), CC.xywh(2 + 2*i, 4, 1, 2));
		}
	}

	private List<SpaltenResizer> getSpaltenResizers() {
		return Arrays.stream(panel.getComponents())
			.filter(c -> c instanceof SpaltenResizer)
			.map(c -> (SpaltenResizer)c)
			.collect(Collectors.toList());
	}

	private void spaltenResizerEntfernen() {
		List<SpaltenResizer> currentResizers = getSpaltenResizers();
		currentResizers.forEach(resizer -> panel.remove(resizer));
	}

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
		panelLayout.setRowSpec(1, RowSpec.decode(layoutRowSpec1()));
		int neueSpaltenbreite = spalteUmrechnen(prozentNeu); /** @author PVN */
		panelCase.setLayout(new FormLayout(neueSpaltenbreite + ", 10px:grow", "fill:pref:grow")); /**@author SD */
		panelSonst.setLayout(new FormLayout("10px:grow, " + neueSpaltenbreite, "fill:pref:grow")); /**@author SD*/
		panelFall1.setLayout(new FormLayout(neueSpaltenbreite + ", 10px:grow", "fill:pref:grow")); /**@author SD*/
		panelCase.add(editContainer, CC.xy(2, 1)); //siehe Methode layoutConstraintsSetzen
		panelSonst.add(sonstSequenz.ueberschrift, CC.xy(1, 1)); //siehe Methode layoutConstraintsSetzen
		panelFall1.add(caseSequenzen.get(0).ueberschrift, CC.xy(2, 1)); //siehe Methode layoutConstraintsSetzen
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
		sonstSequenz.ueberschrift.addEditAreasFocusListener(this);
		panel.add(sonstSequenz.ueberschrift, INITIAL_DUMMY);
		panel.add(sonstSequenz.getContainer(), INITIAL_DUMMY);
	}

	private void casesAnlegen(List<ZweigSchrittSequenzView> caseSequenzen) {
		this.caseSequenzen = caseSequenzen;
		for (int i = 0; i < caseSequenzen.size(); i++) {
			caseSequenzen.get(i).ueberschrift.addEditAreasFocusListener(this);
			panel.add(caseSequenzen.get(i).ueberschrift, INITIAL_DUMMY);
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
	public void setBackgroundUDBL(Color bg) {
		super.setBackgroundUDBL(bg);
		sonstSequenz.ueberschrift.setBackgroundUDBL(bg);
		UDBL.setBackgroundUDBL(lueckenFueller, bg);
		UDBL.setBackgroundUDBL(panelCase, bg);
		UDBL.setBackgroundUDBL(panelSonst, bg);
		UDBL.setBackgroundUDBL(panelFall1, bg);
		caseSequenzen.forEach(sequenz -> sequenz.ueberschrift.setBackgroundUDBL(bg));
		UDBL.repaint(panel); // Damit die Linien nachgezeichnet werden
	}

	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		CaseSchrittModel_V001 model = new CaseSchrittModel_V001(
			id,
			getEditorContent(formatierterText),
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
	public List<SchrittSequenzView> unterSequenzen() {
		return sequenzenAuflisten(caseSequenzen, sonstSequenz);
	}

	@Override
	public boolean enthaelt(InteractiveStepFragment fragment) {
		return super.enthaelt(fragment) ||
			istZweigUeberschrift(fragment) != null;
	}

	public ZweigSchrittSequenzView istZweigUeberschrift(InteractiveStepFragment fragment) {
		if (sonstSequenz.hatUeberschrift(fragment))
			return sonstSequenz;
		for (ZweigSchrittSequenzView caseSequenz: caseSequenzen) {
			if (caseSequenz.hatUeberschrift(fragment))
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
			panelFall1.remove(caseSequenzen.get(0).ueberschrift);
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
			panel.add(caseSequenzen.get(0).ueberschrift, CC.xy(5, 3));
		}
		ArrayList<Integer> spaltenBreiten = zweigbreiteInSpaltenbreitenEinpassen(zweig, zweigIndex);
		caseSequenzen.add(zweigIndex-1, zweig); // 0 ist Indikator f�r Sonst-Zweig, ab 1 beginnen die Cases
		panel.add(zweig.getContainer(), INITIAL_DUMMY);
		panel.add(zweig.ueberschrift, INITIAL_DUMMY);

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
		klappen.refreshGeklappt();

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
		panel.remove(zweig.ueberschrift);
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

	@Override public void alsGeloeschtMarkierenUDBL(EditorI editor) {
		ZweigSchrittSequenzView zweig = istZweigUeberschrift(editor.getLastFocusedTextArea());
		if (zweig != null) {
			if (zweig.getAenderungsart() == Aenderungsart.Hinzugefuegt) {
				int zweigIndex = zweigEntfernen(editor, zweig);
				editor.addEdit(new UndoableZweigEntfernt(editor, zweig, this, zweigIndex));
			}

			//Markieren von Sonstsequenz und fall 1, 2 nicht ermöglichen
			else if (zweig == sonstSequenz) {
				System.err.println("Sonst-Sequenz kann nicht entfernt werden");
			}
			else if (zweig == caseSequenzen.get(0) && caseSequenzen.size() <= 2) {
				System.err.println("Es m\u00FCssen mindestens 2 F\u00E4lle bestehen bleiben");
			}
			else if (zweig == caseSequenzen.get(1) && caseSequenzen.size() <= 2) {
				System.err.println("Es m\u00FCssen mindestens 2 F\u00E4lle bestehen bleiben");
			}
			else {
				zweig.alsGeloeschtMarkierenUDBL(editor);
				if (zweig == caseSequenzen.get(0)) {
					UDBL.setBackgroundUDBL(panelFall1, AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
					UDBL.repaint(panel);
				}
			}
			return;
		}

		sonstSequenz.alsGeloeschtMarkierenUDBL(editor);
		for (ZweigSchrittSequenzView caseSequenz : caseSequenzen) {
			caseSequenz.alsGeloeschtMarkierenUDBL(editor);
		}
		super.alsGeloeschtMarkierenUDBL(editor);
	}

	@Override public void aenderungsmarkierungenEntfernen() {
		super.aenderungsmarkierungenEntfernen();
		panelFall1.setBackground(BACKGROUND_COLOR_STANDARD);
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
		panelCase.add(editContainer, CC.xy(2, 1));
		panelLayout.setConstraints(panelSonst, CC.xy(1, 3));
		panelSonst.add(sonstSequenz.ueberschrift, CC.xy(1, 1));
		panelLayout.setConstraints(sonstSequenz.getContainer(), CC.xy(1, 5));
		panelLayout.setConstraints(panelFall1, CC.xy(3, 3));
		panelFall1.add(caseSequenzen.get(0).ueberschrift, CC.xy(2, 1));
		for (int i = 1; i < caseSequenzen.size(); i++) {
			panelLayout.setConstraints(caseSequenzen.get(i).ueberschrift, CC.xy(5 + (i*2)-2, 3));
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

	@Override protected int editAenderungenUebernehmen() {
		int changesMade = super.editAenderungenUebernehmen();
		changesMade += sonstSequenz.ueberschriftAenderungenUebernehmen();
		for (ZweigSchrittSequenzView caseSequenz : caseSequenzen) {
			changesMade += caseSequenz.ueberschriftAenderungenUebernehmen();
		}
		return changesMade;
	}

	@Override public int aenderungenUebernehmen(EditorI editor) throws EditException {
		panelFall1.setBackground(BACKGROUND_COLOR_STANDARD);
		setBackgroundUDBL(BACKGROUND_COLOR_STANDARD);
		int changesMade = sonstSequenz.aenderungenUebernehmen(editor);
		List<ZweigSchrittSequenzView> caseSequenzen = new CopyOnWriteArrayList<ZweigSchrittSequenzView>(this.caseSequenzen);
		for (ZweigSchrittSequenzView caseSequenz : caseSequenzen) {
			if (caseSequenz.getAenderungsart() == Aenderungsart.Geloescht) {
				zweigEntfernen(editor, caseSequenz);
				changesMade++;
			}
			else {
				changesMade += caseSequenz.aenderungenUebernehmen(editor);
			}
		}
		changesMade += super.aenderungenUebernehmen(editor);
		return changesMade;
	}

	@Override protected int editAenderungenVerwerfen() {
		int changesReverted = super.editAenderungenVerwerfen();
		changesReverted += sonstSequenz.ueberschriftAenderungenVerwerfen();
		for (ZweigSchrittSequenzView caseSequenz : caseSequenzen) {
			changesReverted += caseSequenz.ueberschriftAenderungenVerwerfen();
		}
		return changesReverted;
	}

	@Override public int aenderungenVerwerfen(EditorI editor) throws EditException {
		//Wir spiegeln die Liste einmal auf eine CopyOnWriteArrayList um zweige während des durchlaufens bearbeiten zu können
		List<ZweigSchrittSequenzView> caseSequenzen = new CopyOnWriteArrayList<ZweigSchrittSequenzView>(this.caseSequenzen);
		int changesReverted = sonstSequenz.aenderungenVerwerfen(editor);
		for (ZweigSchrittSequenzView caseSequenz : caseSequenzen) {
			if(caseSequenz.getAenderungsart() == Aenderungsart.Hinzugefuegt) {
				changesReverted += zweigEntfernen(editor, caseSequenz);
			}
			else {
				changesReverted += caseSequenz.aenderungenVerwerfen(editor);
			}
		}
		changesReverted += super.aenderungenVerwerfen(editor);
		return changesReverted;
	}

	@Override
	public List<JTextComponent> getTextAreas() {
		List<JTextComponent> result = super.getTextAreas();
		result.addAll(sonstSequenz.getTextAreas());
		caseSequenzen.forEach(seq -> result.addAll(seq.getTextAreas()));
		return result;
	}

	@Override
	public Shape getShape() {
		Shape shape = new Shape(getPanel())
			.withBackgroundColor(GAP_COLOR)
			.add(lueckenFueller)
			.add(new Shape(panelCase).add(editContainer.getShape()))
			.add(new Shape(panelSonst).add(sonstSequenz.ueberschrift.getShape()))
			.add(new Shape(panelFall1).add(caseSequenzen.get(0).ueberschrift.getShape()))
			.add(caseSequenzen.get(0).getShapeSequence())
			.add(sonstSequenz.getShapeSequence());
		for (ZweigSchrittSequenzView caseSequenz : caseSequenzen.subList(1, caseSequenzen.size())) {
			shape
				.add(caseSequenz.getShapeSequence())
				.add(caseSequenz.ueberschrift.getShape());
		}
		return decoratedShape(shape.add(createDiamond()));
	}
}