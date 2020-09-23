package specman;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import specman.model.SchrittModel;
import specman.model.WhileSchrittModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class SchleifenSchrittView extends SchrittView implements SpaltenContainerI {
	
	final JPanel panel;
	final JPanel linkerBalken;
	final JPanel untererBalken;
	final KlappButton klappen;
	final SchrittSequenzView wiederholSequenz;
	final FormLayout layout;
	int balkenbreite;

	public SchleifenSchrittView(EditorI editor, String initialerText, SchrittSequenzView wiederholSequenz, SchrittID id, boolean mitUnteremBalken) {
		super(editor, initialerText, id);
		panel = new JPanel();
		panel.setBackground(Color.black);
		balkenbreite = SPALTENLAYOUT_UMGEHUNG_GROESSE;
		layout = new FormLayout(
				umgehungLayout(balkenbreite) + ", " + FORMLAYOUT_GAP + ", 10dlu:grow",
				"fill:pref, " + FORMLAYOUT_GAP + ", " + ZEILENLAYOUT_INHALT_SICHTBAR);
		panel.setLayout(layout);
		
		panel.add(text, CC.xywh(2, 1, 2, 1));

		this.wiederholSequenz = wiederholSequenz;
		panel.add(wiederholSequenz.getContainer(), CC.xy(3, 3));
		
		linkerBalken = new JPanel();
		linkerBalken.setLayout(null);
		linkerBalken.setBackground(Specman.schrittHintergrund());
		
		if (mitUnteremBalken) {
			layout.appendRow(RowSpec.decode(FORMLAYOUT_GAP));
			layout.appendRow(RowSpec.decode(umgehungLayout()));
			panel.add(linkerBalken, CC.xywh(1, 1, 1, 4));
			untererBalken = new JPanel();
			untererBalken.setLayout(null);
			untererBalken.setBackground(Specman.schrittHintergrund());
			panel.add(untererBalken, CC.xywh(1, 5, 3, 1));
		}
		else {
			panel.add(linkerBalken, CC.xywh(1, 1, 1, 3));
			untererBalken = null;
		}

		panel.add(new SpaltenResizer(this, editor), CC.xy(2, 3));

		klappen = new KlappButton(this, linkerBalken, layout, 3);
	}

	public SchleifenSchrittView(EditorI editor, SchrittID id) {
		this(editor, null, einschrittigeInitialsequenz(editor, id.naechsteEbene()), id, false);
	}

	public SchleifenSchrittView(EditorI editor, WhileSchrittModel model, boolean mitUnteremBalken) {
		this(editor, model.inhalt.text, new SchrittSequenzView(editor, model.wiederholSequenz), model.id, mitUnteremBalken);
		setBackground(new Color(model.farbe));
		balkenbreiteSetzen(model.balkenbreite);
		klappen.init(model.zugeklappt);;
	}

	public SchleifenSchrittView(EditorI editor) {
		this(editor, (SchrittID) null);
	}
	
	@Override
	public int spaltenbreitenAnpassenNachMausDragging(int vergroesserung, int spalte) {
		int angepassteBalkenBreite = linkerBalken.getWidth() + vergroesserung;
		balkenbreiteSetzen(angepassteBalkenBreite);
		Specman.instance().diagrammAktualisieren(null);
		return vergroesserung;
	}

	private void balkenbreiteSetzen(int balkenbreite) {
		this.balkenbreite = balkenbreite;
		layout.setColumnSpec(1, ColumnSpec.decode(balkenbreite + "px"));
	}

	protected static SchrittSequenzView einschrittigeInitialsequenz(EditorI editor, SchrittID id) {
		SchrittSequenzView sequenz = new SchrittSequenzView(id);
		sequenz.einfachenSchrittAnhaengen(editor);
		return sequenz;
	}
	
	@Override
	public void setId(SchrittID id) {
		super.setId(id);
		wiederholSequenz.renummerieren(id.naechsteEbene());
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		linkerBalken.setBackground(bg);
		if (untererBalken != null)
			untererBalken.setBackground(bg);
	}

	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public boolean isStrukturiert() {
		return true;
	}

	@Override
	void schrittnummerSichtbarkeitSetzen(boolean sichtbar) {
		super.schrittnummerSichtbarkeitSetzen(sichtbar);
		wiederholSequenz.schrittnummerSichtbarkeitSetzen(sichtbar);
	}

	public SchrittSequenzView getSequenz() {
		return wiederholSequenz;
	}

	
	@Override
	protected List<SchrittSequenzView> unterSequenzen() {
		return sequenzenAuflisten(wiederholSequenz);
	}

	@Override
	public void zusammenklappenFuerReview() {
		if (!enthaeltAenderungsmarkierungen()) {
			klappen.init(true);
		}
		wiederholSequenz.zusammenklappenFuerReview();
	}
	
	public void skalieren(int prozentNeu, int prozentAktuell) {
		super.skalieren(prozentNeu, prozentAktuell);
		int neueBalkenbreite = groesseUmrechnen(balkenbreite, prozentNeu, prozentAktuell);
		balkenbreiteSetzen(neueBalkenbreite);
		if (untererBalken != null) {
			String unterBalkenLayout = untererBalken.isVisible() ? umgehungLayout() : ZEILENLAYOUT_INHALT_VERBORGEN;
			layout.setRowSpec(5, RowSpec.decode(unterBalkenLayout));
		}
	}

	@Override
	public void geklappt(boolean auf) {
		wiederholSequenz.setVisible(auf);
		if (untererBalken != null) {
			untererBalken.setVisible(auf);
			String unterBalkenLayout = auf ? umgehungLayout() : ZEILENLAYOUT_INHALT_VERBORGEN;
			layout.setRowSpec(5, RowSpec.decode(unterBalkenLayout));
		}
	}

	@Override
	public SchrittModel generiereModel(boolean formatierterText) {
		WhileSchrittModel model = new WhileSchrittModel();
		model.inhalt = getTextMitAenderungsmarkierungen(formatierterText);
		model.id = id;
		model.farbe = getBackground().getRGB();
		model.wiederholSequenz = wiederholSequenz.generiereSchittSequenzModel(formatierterText);
		model.zugeklappt = klappen.isSelected();
		return model;
	}


}
