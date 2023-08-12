package specman.view;

import specman.Aenderungsart;
import specman.EditException;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.BreakSchrittModel_V001;
import specman.model.v001.CaseSchrittModel_V001;
import specman.model.v001.CatchSchrittModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.EinfacherSchrittModel_V001;
import specman.model.v001.IfElseSchrittModel_V001;
import specman.model.v001.IfSchrittModel_V001;
import specman.model.v001.QuellSchrittModel_V001;
import specman.model.v001.SubsequenzSchrittModel_V001;
import specman.model.v001.WhileSchrittModel_V001;
import specman.model.v001.WhileWhileSchrittModel_V001;
import specman.textfield.Indentions;
import specman.textfield.InteractiveStepFragment;
import specman.textfield.TextfieldShef;
import specman.undo.AbstractUndoableInteraktion;
import specman.undo.UndoableSchrittEntferntMarkiert;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static specman.textfield.TextStyles.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE;
import static specman.textfield.TextStyles.Hintergrundfarbe_Standard;
import static specman.view.RelativeStepPosition.After;
import static specman.view.RoundedBorderDecorationStyle.Co;
import static specman.view.RoundedBorderDecorationStyle.Full;
import static specman.view.RoundedBorderDecorationStyle.None;

abstract public class AbstractSchrittView implements KlappbarerBereichI, ComponentListener, FocusListener {
	public static final int LINIENBREITE = 2;
	public static final String FORMLAYOUT_GAP = LINIENBREITE + "px";
	public static final String ZEILENLAYOUT_INHALT_SICHTBAR = "fill:pref:grow";
	public static final String ZEILENLAYOUT_INHALT_VERBORGEN = "0px";
	public static final int SPALTENLAYOUT_UMGEHUNG_GROESSE = 18;

	protected static final List<SchrittSequenzView> KEINE_SEQUENZEN = new ArrayList<SchrittSequenzView>();

	protected final TextfieldShef editContainer;
	protected SchrittID id;
	protected Aenderungsart aenderungsart;
	protected SchrittSequenzView parent;
	protected RoundedBorderDecorator roundedBorderDecorator;
	protected QuellSchrittView quellschritt;

