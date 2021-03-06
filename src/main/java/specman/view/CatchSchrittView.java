package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.CatchSchrittModel_V001;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.SchrittSequenzModel_V001;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.util.List;

public class CatchSchrittView extends AbstractSchrittView {
	public static final int TEXTEINRUECKUNG = SubsequenzSchrittView.TEXTEINRUECKUNG;
	
	final JPanel schrittPanel;
	final JPanel fussPanel;
	final JPanel dreiecksPanel;
	final FormLayout layout;
	final HandlingSchrittSequenz handlingSequenz;
	boolean hatNachfolger;
	KlappButton klappen;
	boolean breakAngekoppelt;

	public CatchSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id, SchrittSequenzModel_V001 handlingModel) {
		super(editor, parent, initialerText, id);
		schrittPanel = new JPanel();
		schrittPanel.setBackground(Color.black);
		layout = new FormLayout(
				umgehungLayout() + ", 10dlu:grow",
				(LINIENBREITE * 2) + "px, " + ZEILENLAYOUT_INHALT_SICHTBAR + ", " + FORMLAYOUT_GAP + ", " + ZEILENLAYOUT_INHALT_SICHTBAR + ", pref:grow, 0px");
		schrittPanel.setLayout(layout);
		
		schrittPanel.add(text.asJComponent(), CC.xy(2, 2));
		
		dreiecksPanel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				dreieckZeichnen((Graphics2D)g);
			}
		};
		dreiecksPanel.setBackground(Specman.schrittHintergrund());
		dreiecksPanel.setLayout(null);
		schrittPanel.add(dreiecksPanel, CC.xy(1, 2));
		
		JPanel doppellinie = new JPanel();
		doppellinie.setBackground(Specman.schrittHintergrund());
		doppellinie.setBorder(new MatteBorder(0, 0, LINIENBREITE, 0, Color.black));
		schrittPanel.add(doppellinie, CC.xyw(1, 1, 2));
		
		fussPanel = new JPanel();
		fussPanel.setBackground(Specman.schrittHintergrund());
		schrittPanel.add(fussPanel, CC.xyw(1, 6, 2));
		
		klappen = new KlappButton(this, dreiecksPanel, layout, 4);
		klappen.addComponentListener(new ComponentAdapter() {
			// Kleine Sch�nheitsgeschichte, vergleiche {@link IfElseSchrittView#addComponentListener}
			@Override public void componentHidden(ComponentEvent e) {
				dreiecksPanel.repaint();
			}
		});

		if (handlingModel != null) {
			handlingSequenz = new HandlingSchrittSequenz(editor, this, handlingModel);
		}
		else {
			handlingSequenz = new HandlingSchrittSequenz(this, id.naechsteEbene());
			handlingSequenz.einfachenSchrittAnhaengen(editor);
			schrittnummerSichtbarkeitSetzen(false);
		}
		schrittPanel.add(handlingSequenz.getContainer(), CC.xywh(1, 4, 2, 1));

	}

	private void dreieckZeichnen(Graphics2D g) {
		int hoehe = text.getHeight();
		int dreieckSpitzeY = hoehe / 2;
		int dreieckSpitzeX = text.getX() - LINIENBREITE;
		g.setStroke(new BasicStroke(1));
		g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawLine(0,  0,  dreieckSpitzeX,  dreieckSpitzeY);
		g.drawLine(dreieckSpitzeX,  dreieckSpitzeY, - LINIENBREITE, hoehe); // Wieso -LINIENBREITE statt 0 ist mir selber nicht ganz klar ;-)
		// Folgende Zeile vergleiche {@link IfElseSchrittView#dreieckZeichnen}
		klappen.repaint();
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
		this(editor, parent, model.inhalt.text, model.id, model.handlingSequenz);
		setBackground(new Color(model.farbe));
		klappen.init(model.zugeklappt);
		breakAngekoppelt = model.breakAngekoppelt;
		schrittnummerSichtbarkeitSetzen(breakAngekoppelt);
		editor.schrittFuerNachinitialisierungRegistrieren(this);
	}

	public CatchSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText) {
		this(editor, parent, initialerText, null, null);
	}
	
	@Override
	public void nachinitialisieren() {
		if (breakAngekoppelt) {
			breakAnkopplungAktualisieren();
		}
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		schrittPanel.setBackground(bg);
		text.setBackground(bg);
		dreiecksPanel.setBackground(bg);
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
	public JComponent getComponent() { return decorated(schrittPanel); }

	@Override
	public AbstractSchrittView findeSchritt(JTextComponent zuletztFokussierterText) {
		return handlingSequenz.findeSchritt(zuletztFokussierterText);
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
	protected List<SchrittSequenzView> unterSequenzen() {
		return sequenzenAuflisten(handlingSequenz);
	}

	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		CatchSchrittModel_V001 model = new CatchSchrittModel_V001(
			id,
			getTextMitAenderungsmarkierungen(formatierterText),
			getBackground().getRGB(),
			getDecorated(),
			klappen.isSelected(),
			handlingSequenz.generiereSchittSequenzModel(formatierterText),
			breakAngekoppelt);
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

		public HandlingSchrittSequenz(AbstractSchrittView parent, SchrittID sequenzBasisId) {
			super(parent, sequenzBasisId);
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

		@Override public int schrittEntfernen(AbstractSchrittView schritt) {
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
			Color hintergrundfarbe = (anzeigen && !klappen.isSelected()) ? Color.black : Specman.schrittHintergrund();
			schrittPanel.setBackground(hintergrundfarbe);
			String fusszeilenTrennerLayout = (anzeigen && hatNachfolger && !klappen.isSelected()) ? FORMLAYOUT_GAP : ZEILENLAYOUT_INHALT_VERBORGEN;
			layout.setRowSpec(5, RowSpec.decode(fusszeilenTrennerLayout));
			String fusszeilenLayout = (anzeigen && hatNachfolger && !klappen.isSelected()) ? zeilenLayoutSchmalsterSchritt() : ZEILENLAYOUT_INHALT_VERBORGEN;
			layout.setRowSpec(6, RowSpec.decode(fusszeilenLayout));
		}

		private String zeilenLayoutSchmalsterSchritt() {
			int schmalsterSchritt = getText().getHeight() > 0 ? getText().getHeight() : 25;
			for (AbstractSchrittView schritt: schritte) {
				if (schritt.getComponent().getHeight() > 0) { // Noch nicht gerenderte Schritte bleiben unber�cksichtigt
					schmalsterSchritt = Math.min(schmalsterSchritt, schritt.getComponent().getHeight());
				}
			}
			return "fill:" + schmalsterSchritt + "px";
		}

	}

}
