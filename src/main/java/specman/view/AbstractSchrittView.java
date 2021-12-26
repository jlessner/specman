package specman.view;

import specman.Aenderungsart;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.*;
import specman.textfield.Indentions;
import specman.textfield.TextfieldShef;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static specman.view.RelativeStepPosition.After;
import static specman.view.RoundedBorderDecorationStyle.Co;
import static specman.view.RoundedBorderDecorationStyle.Full;
import static specman.view.RoundedBorderDecorationStyle.None;

abstract public class AbstractSchrittView implements FocusListener, KlappbarerBereichI {
	public static final int LINIENBREITE = 2;
	public static final String FORMLAYOUT_GAP = LINIENBREITE + "px";
	public static final String ZEILENLAYOUT_INHALT_SICHTBAR = "fill:pref:grow";
	public static final String ZEILENLAYOUT_INHALT_VERBORGEN = "0px";
	public static final int SPALTENLAYOUT_UMGEHUNG_GROESSE = 18;

	protected static final List<SchrittSequenzView> KEINE_SEQUENZEN = new ArrayList<SchrittSequenzView>();

	protected final TextfieldShef text;
	protected SchrittID id;
	protected Aenderungsart aenderungsart;
	protected SchrittSequenzView parent;
	protected RoundedBorderDecorator roundedBorderDecorator;
	protected QuellSchrittView quellschritt;

