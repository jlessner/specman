package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import specman.Aenderungsart;
import specman.EditException;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.SchrittSequenzModel_V001;
import specman.textfield.Indentions;
import specman.textfield.InteractiveStepFragment;
import specman.textfield.TextfieldShef;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static specman.Specman.initialtext;
import static specman.view.RelativeStepPosition.After;
import static specman.view.RelativeStepPosition.Before;
import static specman.view.RoundedBorderDecorationStyle.Co;
import static specman.view.RoundedBorderDecorationStyle.Full;
import static specman.view.RoundedBorderDecorationStyle.None;

public class SchrittSequenzView {
	public static final String ZEILENLAYOUT_GAP = AbstractSchrittView.FORMLAYOUT_GAP;
	public static final String ZEILENLAYOUT_SCHRITT = "fill:pref";
	public static final String ZEILENLAYOUT_LETZTER_SCHRITT = "fill:pref:grow";
	public static final String ZEILENLAYOUT_CATCHBEREICH = "pref";

	SchrittID sequenzBasisId;
	Aenderungsart aenderungsart;
	final JPanel sequenzBereich;
	final CatchBereich catchBereich;
	final JPanel panel;
	//CopyOnWriteArrayList Arraylisten ermöglichen das removen von Schritten während man eine Liste durchläuft
	//wird benötigt um mehre als gelöscht markierte Schritte auf einmal zu löschen
	public final List<AbstractSchrittView> schritte = new CopyOnWriteArrayList<AbstractSchrittView>();
	final FormLayout sequenzbereichLayout;
	boolean schrittnummernSichtbar = true;
	final FormLayout huellLayout;
	final AbstractSchrittView parent;
	
	public SchrittSequenzView() {
		this(null, new SchrittID(0), null);
	}
	
	public SchrittSequenzView(AbstractSchrittView parent, SchrittID sequenzBasisId, Aenderungsart aenderungsart) {
		this.parent = parent;
		panel = new JPanel();
		huellLayout = new FormLayout("10px:grow", ZEILENLAYOUT_LETZTER_SCHRITT + ", " + ZEILENLAYOUT_CATCHBEREICH);
		panel.setLayout(huellLayout);
		panel.setBackground(Specman.schrittHintergrund());
		this.sequenzBasisId = sequenzBasisId;
		sequenzBereich = new JPanel();
		sequenzBereich.setBackground(Color.black);
		//sequenzBereich.setBackground(Color.white); // Nur um zu schauen, wie dekorierte Schritte ohne Trennlinien aussehen
		sequenzbereichLayout = new FormLayout("10px:grow");
		sequenzBereich.setLayout(sequenzbereichLayout);
		panel.add(sequenzBereich, CC.xy(1, 1));
		catchBereich = new CatchBereich();
		panel.add(catchBereich, CC.xy(1, 2));
	}
	
	public Aenderungsart getAenderungsart() {
		return aenderungsart;
	}

	public void setAenderungsart(Aenderungsart aenderungsart) {
		this.aenderungsart = aenderungsart;
	}

	public List<AbstractSchrittView> getSchritte(){
		return schritte;
	}

	public SchrittSequenzView(EditorI editor, AbstractSchrittView parent, SchrittSequenzModel_V001 model) {
		this(parent, model.id, model.aenderungsart);
		for (AbstractSchrittModel_V001 schritt : model.schritte) {
			AbstractSchrittView schrittView = AbstractSchrittView.baueSchrittView(editor, this, schritt);
			schrittAnhaengen(schrittView, editor);
			// TODO JL: Das hier ist schön einfach hinzuschreiben, aber ziemlich ineffizient
			// Wir sollten schauen, ob das herstellen einer *initialen* Dekoration nicht leichtgewichtiger geht
			if (schritt.decorationStyle != None) {
				toggleBorderType(schrittView);
			}
		}
		for (AbstractSchrittModel_V001 catchSchritt : model.catchBloecke) {
			CatchSchrittView schrittView = (CatchSchrittView) AbstractSchrittView.baueSchrittView(editor, this, catchSchritt);
			catchAnhaengen(schrittView, editor);
		}
		catchBereich.umgehungBreiteSetzen(model.catchBloeckeUmgehungBreite);
		catchBereich.klappen.init(model.catchBloeckeZugeklappt);
	}
	
	public JPanel getContainer() {
		return panel;
	}

