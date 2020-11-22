package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import specman.EditorI;
import specman.SchrittID;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.SubsequenzSchrittModel_V001;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SubsequenzSchrittView extends AbstractSchrittView {
	public static final int TEXTEINRUECKUNG = 18;
	
	final JPanel panel;
	final KlappButton klappen;
	final SchrittSequenzView subsequenz;
	final FormLayout layout;

	public SubsequenzSchrittView(EditorI editor, String initialerText, SchrittSequenzView wiederholSequenz, SchrittID id) {
		super(editor, initialerText, id);
		
		text.setLeftInset(TEXTEINRUECKUNG);

		panel = new JPanel();
		panel.setBackground(Color.black);
		layout = new FormLayout("10dlu:grow",
				"fill:pref, " + FORMLAYOUT_GAP + ", " + ZEILENLAYOUT_INHALT_SICHTBAR);
		panel.setLayout(layout);
		
		panel.add(text.asJComponent(), CC.xy(1, 1));

		this.subsequenz = wiederholSequenz;
		panel.add(wiederholSequenz.getContainer(), CC.xy(1, 3));
		
		klappen = new KlappButton(this, text.getTextComponent(), layout, 3);
	}

	private static SchrittSequenzView einschrittigeInitialsequenz(EditorI editor, SchrittID id) {
		SchrittSequenzView sequenz = new SchrittSequenzView(id);
		sequenz.einfachenSchrittAnhaengen(editor);
		return sequenz;
	}
	
	public SubsequenzSchrittView(EditorI editor, String initialerText, SchrittID id) {
		this(editor, initialerText, einschrittigeInitialsequenz(editor, id.naechsteEbene()), id);
	}

	public SubsequenzSchrittView(EditorI editor, SubsequenzSchrittModel_V001 model) {
		this(editor, model.inhalt.text, new SchrittSequenzView(editor, model.subsequenz), model.id);
		setBackground(new Color(model.farbe));
		klappen.init(model.zugeklappt);
	}

	public SubsequenzSchrittView(EditorI editor, String initialerText) {
		this(editor, initialerText, (SchrittID) null);
	}
	
	@Override
	public void setId(SchrittID id) {
		super.setId(id);
		subsequenz.renummerieren(id.naechsteEbene());
	}

	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public boolean isStrukturiert() {
		return true;
	}

	public SchrittSequenzView getSequenz() {
		return subsequenz;
	}

	@Override
	protected List<SchrittSequenzView> unterSequenzen() {
		return sequenzenAuflisten(subsequenz);
	}

	@Override
	public void zusammenklappenFuerReview() {
		if (!enthaeltAenderungsmarkierungen()) {
			klappen.init(true);
		}
		subsequenz.zusammenklappenFuerReview();
	}
	
	@Override
	public void skalieren(int prozent, int prozentAktuell) {
		super.skalieren(prozent, prozentAktuell);
		subsequenz.skalieren(prozent, prozentAktuell);
	}

	@Override
	public void geklappt(boolean auf) {
		subsequenz.setVisible(auf);
	}

	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		SubsequenzSchrittModel_V001 model = new SubsequenzSchrittModel_V001(
			id,
			getTextMitAenderungsmarkierungen(formatierterText),
			getBackground().getRGB(),
			klappen.isSelected(),
			subsequenz.generiereSchittSequenzModel(formatierterText));
		return model;
	}

}