	public AbstractSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialContent, SchrittID id, Aenderungsart aenderungsart) {
		this.id = id;
		this.aenderungsart = aenderungsart;
		this.editContainer = new TextfieldShef(editor, initialContent, id != null ? id.toString() : null);
		this.parent = parent;
		editContainer.addFocusListener(editor);
		editContainer.addFocusListener(this);
		editContainer.addEditComponentListener(this);
	}

	public Aenderungsart getAenderungsart() {
		return aenderungsart;
	}

	public void setAenderungsart(Aenderungsart aenderungsart) {
		this.aenderungsart = aenderungsart;
	}

	public void setId(SchrittID id) {
		this.id = id;
		editContainer.setId(id.toString());
	}

	public SchrittID newStepIDInSameSequence(RelativeStepPosition direction) {
		return direction == RelativeStepPosition.After ? id.naechsteID() : id.sameID();
	}

	public void setPlainText(String plainText) {
		editContainer.setPlainText(plainText);
	}

	public TextfieldShef getshef() {
		return editContainer;
	}

	protected EditorContentModel_V001 getEditorContent(boolean formatierterText) {
		return editContainer.editorContent2Model(formatierterText);
	}

	public void setBackground(Color bg) {
		editContainer.setBackground(bg);
	}

	public Color getBackground() {
		return editContainer.getBackground();
	}

	abstract public JComponent getComponent();

	protected JComponent decorated(JComponent core) {
		return roundedBorderDecorator != null ? roundedBorderDecorator : core;
	}

	public boolean isStrukturiert() { return false; }

	void schrittnummerSichtbarkeitSetzen(boolean sichtbar) {
		editContainer.schrittnummerAnzeigen(sichtbar);
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
		String[] zeilen = editContainer.getPlainText().split("\n");
		for (String zeile: zeilen) {
			String getrimmteZeile = zeile.trim();
			if (getrimmteZeile.length() > 0)
				return getrimmteZeile;
		}
		return null;
	}

	public void setStandardStil() {
		setBackground(Hintergrundfarbe_Standard);
		editContainer.setEditable(true);
		getshef().setStandardStil(id);
		setAenderungsart(null);
	}

	public void setGeloeschtMarkiertStil() {
		setBackground(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
		editContainer.setEditable(false);
		getshef().setGeloeschtMarkiertStil(id);
		setAenderungsart(Aenderungsart.Geloescht);
	}

	public void setZielschrittStil() {
		getshef().setZielschrittStil(getQuellschritt().getId());
		setAenderungsart(Aenderungsart.Zielschritt);
	}

	public AbstractUndoableInteraktion alsGeloeschtMarkieren(EditorI editor) {
		editContainer.setEditable(false);
		setGeloeschtMarkiertStil();
		return new UndoableSchrittEntferntMarkiert(this, editor);
	}

	public void aenderungsmarkierungenEntfernen() {
		setStandardStil();
	}

	public boolean enthaeltAenderungsmarkierungen() {
		if (editContainer.enthaeltAenderungsmarkierungen())
			return true;
		for (SchrittSequenzView unterSequenz: unterSequenzen()) {
			if (unterSequenz.enthaeltAenderungsmarkierungen())
				return true;
		}
		return false;
	}

	public AbstractSchrittView findeSchritt(InteractiveStepFragment fragment) {
		for (SchrittSequenzView unterSequenz: unterSequenzen()) {
			AbstractSchrittView schritt = unterSequenz.findeSchritt(fragment);
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
	 * Traversierungsfunktionen wie {@link #findeSchritt(InteractiveStepFragment)} zu füttern. Damit
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
		editContainer.skalieren(prozentNeu, prozentAktuell);
		editContainer.updateBounds();
		if (roundedBorderDecorator != null) {
			roundedBorderDecorator.skalieren(prozentNeu, editContainer.getStepNumberBounds().getHeight());
		}
		unterSequenzen().forEach(sequenz -> sequenz.skalieren(prozentNeu, prozentAktuell));
	}

	public boolean enthaelt(InteractiveStepFragment fragment) {
		return editContainer.enthaelt(fragment);
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

	public void requestFocus() { editContainer.requestFocus(); }

	public JComponent toggleBorderType() {
		JComponent toggleResult;
		if (roundedBorderDecorator == null) {
			JComponent coreComponent = getComponent();
			roundedBorderDecorator = new RoundedBorderDecorator(coreComponent, editContainer.getStepNumberBounds().getHeight());
			RoundedBorderDecorationStyle requiredDecorationStyle =
					parent.deriveDecorationStyleFromPosition(this);
			roundedBorderDecorator.setStyle(requiredDecorationStyle);
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
		editContainer.updateDecorationIndentions(indentions);
	}

	public void updateTextfieldDecorationIndentions() {
		Indentions indentions = new Indentions(getDecorated());
		updateTextfieldDecorationIndentions(indentions);
	}

	public RoundedBorderDecorationStyle getDecorated() {
		if (roundedBorderDecorator == null) {
			return None;
		}
		return roundedBorderDecorator.getStyle();
	}

	public void decorateAsFollower(RoundedBorderDecorationStyle predecessorDeco) {
		if (getDecorated() != None) {
			roundedBorderDecorator.setStyle(predecessorDeco == None ? Full : Co);
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
		return editContainer;
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
			getshef().wrapSchrittnummerAsDeleted();
		}
		if (getAenderungsart() == Aenderungsart.Quellschritt) {
			getshef().wrapSchrittnummerAsQuelle(((QuellSchrittView)this).getZielschrittID());
		}
		if (getAenderungsart() == Aenderungsart.Zielschritt) {
			getshef().wrapSchrittnummerAsZiel(getQuellschritt().getId());
		}
	}

	public void viewsNachinitialisieren() {
		if (aenderungsart != null) {
			switch(aenderungsart) {
				case Geloescht:
					setGeloeschtMarkiertStil();
					editContainer.setEditable(false);
					break;
				case Quellschritt:
					((QuellSchrittView)this).setQuellStil();
					editContainer.setEditable(false);
					break;
				case Zielschritt:
					setZielschrittStil();
					break;
			}
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

	public void aenderungenUebernehmen(EditorI editor) throws EditException {
		textAenderungenUebernehmen();
		if (aenderungsart != null) {
			switch (aenderungsart) {
				case Hinzugefuegt:
					aenderungsmarkierungenEntfernen();
					break;
				case Geloescht:
				case Quellschritt:
					getParent().schrittEntfernen(this);
					break;
				case Zielschritt:
					setQuellschritt(null);
					setStandardStil();
			}
			setAenderungsart(null);
		}
	}

	protected void textAenderungenUebernehmen() {
		getshef().aenderungsmarkierungenUebernehmen();
	}

	public void aenderungenVerwerfen(EditorI editor) throws EditException {
		aenderungsmarkierungenVerwerfen();
		if (aenderungsart != null) {
			switch (aenderungsart) {
				case Hinzugefuegt:
					getParent().schrittEntfernen(this);
					break;
				case Geloescht:
					aenderungsmarkierungenEntfernen();
					break;
				case Quellschritt:
					break;
				case Zielschritt:
					getParent().schrittEntfernen(this);
					setId(getQuellschritt().newStepIDInSameSequence(After));
					setParent(getQuellschritt().getParent());
					getQuellschritt().getParent().schrittZwischenschieben(this, After, getQuellschritt(), editor);
					getQuellschritt().getParent().schrittEntfernen(getQuellschritt());
					setQuellschritt(null);
					this.setStandardStil();
			}
			setAenderungsart(null);
		}

	}

	protected void aenderungsmarkierungenVerwerfen() {
		getshef().aenderungsmarkierungenVerwerfen();
	}

	@Override
	public void componentResized(ComponentEvent e) {
		editContainer.updateBounds();
	}

	@Override public void componentMoved(ComponentEvent e) {
	}

	@Override public void componentShown(ComponentEvent e) {
	}

	@Override public void componentHidden(ComponentEvent e) {
	}

	@Override public void focusGained(FocusEvent e) {}

	@Override
	public void focusLost(FocusEvent e) {}

}