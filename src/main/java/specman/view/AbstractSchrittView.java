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
import specman.pdf.RoundedBorderShape;
import specman.pdf.Shape;
import specman.editarea.EditContainer;
import specman.editarea.Indentions;
import specman.editarea.InteractiveStepFragment;
import specman.editarea.StepnumberLink;
import specman.editarea.TextEditArea;
import specman.undo.AbstractUndoableInteraction;
import specman.undo.UndoableSchrittEntferntMarkiert;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static specman.editarea.TextStyles.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE;
import static specman.editarea.TextStyles.BACKGROUND_COLOR_STANDARD;
import static specman.editarea.TextStyles.TEXT_BACKGROUND_COLOR_STANDARD;
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

	protected final EditContainer editContainer;
	protected SchrittID id;
	protected Aenderungsart aenderungsart;
	protected SchrittSequenzView parent;
	protected RoundedBorderDecorator roundedBorderDecorator;
	protected QuellSchrittView quellschritt;

	private final java.util.List<TextEditArea> referencedByTextEditAreas = new ArrayList<>();

	public AbstractSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialContent, SchrittID id, Aenderungsart aenderungsart) {
		this.id = id;
		this.aenderungsart = aenderungsart;
		this.editContainer = new EditContainer(editor, initialContent, id != null ? id.toString() : null);
		this.parent = parent;
		editContainer.addEditAreasFocusListener(editor);
		editContainer.addEditAreasFocusListener(this);
		editContainer.addEditComponentListener(this);
	}

	public Aenderungsart getAenderungsart() {
		return aenderungsart;
	}

	public void setAenderungsart(Aenderungsart aenderungsart) {
		this.aenderungsart = aenderungsart;
	}

	public void setId(SchrittID id) {
		SchrittID oldStepID = this.id;

		this.id = id;
		editContainer.setId(id.toString());

		if (!oldStepID.equals(id)) {
			for (TextEditArea textEditArea : referencedByTextEditAreas) {
				textEditArea.updateStepnumberLink(oldStepID.toString(), id.toString());
			}
		}
	}

	public SchrittID newStepIDInSameSequence(RelativeStepPosition direction) {
		return direction == RelativeStepPosition.After ? id.naechsteID() : id.sameID();
	}

	public void setPlainText(String plainText) {
		editContainer.setPlainText(plainText);
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

	public void scrollTo() {
		editContainer.scrollRectToVisible(editContainer.getBounds());
		editContainer.setCaretAtStart();
	}

	public specman.pdf.Shape getShape() {
		return decoratedShape(new specman.pdf.Shape(getPanel(), this)
			.withBackgroundColor(editContainer.getBackground())
			.add(editContainer.getShape()));
	}

	protected Shape decoratedShape(Shape undecoratedShape) {
		return roundedBorderDecorator != null
			? new RoundedBorderShape(roundedBorderDecorator, undecoratedShape)
			: undecoratedShape;
	}

	public JComponent getDecoratedComponent() {
		return decorated(getPanel());
	}

	protected JComponent decorated(JComponent undecoratedComponent) {
		return roundedBorderDecorator != null ? roundedBorderDecorator : undecoratedComponent;
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
		setBackground(BACKGROUND_COLOR_STANDARD);
		editContainer.aenderungsmarkierungenEntfernen(id);
		setAenderungsart(null);
	}

	public void setGeloeschtMarkiertStil() {
		setBackground(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
		editContainer.setGeloeschtMarkiertStil(id);
		setAenderungsart(Aenderungsart.Geloescht);
	}

	public void setZielschrittStil() {
		editContainer.setZielschrittStil(getQuellschritt().getId());
		setAenderungsart(Aenderungsart.Zielschritt);
	}

	public AbstractUndoableInteraction alsGeloeschtMarkieren(EditorI editor) {
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

	public List<SchrittSequenzView> unterSequenzen() {
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
			JComponent coreComponent = getDecoratedComponent();
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

	public EditContainer getTextShef() {
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
			editContainer.wrapSchrittnummerAsDeleted();
		}
		if (getAenderungsart() == Aenderungsart.Quellschritt) {
			editContainer.wrapSchrittnummerAsQuelle(((QuellSchrittView)this).getZielschrittID());
		}
		if (getAenderungsart() == Aenderungsart.Zielschritt) {
			editContainer.wrapSchrittnummerAsZiel(getQuellschritt().getId());
		}
	}

	public void viewsNachinitialisieren() {
		if (aenderungsart != null) {
            switch (aenderungsart) {
                case Geloescht -> setGeloeschtMarkiertStil();
                case Quellschritt -> ((QuellSchrittView) this).setQuellStil();
                case Zielschritt -> setZielschrittStil();
            }
		}
		registerAllExistingStepnumbers();
	}

	/**
	 * Registers all stepnumbers found in all editAreas.
	 * <p>
	 * This is needed for loading diagrams since the references between stepnumberLinks
	 * and its referenced stepnumber are not saved.
	 * <p>
	 * This can be further optimized by using a HashMap as a cache to prevent
	 * calling {@link Specman#findStepByStepID(String)} more than once for the same step.
	 * However, to benefit from such a cache it would need to be shared with other {@link AbstractSchrittView}s
	 */
	protected void registerAllExistingStepnumbers() {
		EditorI editor = Specman.instance();
		HashMap<TextEditArea, List<String>> stepnumberLinks = new HashMap<>();
		editContainer.findStepnumberLinkIDs(stepnumberLinks);
		for (Map.Entry<TextEditArea, List<String>> listEntry : stepnumberLinks.entrySet()) {
			TextEditArea referencingTextEditArea = listEntry.getKey();
			List<String> stepIDs = listEntry.getValue();

			for (String stepID : stepIDs) {
				if (!StepnumberLink.isStepnumberLinkDefect(stepID)) {
					AbstractSchrittView step = editor.findStepByStepID(stepID);
					step.registerStepnumberLink(referencingTextEditArea);
				}
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

	public int aenderungenUebernehmen(EditorI editor) throws EditException {
		int changesMade = textAenderungenUebernehmen();
		if (aenderungsart != null) {
			switch (aenderungsart) {
				case Hinzugefuegt:
					aenderungsmarkierungenEntfernen();
					break;
				case Geloescht:
				case Quellschritt:
					markStepnumberLinksAsDefect();
					getParent().schrittEntfernen(this);
					break;
				case Zielschritt:
					setQuellschritt(null);
					setStandardStil();
			}
			setAenderungsart(null);
			changesMade++;
		}
		return changesMade;
	}

	protected int textAenderungenUebernehmen() {
		return editContainer.aenderungsmarkierungenUebernehmen();
	}

	public int aenderungenVerwerfen(EditorI editor) throws EditException {
		int changesReverted = aenderungsmarkierungenVerwerfen();
		if (aenderungsart != null) {
			switch (aenderungsart) {
				case Hinzugefuegt:
                    markStepnumberLinksAsDefect();
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
			changesReverted++;
		}
		return changesReverted;
	}

	protected int  aenderungsmarkierungenVerwerfen() {
		return editContainer.aenderungsmarkierungenVerwerfen();
	}

	public void registerStepnumberLink(TextEditArea textEditArea) {
		referencedByTextEditAreas.add(textEditArea);
	}

	public void unregisterStepnumberLink(TextEditArea textEditArea) {
		if (!referencedByTextEditAreas.remove(textEditArea)) {
			throw new IllegalArgumentException(
					"The referenced TextEditArea '" + textEditArea.getPlainText() + "' was not registered." +
					" If there was a recent Undo/Redo, check if registerStepnumberLink() was called.");
		}
	}

	public void markStepnumberLinksAsDefect() {
		String id = getId().toString();
        for (TextEditArea referencedByTextEditArea : referencedByTextEditAreas) {
            referencedByTextEditArea.markStepnumberLinkAsDefect(id);
        }
	}

	public boolean hasStepnumberLinks() {
		return !referencedByTextEditAreas.isEmpty();
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

	@Override
	public String toString() {
		return id + " - " + getTextShef().getPlainText();
	}

}