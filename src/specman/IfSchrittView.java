package specman;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import specman.model.IfSchrittModel;
import specman.model.SchrittModel;

import java.awt.*;

import static specman.Specman.schrittHintergrund;

/**
 * Im Gegensatz zum Struktogramm-Standard verwenden wird die <i>rechte</i> Seite für die Sequenz der
 * bedingt auszuführenden Unterschritte und die linke Seite bleibt leer. Das ist vorteilhaft für die grafische
 * Anordnung. Z.B. kann der Fragetext linksbündig platziert werden.<br>
 * Durch die Basisklasse ist auf der rechten Seite der Else-Zweig. Der Einfachheit halber verwenden wir den
 * also hier. Kann man auch noch mal ändern, wenn das bei generativen Auswertungen der Modelle für zu
 * viel Verwirrung sorgen sollte.
 * 
 * @author less02
 */
public class IfSchrittView extends IfElseSchrittView {

	int ifBreite;
	
	public IfSchrittView(EditorI editor, String initialerString, SchrittID id, ZweigSchrittSequenzView pIfSequenz, ZweigSchrittSequenzView pElseSequenz) {
		super(editor, initialerString, id, pIfSequenz, pElseSequenz);
		ifSequenz.sequenzBereich.setBackground(schrittHintergrund());
		ifBreite = SPALTENLAYOUT_UMGEHUNG_GROESSE * Specman.instance().zoomFaktor() / 100;
	}

	public IfSchrittView(EditorI editor, String initialerString, SchrittID id) {
		this(editor, initialerString, id,
			new ZweigSchrittSequenzView(editor, id.naechsteID().naechsteEbene(), ""),
			new ZweigSchrittSequenzView(editor, id.naechsteEbene(), TextfeldShef.right("Ja")));
	}
	
	public IfSchrittView(EditorI editor, IfSchrittModel model) {
		this(editor, model.inhalt.text, model.id,
			new ZweigSchrittSequenzView(editor, new SchrittID(), ""),
			new ZweigSchrittSequenzView(editor, model.ifSequenz));
		setBackground(new Color(model.farbe));
		ifBreiteSetzen(model.leerBreite);
		klappen.init(model.zugeklappt);;
	}

	@Override
	protected void initialeSchritteAnhaengen(EditorI editor) {
		elseSequenz.einfachenSchrittAnhaengen(editor);
	}

	protected static FormLayout createPanelLayout() {
		FormLayout layout = IfElseSchrittView.createPanelLayout();
		layout.setColumnSpec(1, ColumnSpec.decode(umgehungLayout()));
		return layout;
	}

	@Override
	public void setId(SchrittID id) {
		super.setId(id);
		SchrittID elseID = id.naechsteEbene();
		elseSequenz.renummerieren(elseID);
	}
	
	@Override
	public SchrittID folgeIDInGleicherSequenz() {
		return id.naechsteID();
	}

	@Override
	public int spaltenbreitenAnpassenNachMausDragging(int vergroesserung, int spalte) {
		int angepassteIfBreite = ifSequenz.ueberschrift.getWidth() + vergroesserung;
		ifBreiteSetzen(angepassteIfBreite);
		Specman.instance().diagrammAktualisieren(null);
		return vergroesserung;
	}

	private void ifBreiteSetzen(int angepassteIfBreite) {
		ifBreite = angepassteIfBreite;
		panelLayout.setColumnSpec(1, ColumnSpec.decode(angepassteIfBreite + "px"));
	}
	
	@Override
	protected int texteinrueckungNeuberechnen() {
		return ifSequenz.ueberschrift.getWidth();
	}
	
	@Override
	public void skalieren(int prozentNeu, int prozentAktuell) {
		super.skalieren(prozentNeu, prozentAktuell);
		int neueIfBreite = groesseUmrechnen(ifBreite, prozentNeu, prozentAktuell);
		ifBreiteSetzen(neueIfBreite);
	}

	@Override
	public SchrittModel generiereModel(boolean formatierterText) {
		IfSchrittModel model = new IfSchrittModel();
		model.id = id;
		model.inhalt = getTextMitAenderungsmarkierungen(formatierterText);
		model.farbe = getBackground().getRGB();
		model.ifSequenz = elseSequenz.generiereZweigSchrittSequenzModel(formatierterText);
		model.leerBreite = ifSequenz.ueberschrift.getWidth();
		model.zugeklappt = klappen.isSelected();
		return model;
	}
	
}
