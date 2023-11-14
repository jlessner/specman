package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import specman.Aenderungsart;
import specman.EditException;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.CatchSchrittModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.SchrittSequenzModel_V001;
import specman.editarea.InteractiveStepFragment;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.util.List;

import static specman.editarea.TextStyles.DIAGRAMM_LINE_COLOR;

public class CatchSchrittView extends AbstractSchrittView {
	public static final int TEXTEINRUECKUNG = SubsequenzSchrittView.TEXTEINRUECKUNG;

	final JPanel schrittPanel;
	final JPanel fussPanel;
	final FormLayout layout;
	final HandlingSchrittSequenz handlingSequenz;
	boolean hatNachfolger;
	KlappButton klappen;
	boolean breakAngekoppelt;

	public CatchSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialerText, SchrittID id, Aenderungsart aenderungsart, SchrittSequenzModel_V001 handlingModel) {
		super(editor, parent, initialerText, id, aenderungsart);
		schrittPanel = new JPanel();
		schrittPanel.setBackground(DIAGRAMM_LINE_COLOR);
		layout = new FormLayout(
				umgehungLayout() + ", 10dlu:grow",
				(LINIENBREITE * 2) + "px, " + ZEILENLAYOUT_INHALT_SICHTBAR + ", " + FORMLAYOUT_GAP + ", " + ZEILENLAYOUT_INHALT_SICHTBAR + ", pref:grow, 0px");
		schrittPanel.setLayout(layout);

		schrittPanel.add(editContainer, CC.xy(2, 2));


		JPanel doppellinie = new JPanel();
		doppellinie.setBackground(Specman.schrittHintergrund());
		doppellinie.setBorder(new MatteBorder(0, 0, LINIENBREITE, 0, DIAGRAMM_LINE_COLOR));
		schrittPanel.add(doppellinie, CC.xyw(1, 1, 2));

		fussPanel = new JPanel();
		fussPanel.setBackground(Specman.schrittHintergrund());
		schrittPanel.add(fussPanel, CC.xyw(1, 6, 2));


		if (handlingModel != null) {
			handlingSequenz = new HandlingSchrittSequenz(editor, this, handlingModel);
		}
		else {
			handlingSequenz = new HandlingSchrittSequenz(this, id.naechsteEbene(), aenderungsart);
			handlingSequenz.einfachenSchrittAnhaengen(editor);
			schrittnummerSichtbarkeitSetzen(false);
		}
		schrittPanel.add(handlingSequenz.getContainer(), CC.xywh(1, 4, 2, 1));

	}

	@Override
	public void setId(SchrittID id) {
		super.setId(id);
		handlingSequenz.renummerieren(id.naechsteEbene());
	}

	@Override
	public void geklappt(boolean auf) {
		handlingSequenz.setVisible(auf);
		if (!auf)
			handlingSequenz.rahmenAnzeigen(false);
		else
			handlingSequenz.rahmenanzeigeAnpassen();
	}

	public CatchSchrittView(EditorI editor, SchrittSequenzView parent, CatchSchrittModel_V001 model) {
		this(editor, parent, model.inhalt, model.id, model.aenderungsart, model.handlingSequenz);
		setBackgroundUDBL(new Color(model.farbe));
		klappen.init(model.zugeklappt);
		breakAngekoppelt = model.breakAngekoppelt;
		schrittnummerSichtbarkeitSetzen(breakAngekoppelt);
		editor.schrittFuerNachinitialisierungRegistrieren(this);
	}

	public CatchSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialerText) {
		this(editor, parent, initialerText, null, null, null);
	}

	@Override
	public void nachinitialisieren() {
		if (breakAngekoppelt) {
			breakAnkopplungAktualisieren();
		}
	}

	@Override
	public void setBackgroundUDBL(Color bg) {
		super.setBackgroundUDBL(bg);
		schrittPanel.setBackground(bg);
		editContainer.setBackground(bg);
//		dreiecksPanel.setBackground(bg);
		fussPanel.setBackground(bg);
	}

	public void hatNachfolger(boolean hatNachfolger) {
		this.hatNachfolger = hatNachfolger;
		handlingSequenz.rahmenanzeigeAnpassen();
	}

	@Override
	void schrittnummerSichtbarkeitSetzen(boolean sichtbar) {
		super.schrittnummerSichtbarkeitSetzen(sichtbar);
		handlingSequenz.schrittnummerSichtbarkeitSetzen(sichtbar);
	}

	@Override
	public JComponent getDecoratedComponent() { return decorated(schrittPanel); }

	@Override
	public AbstractSchrittView findeSchritt(InteractiveStepFragment fragment) {
		return handlingSequenz.findeSchritt(fragment);
	}

	@Override
	public BreakSchrittView findeBreakSchritt(String catchText) {
		return handlingSequenz.findeBreakSchritt(catchText);
	}

	@Override
	public boolean isStrukturiert() { return true; }

	@Override
	public void focusLost(FocusEvent e) {
		super.focusLost(e);
		breakAnkopplungAktualisieren();
	}

	private void breakAnkopplungAktualisieren() {
		BreakSchrittView passenderBreakSchritt = Specman.instance().findeBreakSchritt(this);
		if (passenderBreakSchritt != null) {
			setId(passenderBreakSchritt.id);
			handlingSequenz.renummerieren(passenderBreakSchritt.id.naechsteEbene());
			passenderBreakSchritt.zielAnkoppeln(this);
			breakAngekoppelt = true;
		}
		else {
			breakAngekoppelt = false;
		}
		schrittnummerSichtbarkeitSetzen(breakAngekoppelt);
	}

	public void catchTextAktualisieren(String ersteBreakZeile) {
		// Besser w�re: bestehendes Styling beibehalten
		setPlainText("<b>" + ersteBreakZeile + "</b>");
	};

	public void breakAbkoppeln(BreakSchrittView breakSchrittView) {
		breakAngekoppelt = false;
		schrittnummerSichtbarkeitSetzen(breakAngekoppelt);
	}

	@Override
	public boolean enthaeltAenderungsmarkierungen() {
		return handlingSequenz.enthaeltAenderungsmarkierungen();
	}

	public boolean enthaeltAenderungsmarkierungenInklName() {
		return super.enthaeltAenderungsmarkierungen() ||
				enthaeltAenderungsmarkierungen();
	}

	@Override
	public void zusammenklappenFuerReview() {
		if (!enthaeltAenderungsmarkierungen()) {
			klappen.init(true);
		}
		handlingSequenz.zusammenklappenFuerReview();
	}

	@Override
	public void skalieren(int prozent, int prozentAktuell) {
		super.skalieren(prozent, prozentAktuell);
		layout.setColumnSpec(1, ColumnSpec.decode(umgehungLayout()));
		handlingSequenz.skalieren(prozent, prozentAktuell);
	}


	@Override
	public List<SchrittSequenzView> unterSequenzen() {
		return sequenzenAuflisten(handlingSequenz);
	}

	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		CatchSchrittModel_V001 model = new CatchSchrittModel_V001(
			id,
			getEditorContent(formatierterText),
			getBackground().getRGB(),
			klappen.isSelected(),
			aenderungsart,
			handlingSequenz.generiereSchittSequenzModel(formatierterText),
			breakAngekoppelt,
			getQuellschrittID(),
			getDecorated());
		return model;
	}

	/** Diese spezialisierte Klasse sorgt daf�r, dass das Exceptionhandling abh�ngig von der
	 * Anzahl der Handlingschritte vereinfacht oder als eine normale Schrittsequenz dargsetellt
	 * wird. Besteht das Handling nur aus einem einzelnen einfachen Schritt, sparen wir Trennlinien
	 * und Zusatzplatz im Fu�bereich. Sonst halt nicht.
	 */
	private class HandlingSchrittSequenz extends SchrittSequenzView {

		public HandlingSchrittSequenz(EditorI editor, AbstractSchrittView parent, SchrittSequenzModel_V001 model) {
			super(editor, parent, model);
		}

		public HandlingSchrittSequenz(AbstractSchrittView parent, SchrittID sequenzBasisId, Aenderungsart aenderungsart) {
			super(parent, sequenzBasisId, aenderungsart);
		}

		@Override public AbstractSchrittView schrittAnhaengen(AbstractSchrittView schritt, EditorI editor) {
			AbstractSchrittView ergebnis = super.schrittAnhaengen(schritt, editor);
			rahmenanzeigeAnpassen();
			return ergebnis;
		}

		@Override public AbstractSchrittView schrittZwischenschieben(AbstractSchrittView schritt, RelativeStepPosition insertionPosition, AbstractSchrittView vorgaengerSchritt, EditorI editor) {
			AbstractSchrittView ergebnis = super.schrittZwischenschieben(schritt, insertionPosition, vorgaengerSchritt, editor);
			rahmenanzeigeAnpassen();
			return ergebnis;
		}

		@Override public int schrittEntfernen(AbstractSchrittView schritt) throws EditException {
			int index = super.schrittEntfernen(schritt);
			rahmenanzeigeAnpassen();
			return index;
		}

		@Override
		void schrittnummerSichtbarkeitSetzen(boolean sichtbar) {
			super.schrittnummerSichtbarkeitSetzen(sichtbar && breakAngekoppelt && !istMinimalHandling());
		}

		boolean istMinimalHandling() {
			if (schritte.size() == 1) {
				AbstractSchrittView ersterSchritt = schritte.get(0);
				if (ersterSchritt instanceof EinfacherSchrittView) {
					return true;
				}
			}
			return false;
		}

		void rahmenanzeigeAnpassen() {
			rahmenAnzeigen(!istMinimalHandling());
		}

		private void rahmenAnzeigen(boolean anzeigen) {
			schrittnummerSichtbarkeitSetzen(anzeigen);
			Color hintergrundfarbe = (anzeigen && !klappen.isSelected()) ? DIAGRAMM_LINE_COLOR : Specman.schrittHintergrund();
			schrittPanel.setBackground(hintergrundfarbe);
			String fusszeilenTrennerLayout = (anzeigen && hatNachfolger && !klappen.isSelected()) ? FORMLAYOUT_GAP : ZEILENLAYOUT_INHALT_VERBORGEN;
			layout.setRowSpec(5, RowSpec.decode(fusszeilenTrennerLayout));
			String fusszeilenLayout = (anzeigen && hatNachfolger && !klappen.isSelected()) ? zeilenLayoutSchmalsterSchritt() : ZEILENLAYOUT_INHALT_VERBORGEN;
			layout.setRowSpec(6, RowSpec.decode(fusszeilenLayout));
		}

		private String zeilenLayoutSchmalsterSchritt() {
			int schmalsterSchritt = editContainer.getHeight() > 0 ? editContainer.getHeight() : 25;
			for (AbstractSchrittView schritt: schritte) {
				if (schritt.getDecoratedComponent().getHeight() > 0) { // Noch nicht gerenderte Schritte bleiben unberücksichtigt
					schmalsterSchritt = Math.min(schmalsterSchritt, schritt.getDecoratedComponent().getHeight());
				}
			}
			return "fill:" + schmalsterSchritt + "px";
		}

	}
	//TODO Catchbereich noch nicht definiert
	@Override
	public JComponent getPanel() {
		return null;
	}

}