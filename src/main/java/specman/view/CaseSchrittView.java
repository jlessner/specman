package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import specman.EditorI;
import specman.SchrittID;
import specman.SpaltenResizer;
import specman.Specman;
import specman.geometry.LineIntersect;
import specman.model.v001.CaseSchrittModel_V001;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.ZweigSchrittSequenzModel_V001;

import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static specman.Specman.initialtext;


public class CaseSchrittView extends VerzweigungSchrittView implements ComponentListener, FocusListener {
	// Alle Components kriegen am Anfang einen sinnfreien Dummy und werden dann
	// über die Methode layoutCojnstraintsSetzen korrekt plaziert. Damit verhindern
	// wir, dass die relativ komplizierte Positionsvergabe redundant im Code auftaucht
	private static final CellConstraints INITIAL_DUMMY = CC.xy(1, 1);
	
	ZweigSchrittSequenzView sonstSequenz;
	List<ZweigSchrittSequenzView> caseSequenzen;

	public CaseSchrittView(EditorI editor, String initialerText, SchrittID id,
			ZweigSchrittSequenzView sonstSequenz,
			List<ZweigSchrittSequenzView> caseSequenzen) {
		super(editor, initialerText, id, createPanelLayout(caseSequenzen.size()));
		
		panel.add(text.asJComponent(), INITIAL_DUMMY);

		sonstFallAnlegen(sonstSequenz);
		casesAnlegen(caseSequenzen);
		
		layoutConstraintsSetzen();
		spaltenResizerAnlegen(editor);
	}

	public CaseSchrittView(EditorI editor, String initialerText, SchrittID id) {
		this(editor, initialerText, id,
			 new ZweigSchrittSequenzView(editor, id.naechsteEbene(), initialtext("Sonst")),
		     new ArrayList<ZweigSchrittSequenzView>(Arrays.asList(new ZweigSchrittSequenzView[] {
		    	new ZweigSchrittSequenzView(editor, id.naechsteID().naechsteEbene(), initialtext("Fall 1")),
		    	new ZweigSchrittSequenzView(editor, id.naechsteID().naechsteID().naechsteEbene(), initialtext("Fall 2"))
		     })));
	}
	
	private static List<ZweigSchrittSequenzView> caseSequenzenAufbauen(EditorI editor, List<ZweigSchrittSequenzModel_V001> model) {
		return model.stream()
				.map(sequenzModel -> new ZweigSchrittSequenzView(editor, sequenzModel))
				.collect(Collectors.toList());
	}
	
	public CaseSchrittView(EditorI editor, CaseSchrittModel_V001 model) {
		this(editor, model.inhalt.text, model.id,
			new ZweigSchrittSequenzView(editor, model.sonstSequenz),
			caseSequenzenAufbauen(editor, model.caseSequenzen));
		setBackground(new Color(model.farbe));
		spaltenbreitenAnteileSetzen(model.spaltenbreitenAnteile);
		klappen.init(model.zugeklappt);
	}

	private void spaltenResizerAnlegen(EditorI editor) {
		for (int i = 0; i < caseSequenzen.size(); i++) {
			panel.add(new SpaltenResizer(this, i, editor), CC.xywh(2 + 2*i, 2, 1, 3));
		}
	}
	
	private void spaltenResizerEntfernen() {
		for (MouseListener ml: panel.getMouseListeners()) {
			if (ml instanceof SpaltenResizer)
				panel.removeMouseListener(ml);
		}
	}

	private static FormLayout createPanelLayout(int anzahlCases) {
		String spaltenSpec = "10px:grow, " + FORMLAYOUT_GAP;
		for (int i = 1; i < anzahlCases; i++)
			spaltenSpec += ", 10px:grow, " + FORMLAYOUT_GAP;
		spaltenSpec += ", 10px:grow";
		return new FormLayout(spaltenSpec,
				layoutRowSpec1() + ", fill:pref, " + FORMLAYOUT_GAP + ", " + ZEILENLAYOUT_INHALT_SICHTBAR);
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
	}

	@Override
	public SchrittID folgeIDInGleicherSequenz() {
		SchrittID id = super.folgeIDInGleicherSequenz();
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
		return sonstSequenz.ueberschrift.getWidth() / 2;
	}

	protected Point dreieckUndTrennerZeichnen(Graphics2D g) {
		Point dreieckSpitze = super.dreieckUndTrennerZeichnen(g);
		
		// Wir zeichnen gleich nur noch senkrechte Linie. Die werden mit Antialiasing unscharf und
		// bilden dann keine nahtlose, sauber Verl�ngerung der Gaps aus dem Layoutgitter mehr.
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		
		for (ZweigSchrittSequenzView caseSequenz: caseSequenzen) {
			int x = caseSequenz.ueberschrift.getX() - LINIENBREITE/2;
			Point2D.Double schnittpunktMitDreieckslinie = LineIntersect.lineLineIntersect(
					dreieckSpitze.x,  dreieckSpitze.y, panel.getWidth(), 0,
					x, dreieckSpitze.y, x, 0);
			g.drawLine(x, dreieckSpitze.y, x, (int)schnittpunktMitDreieckslinie.getY() + LINIENBREITE);
		}

		return dreieckSpitze;
	}
	
	protected Point berechneDreieckspitze() {
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
		caseSequenzen.forEach(sequenz -> sequenz.ueberschrift.setBackground(bg));
		panel.repaint(); // Damit die Linien nachgezeichnet werden
	}

	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		CaseSchrittModel_V001 model = new CaseSchrittModel_V001(
			id,
			getTextMitAenderungsmarkierungen(formatierterText),
			getBackground().getRGB(),
			klappen.isSelected(),
			sonstSequenz.generiereZweigSchrittSequenzModel(formatierterText),
			new ArrayList<Float>(spaltenbreitenAnteileBerechnen(spaltenbreitenErmitteln())));
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
		SchrittSequenzView elternSequenz = editor.findeElternSequenz(this);
		elternSequenz.folgeschritteRenummerieren(this);
		
		Specman.instance().diagrammAktualisieren(null);
	}
	
	public ZweigSchrittSequenzView neuenZweigHinzufuegen(EditorI editor, ZweigSchrittSequenzView linkerNachbar) {
		int linkerNachbarIndex = caseSequenzen.indexOf(linkerNachbar);
		ZweigSchrittSequenzView neuerZweig = new ZweigSchrittSequenzView(editor, linkerNachbar.naechsteNachbarSequenzID(), "Fall " + (linkerNachbarIndex+2));
		neuerZweig.einfachenSchrittAnhaengen(editor);
		zweigHinzufuegen(editor, neuerZweig, linkerNachbarIndex+2);
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

	private void layoutConstraintsSetzen() {
		panelLayout.setConstraints(text.asJComponent(), CC.xywh(1, 1, 1 + caseSequenzen.size()*2, 1));
		panelLayout.setConstraints(sonstSequenz.ueberschrift.asJComponent(), CC.xy(1, 2));
		panelLayout.setConstraints(sonstSequenz.getContainer(), CC.xy(1, 4));
		for (int i = 0; i < caseSequenzen.size(); i++) {
			panelLayout.setConstraints(caseSequenzen.get(i).ueberschrift.asJComponent(), CC.xy(3 + i*2, 2));
			panelLayout.setConstraints(caseSequenzen.get(i).getContainer(), CC.xy(3 + i*2, 4));
		}
	}

}