	public AbstractSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id, Aenderungsart aenderungsart) {
		this.id = id;
		this.aenderungsart = aenderungsart;
		this.text = new TextfieldShef(editor, initialerText, id != null ? id.toString() : null);
		this.parent = parent;
		text.addFocusListener(editor);
		text.addFocusListener(this);
	}

	public Aenderungsart getAenderungsart() {
		return aenderungsart;
	}

	public void setAenderungsart(Aenderungsart aenderungsart) {
		this.aenderungsart = aenderungsart;
	}

	public void setId(SchrittID id) {
		this.id = id;
		text.setId(id.toString());
	}

	public SchrittID newStepIDInSameSequence(RelativeStepPosition direction) {
		return direction == RelativeStepPosition.After ? id.naechsteID() : id.sameID();
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

	public TextfieldShef getshef() {
		return text;
	}

	protected void setAenderungsmarkierungen(List<Aenderungsmarkierung_V001> aenderungen) {
		text.setAenderungsmarkierungen(aenderungen);
	}

	public List<Aenderungsmarkierung_V001> findeAenderungsmarkierungen() {
		return text.findeAenderungsmarkierungen(false);
	}
	
	protected TextMitAenderungsmarkierungen_V001 getTextMitAenderungsmarkierungen(boolean formatierterText) {
		return text.getTextMitAenderungsmarkierungen(formatierterText);
	}

	public void setBackground(Color bg) {
		text.setBackground(bg);
	}
	
	public Color getBackground() {
		return text.getBackground();
	}

	abstract public JComponent getComponent();

	protected JComponent decorated(JComponent core) {
		return roundedBorderDecorator != null ? roundedBorderDecorator : core;
	}

	public JTextComponent getText() {
		return text.getTextComponent();
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

	abstract public AbstractSchrittModel_V001 generiereModel(boolean formatierterText);
	
	public static AbstractSchrittView baueSchrittView(EditorI editor, SchrittSequenzView parent, AbstractSchrittModel_V001 model) {
		if (model instanceof WhileWhileSchrittModel_V001) {
			return new WhileWhileSchrittView(editor, parent, (WhileWhileSchrittModel_V001) model);
		}
		if (model instanceof WhileSchrittModel_V001) {
			return new WhileSchrittView(editor, parent, (WhileSchrittModel_V001) model);
		}
		if (model instanceof IfElseSchrittModel_V001) {
			return new IfElseSchrittView(editor, parent, (IfElseSchrittModel_V001) model);
		}
		if (model instanceof IfSchrittModel_V001) {
			return new IfSchrittView(editor, parent, (IfSchrittModel_V001) model);
		}
		if (model instanceof CaseSchrittModel_V001) {
			return new CaseSchrittView(editor, parent, (CaseSchrittModel_V001) model);
		}
		if (model instanceof SubsequenzSchrittModel_V001) {
			return new SubsequenzSchrittView(editor, parent, (SubsequenzSchrittModel_V001) model);
		}
		if (model instanceof BreakSchrittModel_V001) {
			return new BreakSchrittView(editor, parent, (BreakSchrittModel_V001) model);
		}
		if (model instanceof CatchSchrittModel_V001) {
			return new CatchSchrittView(editor, parent, (CatchSchrittModel_V001) model);
		}
		//TODO TEST
		if (model instanceof QuellSchrittModel_V001){
			return new QuellSchrittView(editor, parent, (QuellSchrittModel_V001) model);
		}
		// TEST ENDE
		return new EinfacherSchrittView(editor, parent, (EinfacherSchrittModel_V001)model);
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

	public void setStandardStil() {
		setBackground(TextfieldShef.Hintergrundfarbe_Standard);
		getText().setEditable(true);
		getshef().setStandardStil(getId());
		setAenderungsart(null);
	}

	public void setZielschrittStil() {
		getshef().setZielschrittStil(getQuellschritt().getId());
		setAenderungsart(Aenderungsart.Zielschritt);
	}

	public void setGeloeschtMarkiertStil() {
		getshef().setGeloeschtMarkiertStil();
		setBackground(TextfieldShef.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
		setAenderungsart(Aenderungsart.Geloescht);
		getText().setEnabled(false);
	}

	public void setNichtGeloeschtMarkiertStil() {
		getshef().setNichtGeloeschtMarkiertStil();
		setBackground(TextfieldShef.Hintergrundfarbe_Standard);
		setAenderungsart(null);
		getText().setEnabled(true);
	}

	public void alsGeloeschtMarkieren() {
		setGeloeschtMarkiertStil();
		getText().setEditable(false);
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

	public AbstractSchrittView findeSchritt(JTextComponent zuletztFokussierterText) {
		for (SchrittSequenzView unterSequenz: unterSequenzen()) {
			AbstractSchrittView schritt = unterSequenz.findeSchritt(zuletztFokussierterText);
			if (schritt != null)
				return schritt;
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
	 * Traversierungsfunktionen wie {@link #findeSchritt(JTextComponent)} zu füttern. Damit
	 * spart man sich das rekursive Absteigen in allen Ableitungen für jede dieser
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
		if (roundedBorderDecorator != null) {
			roundedBorderDecorator.skalieren(prozentNeu);
		}
		text.skalieren(prozentNeu, prozentAktuell);
		unterSequenzen().forEach(sequenz -> sequenz.skalieren(prozentNeu, prozentAktuell));
	}

	public boolean enthaelt(JTextComponent zuletztFokussierterText) {
		return text.getTextComponent() == zuletztFokussierterText;
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

	public JComponent toggleBorderType() {
		JComponent toggleResult;
		if (roundedBorderDecorator == null) {
			JComponent coreComponent = getComponent();
			roundedBorderDecorator = new RoundedBorderDecorator(coreComponent);
			boolean withTopInset = decorationRequiresTopInset();
			roundedBorderDecorator.withTopInset(withTopInset);
			toggleResult = roundedBorderDecorator;
		}
		else {
			JComponent coreComponent = roundedBorderDecorator.getDecoratedComponent();
			roundedBorderDecorator.remove(coreComponent);
			roundedBorderDecorator = null;
			toggleResult = coreComponent;
		}
		updateTextfieldDecorationIndentions();
		return toggleResult;
	}

	private boolean decorationRequiresTopInset() {
		return parent.decorationRequiresTopInset(this);
	}

	protected void updateTextfieldDecorationIndentions(Indentions indentions) {
		text.updateDecorationIndentions(indentions);
	}

	public void updateTextfieldDecorationIndentions() {
		Indentions indentions = new Indentions(getDecorated());
		updateTextfieldDecorationIndentions(indentions);
	}

	public RoundedBorderDecorationStyle getDecorated() {
		if (roundedBorderDecorator == null) {
			return None;
		}
		return roundedBorderDecorator.withTopInset() ? Full : Co;
	}

	public void decorateAsFollower(RoundedBorderDecorationStyle predecessorDeco) {
		if (getDecorated() != None) {
			roundedBorderDecorator.withTopInset(predecessorDeco == None);
		}
	}

	public void initInheritedTextFieldIndentions() {
		AbstractSchrittView decoratedParent = parent.findFirstDecoratedParent();
		if (decoratedParent != null) {
			decoratedParent.updateTextfieldDecorationIndentions();
		}
	}

	public SchrittSequenzView getParent() { return parent; }

	public void setParent(SchrittSequenzView parent){
		this.parent = parent;
	}

	public TextfieldShef getTextShef() {
		return text;
	}

	public SchrittID getId() {
		return id;
	}
	
	public abstract JComponent getPanel();

	public void setQuellschritt(QuellSchrittView quellschritt){
		this.quellschritt=quellschritt;
	}

	public QuellSchrittView getQuellschritt(){
		return quellschritt;
	}

	public SchrittID getQuellschrittID(){
		return quellschritt!=null?quellschritt.getId():null;
	}

	public void resyncSchrittnummerStil() {
		if (getAenderungsart() == Aenderungsart.Geloescht) {
			getshef().schrittNummer.setText("<html><body><span style='text-decoration: line-through;'>" + getshef().schrittNummer.getText() + "</span></body></html>");
		}
		if (getAenderungsart() == Aenderungsart.Quellschritt) {
			getshef().schrittNummer.setText("<html><body><span style='text-decoration: line-through;'>" + getshef().schrittNummer.getText() + "</span><span>&rArr</span><span>" + ((QuellSchrittView)this).getZielschrittID() + "</span></body></html>");
		}
		if (getAenderungsart() == Aenderungsart.Zielschritt) {
			getshef().schrittNummer.setText("<html><body><span>" + getshef().schrittNummer.getText()+"</span><span>&lArr</span><span style='text-decoration: line-through;'>" + getQuellschritt().getId() + "</span></body></html>");
		}
	}

	public void viewsNachinitialisieren() {
		switch(aenderungsart) {
			case Geloescht:
				setGeloeschtMarkiertStil();
				getshef().getTextComponent().setEditable(false);
				break;
			case Quellschritt:
				((QuellSchrittView)this).setQuellStil();
				getshef().getTextComponent().setEditable(false);
				break;
			case Zielschritt:
				setZielschrittStil();
				break;
		}
	}

	public AbstractSchrittView findeSchrittZuId(SchrittID id) {
		return (this.id.equals(id)) ? this : null;
	}

	protected AbstractSchrittView findeSchrittZuIdIncludingSubSequences(SchrittID id, SchrittSequenzView... subsequenzen) {
		AbstractSchrittView result = (this.id.equals(id)) ? this : null;
		if (result == null) {
			for (SchrittSequenzView subsequenz: subsequenzen) {
				result = subsequenz.findeSchrittZuId(id);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}

}