	private SchrittID naechsteSchrittID() {
		if (schritte.size() > 0)
			return schritte.get(schritte.size() - 1).newStepIDInSameSequence(After);
		return sequenzBasisId.naechsteID();
	}
	
	void schrittnummerSichtbarkeitSetzen(boolean sichtbar) {
		schrittnummernSichtbar = sichtbar;
		for (AbstractSchrittView schritt: schritte) {
			schritt.schrittnummerSichtbarkeitSetzen(sichtbar);
		}
	}

	public AbstractSchrittView einfachenSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("Neuer Schritt " + (schritte.size() + 1));
		EinfacherSchrittView schritt = new EinfacherSchrittView(editor, this, initialerText, naechsteSchrittID(), Specman.initialArt());
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView whileSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("Neue Schleife " + (schritte.size() + 1));
		WhileSchrittView schritt = new WhileSchrittView(editor, this, initialerText, naechsteSchrittID(), Specman.initialArt());
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView whileWhileSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("Neue Schleife " + (schritte.size() + 1));
		WhileWhileSchrittView schritt = new WhileWhileSchrittView(editor, this, initialerText, naechsteSchrittID(), Specman.initialArt());
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView ifElseSchrittAnhaengen(EditorI editor) {
		String initialerText = TextfieldShef.center("If-Else " + (schritte.size()+1));
		IfElseSchrittView schritt = new IfElseSchrittView(editor, this, initialerText, naechsteSchrittID(), Specman.initialArt());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView ifSchrittAnhaengen(EditorI editor) {
		String initialerText = TextfieldShef.center("If " + (schritte.size()+1));
		IfSchrittView schritt = new IfSchrittView(editor, this, initialerText, naechsteSchrittID(), Specman.initialArt());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView caseSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("Case-" + (schritte.size()+1));
		CaseSchrittView schritt = new CaseSchrittView(editor, this, initialerText, naechsteSchrittID(), Specman.initialArt());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView subsequenzSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("<b>Subsequenz " + (schritte.size()+1) + "<b>");
		SubsequenzSchrittView schritt = new SubsequenzSchrittView(editor, this, initialerText, naechsteSchrittID(), Specman.initialArt());
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView breakSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("<b>Exception " + (schritte.size()+1) + "<b>");
		BreakSchrittView schritt = new BreakSchrittView(editor, this, initialerText, naechsteSchrittID(), Specman.initialArt());
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView catchSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("<b>Catch " + (schritte.size()+1) + "<b>");
		CatchSchrittView schritt = new CatchSchrittView(editor, this, initialerText, naechsteSchrittID(), Specman.initialArt(), null);
		return catchAnhaengen(schritt, editor);
	}

	private void updateLayoutRowspecsForAllsStepsAndGaps() {
		for (int i = 0; i < schritte.size(); i++) {
			sequenzbereichLayout.setRowSpec(i*2 + 1, rowspec4step(i));
			if (i > 0) {
				AbstractSchrittView schritt = schritte.get(i);
				String rowSpec = schritt.getDecorated() == Co
						? AbstractSchrittView.ZEILENLAYOUT_INHALT_VERBORGEN
						: ZEILENLAYOUT_GAP;
				sequenzbereichLayout.setRowSpec(i*2, RowSpec.decode(rowSpec));
			}
		}
	}

	private RowSpec rowspec4step(int stepIndex) {
		return (stepIndex == schritte.size()-1)
			? RowSpec.decode(ZEILENLAYOUT_LETZTER_SCHRITT)
			: RowSpec.decode(ZEILENLAYOUT_SCHRITT);
	}

	private CellConstraints constraints4step(int stepIndex) {
		return CC.xy(1, stepIndex * 2 + 1);
	}

	public AbstractSchrittView schrittAnhaengen(final AbstractSchrittView schritt, EditorI editor) {
		schritt.schrittnummerSichtbarkeitSetzen(schrittnummernSichtbar);
		if (schritte.size() != 0) {
			sequenzbereichLayout.appendRow(RowSpec.decode(ZEILENLAYOUT_GAP));
		}
		sequenzbereichLayout.appendRow(RowSpec.decode(ZEILENLAYOUT_SCHRITT));
		sequenzBereich.add(schritt.getComponent(), constraints4step(schritte.size()));
		schritte.add(schritt);
		updateLayoutRowspecsForAllsStepsAndGaps();
		return schritt;
	}

	private AbstractSchrittView catchAnhaengen(CatchSchrittView schritt, EditorI editor) {
		catchBereich.catchAnhaengen(schritt, editor);
		return schritt;
	}

	public AbstractSchrittView einfachenSchrittZwischenschieben(RelativeStepPosition insertionPosition,
			AbstractSchrittView referenceStep, EditorI editor) {
		String initialerText = initialtext("Neuer Schritt " + (schritte.size() + 1));
		EinfacherSchrittView schritt = new EinfacherSchrittView
				(editor, this, initialerText, referenceStep.newStepIDInSameSequence(insertionPosition), Specman.initialArt());
		return schrittZwischenschieben(schritt, insertionPosition, referenceStep, editor);
	}

	public AbstractSchrittView whileSchrittZwischenschieben(RelativeStepPosition insertionPosition,
			AbstractSchrittView referenceStep, EditorI editor) {
		String initialerText = initialtext("Neue Schleife " + (schritte.size() + 1));
		WhileSchrittView schritt = new WhileSchrittView(editor, this, initialerText, referenceStep.newStepIDInSameSequence(insertionPosition), Specman.initialArt());
		return schrittZwischenschieben(schritt, insertionPosition, referenceStep, editor);
	}

	public AbstractSchrittView whileWhileSchrittZwischenschieben(RelativeStepPosition insertionPosition,
			AbstractSchrittView referenceStep, EditorI editor) {
		String initialerText = initialtext("Neue Schleife " + (schritte.size() + 1));
		WhileWhileSchrittView schritt = new WhileWhileSchrittView(editor, this, initialerText, referenceStep.newStepIDInSameSequence(insertionPosition), Specman.initialArt());
		return schrittZwischenschieben(schritt, insertionPosition, referenceStep, editor);
	}

	public AbstractSchrittView ifElseSchrittZwischenschieben(RelativeStepPosition insertionPosition,
			AbstractSchrittView referenceStep, EditorI editor) {
		String initialerText = TextfieldShef.center("Neue Bedingung " + (schritte.size()+1));
		IfElseSchrittView schritt = new IfElseSchrittView(editor, this, initialerText, referenceStep.newStepIDInSameSequence(insertionPosition), Specman.initialArt());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittZwischenschieben(schritt, insertionPosition, referenceStep, editor);
	}

	public AbstractSchrittView ifSchrittZwischenschieben(RelativeStepPosition insertionPosition,
			AbstractSchrittView referenceStep, EditorI editor) {
		String initialerText = TextfieldShef.center("Neue Bedingung " + (schritte.size()+1));
		IfSchrittView schritt = new IfSchrittView(editor, this, initialerText, referenceStep.newStepIDInSameSequence(insertionPosition), Specman.initialArt());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittZwischenschieben(schritt, insertionPosition, referenceStep, editor);
	}

	public AbstractSchrittView caseSchrittZwischenschieben(RelativeStepPosition insertionPosition,
			AbstractSchrittView referenceStep, EditorI editor) {
		String initialerText = initialtext("Case-" + (schritte.size()+1));
		CaseSchrittView schritt = new CaseSchrittView(editor, this, initialerText, referenceStep.newStepIDInSameSequence(insertionPosition), Specman.initialArt());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittZwischenschieben(schritt, insertionPosition, referenceStep, editor);
	}

	public AbstractSchrittView subsequenzSchrittZwischenschieben(RelativeStepPosition insertionPosition,
			AbstractSchrittView referenceStep, EditorI editor) {
		String initialerText = initialtext("<b>Subsequenz " + (schritte.size()+1) + "<b>");
		SubsequenzSchrittView schritt = new SubsequenzSchrittView(editor, this, initialerText, referenceStep.newStepIDInSameSequence(insertionPosition), Specman.initialArt());
		return schrittZwischenschieben(schritt, insertionPosition, referenceStep, editor);
	}

	public AbstractSchrittView breakSchrittZwischenschieben(RelativeStepPosition insertionPosition,
			AbstractSchrittView referenceStep, EditorI editor) {
		String initialerText = initialtext("<b>Exception " + (schritte.size()+1) + "<b>");
		BreakSchrittView schritt = new BreakSchrittView(editor, this, initialerText, referenceStep.newStepIDInSameSequence(insertionPosition), Specman.initialArt());
		return schrittZwischenschieben(schritt, insertionPosition, referenceStep, editor);
	}

	public AbstractSchrittView catchSchrittZwischenschieben(RelativeStepPosition insertionPosition,
			AbstractSchrittView referenceStep, EditorI editor) {
		String initialerText = initialtext("<b>Catch " + (schritte.size()+1) + "<b>");
		CatchSchrittView schritt = new CatchSchrittView(editor, this, initialerText, naechsteSchrittID(), Specman.initialArt(), null);
		return catchAnhaengen(schritt, editor);
	}

	private int stepIndex(AbstractSchrittView schritt) {
		int i = 0;
		for (AbstractSchrittView vorgaenger: schritte) {
			if (vorgaenger == schritt) {
				return i;
			}
			i++;
		}
		throw new IllegalArgumentException("Step " + schritt + " is not part of sequenz " + this);
	}

	public AbstractSchrittView schrittZwischenschieben(AbstractSchrittView newStep, RelativeStepPosition insertionPosition, AbstractSchrittView referenceStep, EditorI editor) {
		newStep.schrittnummerSichtbarkeitSetzen(schrittnummernSichtbar);

		int newStepOffset = (insertionPosition == After ? 1 : 0);
		int newStepIndex = stepIndex(referenceStep) + newStepOffset;

		sequenzbereichLayout.appendRow(RowSpec.decode(ZEILENLAYOUT_GAP));
		sequenzbereichLayout.appendRow(RowSpec.decode(ZEILENLAYOUT_SCHRITT));

		sequenzBereich.add(newStep.getComponent(), constraints4step(newStepIndex));

		for (int followerIndex = newStepIndex; followerIndex < schritte.size(); followerIndex++) {
			AbstractSchrittView nachfolger = schritte.get(followerIndex);
			sequenzbereichLayout.setConstraints(nachfolger.getComponent(), constraints4step(followerIndex+1));
		}

		schritte.add(newStepIndex, newStep);
		updateFollowingStepDecoration(newStepIndex+1);
		updateLayoutRowspecsForAllsStepsAndGaps();
		folgeschritteRenummerieren(newStep);
		return newStep;
	}

	public void folgeschritteRenummerieren(AbstractSchrittView schritt) {
		int i = schritte.indexOf(schritt);
		for (i++; i<schritte.size(); i++) {
			AbstractSchrittView folgeschritt = schritte.get(i);
			folgeschritt.setId(schritt.newStepIDInSameSequence(After));
			schritt = folgeschritt;
		}
	}

	public void renummerieren() { renummerieren(sequenzBasisId); }

	public void renummerieren(SchrittID sequenzBasisId) {
		this.sequenzBasisId = sequenzBasisId;
		if (schritte.size() > 0) {
			AbstractSchrittView ersterSchritt = schritte.get(0);
			ersterSchritt.setId(sequenzBasisId.naechsteID());
			folgeschritteRenummerieren(ersterSchritt);
		}
	}

	public void checkSchrittEntfernen(AbstractSchrittView schritt) throws EditException {
		if (!(schritt instanceof CatchSchrittView)) {
			if (schritte.size() == 1) {
				throw new EditException("Letzten Schritt entfernen is nich!");
			}
		}
	}

	/**
	 * @return Den Index des entfernten Schritts in der Sequenz. Dient der Wiedereingliederung beim Redo
	 */
	public int schrittEntfernen(AbstractSchrittView schritt) throws EditException {
		int schrittIndex;
		if (schritt instanceof CatchSchrittView) {
			schrittIndex = catchBereich.catchEntfernen((CatchSchrittView)schritt);
		}
		else {
			checkSchrittEntfernen(schritt);
			schritt.entfernen(this);
			sequenzBereich.remove(schritt.getComponent());
			schrittIndex = schritte.indexOf(schritt);
			int layoutZeilenLoeschIndex = (schrittIndex == 0) ? 1 : schrittIndex*2;
			sequenzbereichLayout.removeRow(layoutZeilenLoeschIndex);
			sequenzbereichLayout.removeRow(layoutZeilenLoeschIndex);
			schritte.remove(schrittIndex);
			updateFollowingStepDecoration(schrittIndex);
			updateLayoutRowspecsForAllsStepsAndGaps();
			renummerieren(sequenzBasisId);
		}
		// TODO: schritte.get(schrittIndex-1) funktioniert nicht richtig, wenn man einen Catch-Schritt entfernt
		AbstractSchrittView naechsterFokus = schritte.get(schrittIndex == 0 ? schrittIndex : schrittIndex-1);
		Specman.instance().diagrammAktualisieren(naechsterFokus);
		return schrittIndex;
	}

	public void schrittHinzufuegen(AbstractSchrittView schritt, int schrittIndex) {
		if (schritt instanceof CatchSchrittView) {
			System.err.println("Noch nicht implementiert!");
		}
		else {
			if (schrittIndex == schritte.size()) {
				schrittAnhaengen(schritt, Specman.instance());
			}
			else {
				if (schrittIndex == 0) {
					AbstractSchrittView ersterSchritt = schritte.get(schrittIndex);
					schrittZwischenschieben(schritt, Before, ersterSchritt, Specman.instance());
				}
				else {
					AbstractSchrittView vorgaengerSchritt = schritte.get(schrittIndex-1);
					schrittZwischenschieben(schritt, After, vorgaengerSchritt, Specman.instance());
				}
			}
		}
		Specman.instance().diagrammAktualisieren(schritt);
	}

	public AbstractSchrittView findeSchritt(InteractiveStepFragment fragment) {
		for (AbstractSchrittView schritt: schritte) {
			if (schritt.enthaelt(fragment))
				return schritt;
			AbstractSchrittView subStep = schritt.findeSchritt(fragment);
			if (subStep != null) {
				return subStep;
			}
		}
		return catchBereich.findeSchritt(fragment);
	}


	public SchrittSequenzModel_V001 generiereSchittSequenzModel(boolean formatierterText) {
		SchrittSequenzModel_V001 model = new SchrittSequenzModel_V001(
			sequenzBasisId,
			aenderungsart,
			catchBereich.klappen.isSelected(),
			catchBereich.umgehungBreite
		);
		populateModel(model, formatierterText);
		return model;
	}

	protected void populateModel(SchrittSequenzModel_V001 model, boolean formatierterText) {
		for (AbstractSchrittView view : schritte) {
			model.schritte.add(view.generiereModel(formatierterText));
		}
		for (CatchSchrittView view : catchBereich.catchBloecke) {
			model.catchBloecke.add(view.generiereModel(formatierterText));
		}
	}

	protected SchrittSequenzModel_V001 newModel() {
		return new SchrittSequenzModel_V001();
	}
	
	public boolean enthaeltAenderungsmarkierungen() {
		for (AbstractSchrittView schritt: schritte) {
			if (schritt.enthaeltAenderungsmarkierungen())
				return true;
		}
		return false;
	}

	public void setVisible(boolean auf) {
		sequenzBereich.setVisible(auf);
	}

	public BreakSchrittView findeBreakSchritt(String catchText) {
		for (AbstractSchrittView schritt: schritte) {
			if (schritt.istBreakSchrittFuer(catchText))
				return (BreakSchrittView)schritt;
			if (schritt.isStrukturiert()) {
				BreakSchrittView unterschritt = schritt.findeBreakSchritt(catchText);
				if (unterschritt != null)
					return unterschritt;
			}
		}
		return catchBereich.findeBreakSchritt(catchText);
	}

	public void entfernen(AbstractSchrittView container) {
		for (AbstractSchrittView schritt: schritte)
			schritt.entfernen(this);
		catchBereich.entfernen(this);
	}

	public void zusammenklappenFuerReview() {
		for (AbstractSchrittView schritt: schritte) {
			schritt.zusammenklappenFuerReview();
		}
		catchBereich.zusammenklappenFuerReview();
	}

	public void skalieren(int prozentNeu, int prozentAktuell) {
		for (AbstractSchrittView schritt: schritte) {
			schritt.skalieren(prozentNeu, prozentAktuell);
		}
		catchBereich.skalieren(prozentNeu, prozentAktuell);
	}

	public void resyncSchrittnummerStil() {
		for (AbstractSchrittView schritt : schritte) {
			schritt.resyncSchrittnummerStil();
		}
	}

	public void viewsNachinitialisieren() {
		for(AbstractSchrittView schritt: schritte) {
			schritt.viewsNachinitialisieren();
		}
	}

	public AbstractSchrittView findeSchrittZuId(SchrittID id){
		for(AbstractSchrittView schritt: schritte) {
			AbstractSchrittView result = schritt.findeSchrittZuId(id);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public void aenderungenUebernehmen(EditorI editor) throws EditException {
		for (AbstractSchrittView schritt: schritte) {
			schritt.aenderungenUebernehmen(editor);
		}
		setAenderungsart(null);
	}

	public void aenderungenVerwerfen(EditorI editor) throws EditException {
		for (AbstractSchrittView schritt: schritte) {
			schritt.aenderungenVerwerfen(editor);
		}
		setAenderungsart(null);
	}

	public void alsGeloeschtMarkieren(EditorI editor) {
		for (AbstractSchrittView schritt: schritte) {
			schritt.alsGeloeschtMarkieren(editor);
		}
		setAenderungsart(Aenderungsart.Geloescht);
	}

	public void toggleBorderType(AbstractSchrittView schritt) {
		int stepIndex = stepIndex(schritt);
		int componentIndex = stepComponentIndex(schritt);
		sequenzBereich.remove(componentIndex);
		JComponent switchedStepComponent = schritt.toggleBorderType();
		CellConstraints constraints = constraints4step(stepIndex);
		sequenzBereich.add(switchedStepComponent, constraints, componentIndex);
		if (schritt.getDecorated() == None) {
			// If the step just lost its decoration by the toggling, its text fields
			// now have to adjust to what indentions the parent step may require
			schritt.initInheritedTextFieldIndentions();
		}
		updateFollowingStepDecoration(stepIndex+1);
		updateLayoutRowspecsForAllsStepsAndGaps();
	}

	/** Find the index of a step's grafical root component within this sequence' panel */
	int stepComponentIndex(AbstractSchrittView step) {
		JComponent stepComponent = step.getComponent();
		Component[] sequenceChildren = sequenzBereich.getComponents();
		for (int componentIndex = 0; componentIndex < sequenceChildren.length; componentIndex++) {
			if (sequenceChildren[componentIndex] == stepComponent) {
				return componentIndex;
			}
		}
		throw new IllegalArgumentException("Step " + step + " is not part of " + this);
	}

	private void updateFollowingStepDecoration(int followerIndex) {
		if (schritte.size() > followerIndex) {
			RoundedBorderDecorationStyle preceedingStyle = None;
			if (followerIndex > 0) {
				AbstractSchrittView step = schritte.get(followerIndex-1);
				preceedingStyle = step.getDecorated();
			}
			AbstractSchrittView followingStep = schritte.get(followerIndex);
			followingStep.decorateAsFollower(preceedingStyle);
		}
	}

	public void updateTextfieldDecorationIndentions(Indentions lastStepIndention) {
		Indentions stepIndentions = lastStepIndention.withBottom(false);
		int s;
		for (s = 0; s < schritte.size() - 1; s++) {
			forwardTextfieldDecorationIndentions(s, stepIndentions);
		}
		if (s < schritte.size()) {
			forwardTextfieldDecorationIndentions(s, lastStepIndention);
		}
  }

  private void forwardTextfieldDecorationIndentions(int substepIndex, Indentions indentions) {
		AbstractSchrittView substep = schritte.get(substepIndex);
		if (substep.getDecorated() == None) {
			substep.updateTextfieldDecorationIndentions(indentions);
		}
	}

	public AbstractSchrittView findFirstDecoratedParent() {
		SchrittSequenzView sequenz = this;
		while(sequenz.parent != null) {
			if (sequenz.parent.getDecorated() != None) {
				return sequenz.parent;
			}
			sequenz = sequenz.parent.parent;
		}
		return null;
	}

	/** Returns true if the passed step would require a top inset if it is decorated
	 * by a rounded border. This is the case if the step is either the first one in its
	 * sequence or if the preceeding step isn't decorated too. If two decorated steps
	 * directly follow each other we don't want a double-sized space between them. */
	public boolean decorationRequiresTopInset(AbstractSchrittView step) {
		for (int i = 0; i < schritte.size(); i++) {
			if (schritte.get(i) == step) {
				return i == 0 || schritte.get(i-1).getDecorated() == None;
			}
		}
		return false;
	}

	public AbstractSchrittView getParent() {
		return parent;
	}

	public RoundedBorderDecorationStyle deriveDecorationStyleFromPosition(AbstractSchrittView childStep) {
		for (int i = 0; i < schritte.size(); i++) {
			if (schritte.get(i) == childStep) {
				return (i == 0 || schritte.get(i-1).getDecorated() == None)
						? Full : Co;
			}
		}
		return Co;
	}

}
