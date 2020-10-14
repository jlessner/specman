package specman.view;

import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.TextfeldShef;
import specman.model.Aenderungsmarkierung;
import specman.model.BreakSchrittModel;
import specman.model.CaseSchrittModel;
import specman.model.CatchSchrittModel;
import specman.model.IfElseSchrittModel;
import specman.model.IfSchrittModel;
import specman.model.SchrittModel;
import specman.model.SubsequenzSchrittModel;
import specman.model.TextMitAenderungsmarkierungen;
import specman.model.WhileSchrittModel;
import specman.model.WhileWhileSchrittModel;

import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract public class AbstractSchrittView implements FocusListener, KlappbarerBereichI {
	public static final int LINIENBREITE = 2;
	public static final String FORMLAYOUT_GAP = LINIENBREITE + "px";
	public static final String ZEILENLAYOUT_INHALT_SICHTBAR = "fill:pref:grow";
	public static final String ZEILENLAYOUT_INHALT_VERBORGEN = "0px";
	public static final int SPALTENLAYOUT_UMGEHUNG_GROESSE = 18;
	
	protected static final List<SchrittSequenzView> KEINE_SEQUENZEN = new ArrayList<SchrittSequenzView>();

	protected final TextfeldShef text;
	protected SchrittID id;
	 
	public AbstractSchrittView(EditorI editor) {
		this(editor, null, (SchrittID) null);
	}

	public AbstractSchrittView(EditorI editor, String initialerText, SchrittID id) {
		this.id = id;
		this.text = new TextfeldShef(initialerText, id != null ? id.toString() : null);
		text.addFocusListener(editor);
		text.addFocusListener(this);
	}
	
	public void setId(SchrittID id) {
		this.id = id;
		text.setId(id.toString());
	}
	
	public SchrittID folgeIDInGleicherSequenz() {
		return id.naechsteID();
	}

	public void setPlainText(String plainText) {
		text.setPlainText(plainText);
	}

	public void setPlainText(String plainText, int orientation) {
		text.setPlainText(plainText, orientation);
	}

	public String getPlainText() {
		return text.getText();
	}

	protected void setAenderungsmarkierungen(List<Aenderungsmarkierung> aenderungen) {
		text.setAenderungsmarkierungen(aenderungen);
	}

	public List<Aenderungsmarkierung> findeAenderungsmarkierungen() {
		return text.findeAenderungsmarkierungen(false);
	}
	
	protected TextMitAenderungsmarkierungen getTextMitAenderungsmarkierungen(boolean formatierterText) {
		return text.getTextMitAenderungsmarkierungen(formatierterText);
	}

	public void setBackground(Color bg) {
		text.setBackground(bg);
	}
	
	public Color getBackground() {
		return text.getBackground();
	}

	abstract public Component getComponent();

	public JTextComponent getText() {
		return text;
	}

	public boolean isStrukturiert() { return false; }
	
	@Override public void focusGained(FocusEvent e) {}

	@Override
	public void focusLost(FocusEvent e) {
		//System.out.println(text.getText());
	}

	void schrittnummerSichtbarkeitSetzen(boolean sichtbar) {
		text.schrittnummerAnzeigen(sichtbar);
	}

	abstract public SchrittModel generiereModel(boolean formatierterText);
	
	public static AbstractSchrittView baueSchrittView(EditorI editor, SchrittModel model) {
		if (model instanceof WhileWhileSchrittModel) {
			return new WhileWhileSchrittView(editor, (WhileWhileSchrittModel) model);
		}
		if (model instanceof WhileSchrittModel) {
			return new WhileSchrittView(editor, (WhileSchrittModel) model);
		}
		if (model instanceof IfElseSchrittModel) {
			return new IfElseSchrittView(editor, (IfElseSchrittModel) model);
		}
		if (model instanceof IfSchrittModel) {
			return new IfSchrittView(editor, (IfSchrittModel) model);
		}
		if (model instanceof CaseSchrittModel) {
			return new CaseSchrittView(editor, (CaseSchrittModel) model);
		}
		if (model instanceof SubsequenzSchrittModel) {
			return new SubsequenzSchrittView(editor, (SubsequenzSchrittModel) model);
		}
		if (model instanceof BreakSchrittModel) {
			return new BreakSchrittView(editor, (BreakSchrittModel) model);
		}
		if (model instanceof CatchSchrittModel) {
			return new CatchSchrittView(editor, (CatchSchrittModel) model);
		}
		return new EinfacherSchrittView(editor, model);
	}

	public void geklappt(boolean auf) {}

	public void zusammenklappenFuerReview() {}
	
	public String ersteZeileExtraieren() {
		String[] zeilen = text.getPlainText().split("\n");
		for (String zeile: zeilen) {
			String getrimmteZeile = zeile.trim();
			if (getrimmteZeile.length() > 0)
				return getrimmteZeile;
		}
		return null;
	}

	public boolean enthaeltAenderungsmarkierungen() {
		if (text.findeAenderungsmarkierungen(true).size() > 0)
			return true;
		for (SchrittSequenzView unterSequenz: unterSequenzen()) {
			if (unterSequenz.enthaeltAenderungsmarkierungen())
				return true;
		}
		return false;
	}
	
	public SchrittSequenzView findeSequenz(JTextComponent zuletztFokussierterText) {
		for (SchrittSequenzView unterSequenz: unterSequenzen()) {
			SchrittSequenzView sequenz = unterSequenz.findeSequenz(zuletztFokussierterText);
			if (sequenz != null)
				return sequenz;
		}
		return null;
	}

	public AbstractSchrittView findeSchritt(JTextComponent zuletztFokussierterText) {
		for (SchrittSequenzView unterSequenz: unterSequenzen()) {
			AbstractSchrittView schritt = unterSequenz.findeSchritt(zuletztFokussierterText);
			if (schritt != null)
				return schritt;
		}
		return null;
	}

	public SchrittSequenzView findeElternSequenz(AbstractSchrittView kindSchritt) {
		for (SchrittSequenzView unterSequenz: unterSequenzen()) {
			SchrittSequenzView elternSequenz = unterSequenz.findeElternSequenz(kindSchritt);
			if (elternSequenz != null)
				return elternSequenz;
		}
		return null;
	}

	public BreakSchrittView findeBreakSchritt(String catchText) {
		for (SchrittSequenzView unterSequenz: unterSequenzen()) {
			BreakSchrittView schritt = unterSequenz.findeBreakSchritt(catchText);
			if (schritt != null)
				return schritt;
		}
		return null;
	}

	public boolean istBreakSchrittFuer(String catchText) {
		return false;
	}

	/** Liefert alle in einem Schritt enthalten Untersequenzen, um damit die verschiedenen
	 * Traversierungsfunktionen wie {@link #findeSequenz(JTextComponent)} und {@link #findeSchritt(JTextComponent)}
	 * zu f�ttern. Damit spart man sich das rekursive Absteigen in allen Ableitungen f�r jede dieser
	 * Funktionen zu dublizieren
	 */
	protected List<SchrittSequenzView> unterSequenzen() {
		return KEINE_SEQUENZEN;
	}
	
	/** Bisschen Convenience, um die Funktion unterSequenz als Einzeler schreiben zu k�nnen */
	protected static List<SchrittSequenzView> sequenzenAuflisten(List<? extends SchrittSequenzView> sequenzSammlung, SchrittSequenzView... einzelSequenzen) {
		List<SchrittSequenzView> ergebnis = new ArrayList<SchrittSequenzView>();
		if (sequenzSammlung != null)
			ergebnis.addAll(sequenzSammlung);
		ergebnis.addAll(Arrays.asList(einzelSequenzen));
		return ergebnis;
	}

	protected static List<SchrittSequenzView> sequenzenAuflisten(SchrittSequenzView... einzelSequenzen) {
		return sequenzenAuflisten(null, einzelSequenzen);
	}

	/** Informiert den Schritt dar�ber, dass er gerade aus seiner Sequenz entfernt wird */
	public void entfernen(SchrittSequenzView container) {
		unterSequenzen().forEach(sequenz -> sequenz.entfernen(this));
	}

	public void nachinitialisieren() {}

	public void skalieren(int prozentNeu, int prozentAktuell) {
		text.skalieren(prozentNeu, prozentAktuell);
		unterSequenzen().forEach(sequenz -> sequenz.skalieren(prozentNeu, prozentAktuell));
	}

	public boolean enthaelt(JTextComponent zuletztFokussierterText) {
		return text == zuletztFokussierterText;
	}
	
	static int groesseUmrechnen(int groesse, int prozentNeu, int prozentAktuell) {
		float groesse100Prozent = (float)groesse / prozentAktuell * 100;
		return (int)(groesse100Prozent * prozentNeu / 100);
	}
	
	static String umgehungLayout() {
		return umgehungLayout(SPALTENLAYOUT_UMGEHUNG_GROESSE * Specman.instance().zoomFaktor() / 100);
	}
	
	static String umgehungLayout(int groesse) {
		return "fill:" + groesse + "px";
	}

	public void requestFocus() { text.requestFocus(); }
}
