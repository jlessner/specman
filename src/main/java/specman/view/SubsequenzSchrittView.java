package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import specman.Aenderungsart;
import specman.EditException;
import specman.EditorI;
import specman.SchrittID;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.SubsequenzSchrittModel_V001;
import specman.pdf.Shape;
import specman.textfield.Indentions;
import specman.undo.AbstractUndoableInteraction;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.util.List;

public class SubsequenzSchrittView extends AbstractSchrittView {
	public static final int TEXTEINRUECKUNG = 18;

	final JPanel panel;
	final KlappButton klappen;
	final FormLayout layout;
	SchrittSequenzView subsequenz;

	protected SubsequenzSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialerText, SchrittID id, Aenderungsart aenderungsart, boolean withDefaultContent) {
		super(editor, parent, initialerText, id, aenderungsart);

		editContainer.setLeftInset(TEXTEINRUECKUNG);

		panel = new JPanel();
		panel.setBackground(Color.black);
		layout = new FormLayout("10dlu:grow",
				"fill:pref, " + FORMLAYOUT_GAP + ", " + ZEILENLAYOUT_INHALT_SICHTBAR);
		panel.setLayout(layout);

		panel.add(editContainer, CC.xy(1, 1));

		klappen = new KlappButton(this, editContainer.getKlappButtonParent(), layout, 3);

		//roundedBorderDecorator = new RoundedBorderDecorator(panel);

		if (withDefaultContent) {
			initSubsequenz(einschrittigeInitialsequenz(editor, id.naechsteEbene(), aenderungsart));
		}
	}

	public SubsequenzSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialerText, SchrittID id, Aenderungsart aenderungsart) {
		this(editor, parent, initialerText, id, aenderungsart, true);
	}

	public SubsequenzSchrittView(EditorI editor, SchrittSequenzView parent, SubsequenzSchrittModel_V001 model) {
		this(editor, parent, model.inhalt, model.id, model.aenderungsart, false);
		initSubsequenz(new SchrittSequenzView(editor, this, model.subsequenz));
		setBackground(new Color(model.farbe));
		klappen.init(model.zugeklappt);
	}

	private SchrittSequenzView einschrittigeInitialsequenz(EditorI editor, SchrittID id, Aenderungsart aenderungsart) {
		SchrittSequenzView sequenz = new SchrittSequenzView(this, id, aenderungsart);
		sequenz.einfachenSchrittAnhaengen(editor);
		return sequenz;
	}

	protected void initSubsequenz(SchrittSequenzView subsequenz) {
		this.subsequenz = subsequenz;
		panel.add(subsequenz.getContainer(), CC.xy(1, 3));
	}

	@Override
	public void setId(SchrittID id) {
		super.setId(id);
		subsequenz.renummerieren(id.naechsteEbene());
	}

	@Override
	public JComponent getComponent() { return decorated(panel); }

	@Override
	public boolean isStrukturiert() {
		return true;
	}

	public SchrittSequenzView getSequenz() {
		return subsequenz;
	}

	@Override
	public List<SchrittSequenzView> unterSequenzen() {
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
		klappen.scale(prozent, prozentAktuell);
	}

	@Override
	public void geklappt(boolean auf) {
		subsequenz.setVisible(auf);
	}

	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		SubsequenzSchrittModel_V001 model = new SubsequenzSchrittModel_V001(
			id,
			getEditorContent(formatierterText),
			getBackground().getRGB(),
			aenderungsart,
			klappen.isSelected(),
			subsequenz.generiereSchittSequenzModel(formatierterText),
			getQuellschrittID(),
			getDecorated());
		return model;
	}

	@Override public void resyncSchrittnummerStil() {
		super.resyncSchrittnummerStil();
		subsequenz.resyncSchrittnummerStil();
	}

	@Override public void viewsNachinitialisieren() {
		super.viewsNachinitialisieren();
		subsequenz.viewsNachinitialisieren();
	}

	@Override public AbstractSchrittView findeSchrittZuId(SchrittID id) {
		return findeSchrittZuIdIncludingSubSequences(id, subsequenz);
	}

	@Override public void aenderungenUebernehmen(EditorI editor) throws EditException {
		super.aenderungenUebernehmen(editor);
		subsequenz.aenderungenUebernehmen(editor);
	}

	@Override public void aenderungenVerwerfen(EditorI editor) throws EditException {
		super.aenderungenVerwerfen(editor);
		subsequenz.aenderungenVerwerfen(editor);
	}

	@Override public AbstractUndoableInteraction alsGeloeschtMarkieren(EditorI editor) {
		subsequenz.alsGeloeschtMarkieren(editor);
		return super.alsGeloeschtMarkieren(editor);
	}

	protected void updateTextfieldDecorationIndentions(Indentions indentions) {
		super.updateTextfieldDecorationIndentions(indentions);
		Indentions substepIndentions = indentions.withTop(false).withRight(false);
		subsequenz.updateTextfieldDecorationIndentions(substepIndentions);
	}

	public JPanel getPanel() {
		return panel;
	}

	public SchrittSequenzView getSubsequenz() {
		return subsequenz;
	}

	@Override
	public void componentResized(ComponentEvent e) {
		super.componentResized(e);
		klappen.updateLocation(editContainer.getStepNumberBounds());
	}

	@Override
	public Shape getShape() {
		return super.getShape().add(subsequenz.getShapeSequence());
	}
}