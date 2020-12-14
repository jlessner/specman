package specman.view;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.IfSchrittModel_V001;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.textfield.TextfieldShef;

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
	
	public IfSchrittView(EditorI editor, SchrittSequenzView parent, String initialerString, SchrittID id) {
		super(editor, parent, initialerString, id, false);
		initIfSequenz(new ZweigSchrittSequenzView(editor, this, id.naechsteID().naechsteEbene(), ""));
		initElseSequenz(new ZweigSchrittSequenzView(editor, this, id.naechsteEbene(), TextfieldShef.right("Ja")));
		ifBreite = SPALTENLAYOUT_UMGEHUNG_GROESSE * Specman.instance().zoomFaktor() / 100;
	}

	public IfSchrittView(EditorI editor, SchrittSequenzView parent, IfSchrittModel_V001 model) {
		super(editor, parent, model.inhalt.text, model.id, false);
		initIfSequenz(new ZweigSchrittSequenzView(editor, this, new SchrittID(), ""));
		initElseSequenz(new ZweigSchrittSequenzView(editor, this, model.ifSequenz));
		setBackground(new Color(model.farbe));
		ifBreiteSetzen(model.leerBreite);
		klappen.init(model.zugeklappt);;
	}

	@Override
	protected void initIfSequenz(ZweigSchrittSequenzView pIfSequenz) {
		super.initIfSequenz(pIfSequenz);
		ifSequenz.sequenzBereich.setBackground(schrittHintergrund());
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
	
	@Override public SchrittID newStepIDInSameSequence(RelativeStepPosition direction) {
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
//		ifBreite = 10;
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
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		IfSchrittModel_V001 model = new IfSchrittModel_V001(
			id,
			getTextMitAenderungsmarkierungen(formatierterText),
			getBackground().getRGB(),
			klappen.isSelected(),
			elseSequenz.generiereZweigSchrittSequenzModel(formatierterText),
			ifSequenz.ueberschrift.getWidth());
		return model;
	}
	
}
