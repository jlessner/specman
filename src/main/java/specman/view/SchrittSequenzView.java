package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.SchrittSequenzModel_V001;
import specman.textfield.Indentions;
import specman.textfield.TextfieldShef;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static specman.Specman.initialtext;
import static specman.view.RoundedBorderDecorationStyle.Co;
import static specman.view.RoundedBorderDecorationStyle.None;

public class SchrittSequenzView {
	public static final String ZEILENLAYOUT_GAP = AbstractSchrittView.FORMLAYOUT_GAP;
	public static final String ZEILENLAYOUT_SCHRITT = "fill:pref";
	public static final String ZEILENLAYOUT_LETZTER_SCHRITT = "fill:pref:grow";
	public static final String ZEILENLAYOUT_CATCHBEREICH = "pref";

	SchrittID sequenzBasisId;
	final JPanel sequenzBereich;
	final CatchBereich catchBereich;
	final JPanel panel;
	public final List<AbstractSchrittView> schritte = new ArrayList<AbstractSchrittView>();
	final FormLayout sequenzbereichLayout;
	boolean schrittnummernSichtbar = true;
	final FormLayout huellLayout;
	final AbstractSchrittView parent;
	
	public SchrittSequenzView() {
		this(null, new SchrittID(0));
	}
	
	public SchrittSequenzView(AbstractSchrittView parent, SchrittID sequenzBasisId) {
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
	
	public SchrittSequenzView(EditorI editor, AbstractSchrittView parent, SchrittSequenzModel_V001 model) {
		this(parent, model.id);
		for (AbstractSchrittModel_V001 schritt : model.schritte) {
			AbstractSchrittView schrittView = AbstractSchrittView.baueSchrittView(editor, this, schritt);
			schrittAnhaengen(schrittView, editor);
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
			return schritte.get(schritte.size() - 1).folgeIDInGleicherSequenz();
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
		EinfacherSchrittView schritt = new EinfacherSchrittView(editor, this, initialerText, naechsteSchrittID());
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView whileSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("Neue Schleife " + (schritte.size() + 1));
		WhileSchrittView schritt = new WhileSchrittView(editor, this, initialerText, naechsteSchrittID());
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView whileWhileSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("Neue Schleife " + (schritte.size() + 1));
		WhileWhileSchrittView schritt = new WhileWhileSchrittView(editor, this, initialerText, naechsteSchrittID());
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView ifElseSchrittAnhaengen(EditorI editor) {
		String initialerText = TextfieldShef.center("If-Else-" + (schritte.size()+1));
		IfElseSchrittView schritt = new IfElseSchrittView(editor, this, initialerText, naechsteSchrittID());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView ifSchrittAnhaengen(EditorI editor) {
		String initialerText = TextfieldShef.center("If-Else-" + (schritte.size()+1));
		IfSchrittView schritt = new IfSchrittView(editor, this, initialerText, naechsteSchrittID());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView caseSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("Case-" + (schritte.size()+1));
		CaseSchrittView schritt = new CaseSchrittView(editor, this, initialerText, naechsteSchrittID());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView subsequenzSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("<b>Subsequenz " + (schritte.size()+1) + "<b>");
		SubsequenzSchrittView schritt = new SubsequenzSchrittView(editor, this, initialerText, naechsteSchrittID());
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView breakSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("<b>Exception " + (schritte.size()+1) + "<b>");
		BreakSchrittView schritt = new BreakSchrittView(editor, this, initialerText, naechsteSchrittID());
		return schrittAnhaengen(schritt, editor);
	}

	public AbstractSchrittView catchSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("<b>Catch " + (schritte.size()+1) + "<b>");
		CatchSchrittView schritt = new CatchSchrittView(editor, this, initialerText, naechsteSchrittID(), null);
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

	private CellConstraints constraints4step(int stepIndex, AbstractSchrittView step) {
		return CC.xy(1, stepIndex * 2 + 1);
	}

	public AbstractSchrittView schrittAnhaengen(final AbstractSchrittView schritt, EditorI editor) {
		schritt.schrittnummerSichtbarkeitSetzen(schrittnummernSichtbar);
		if (schritte.size() != 0) {
			sequenzbereichLayout.appendRow(RowSpec.decode(ZEILENLAYOUT_GAP));
		}
		sequenzbereichLayout.appendRow(RowSpec.decode(ZEILENLAYOUT_SCHRITT));
		sequenzBereich.add(schritt.getComponent(), constraints4step(schritte.size(), schritt));
		schritte.add(schritt);
		updateLayoutRowspecsForAllsStepsAndGaps();
		return schritt;
	}

	private AbstractSchrittView catchAnhaengen(CatchSchrittView schritt, EditorI editor) {
		catchBereich.catchAnhaengen(schritt, editor);
		return schritt;
	}

	public AbstractSchrittView einfachenSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		AbstractSchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("Neuer Schritt " + (schritte.size() + 1));
		EinfacherSchrittView schritt = new EinfacherSchrittView(editor, this, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public AbstractSchrittView whileSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		AbstractSchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("Neue Schleife " + (schritte.size() + 1));
		WhileSchrittView schritt = new WhileSchrittView(editor, this, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public AbstractSchrittView whileWhileSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		AbstractSchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("Neue Schleife " + (schritte.size() + 1));
		WhileWhileSchrittView schritt = new WhileWhileSchrittView(editor, this, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public AbstractSchrittView ifElseSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		AbstractSchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = TextfieldShef.center("Neue Bedingung " + (schritte.size()+1));
		IfElseSchrittView schritt = new IfElseSchrittView(editor, this, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public AbstractSchrittView ifSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		AbstractSchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = TextfieldShef.center("Neue Bedingung " + (schritte.size()+1));
		IfSchrittView schritt = new IfSchrittView(editor, this, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public AbstractSchrittView caseSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		AbstractSchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("Case-" + (schritte.size()+1));
		CaseSchrittView schritt = new CaseSchrittView(editor, this, initialerText, naechsteSchrittID());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public AbstractSchrittView subsequenzSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		AbstractSchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("<b>Subsequenz " + (schritte.size()+1) + "<b>");
		SubsequenzSchrittView schritt = new SubsequenzSchrittView(editor, this, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public AbstractSchrittView breakSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		AbstractSchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("<b>Exception " + (schritte.size()+1) + "<b>");
		BreakSchrittView schritt = new BreakSchrittView(editor, this, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public AbstractSchrittView catchSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		AbstractSchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("<b>Catch " + (schritte.size()+1) + "<b>");
		CatchSchrittView schritt = new CatchSchrittView(editor, this, initialerText, naechsteSchrittID(), null);
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

	public AbstractSchrittView schrittZwischenschieben(AbstractSchrittView schritt, AbstractSchrittView vorgaengerSchritt, EditorI editor) {
		schritt.schrittnummerSichtbarkeitSetzen(schrittnummernSichtbar);
		
		int i = stepIndex(vorgaengerSchritt) + 1;

		sequenzbereichLayout.appendRow(RowSpec.decode(ZEILENLAYOUT_GAP));
		sequenzbereichLayout.appendRow(RowSpec.decode(ZEILENLAYOUT_SCHRITT));

		sequenzBereich.add(schritt.getComponent(), constraints4step(i, schritt));

		for (int n = i; n < schritte.size(); n++) {
			AbstractSchrittView nachfolger = schritte.get(n);
			sequenzbereichLayout.setConstraints(nachfolger.getComponent(), constraints4step(n+1, schritt));
		}

		schritte.add(i, schritt);
		updateLayoutRowspecsForAllsStepsAndGaps();
		folgeschritteRenummerieren(schritt);
		return schritt;
	}

	public void folgeschritteRenummerieren(AbstractSchrittView schritt) {
		int i = schritte.indexOf(schritt);
		for (i++; i<schritte.size(); i++) {
			AbstractSchrittView folgeschritt = schritte.get(i);
			folgeschritt.setId(schritt.folgeIDInGleicherSequenz());
			schritt = folgeschritt;
		}
	}

	public void renummerieren(SchrittID sequenzBasisId) {
		this.sequenzBasisId = sequenzBasisId;
		if (schritte.size() > 0) {
			AbstractSchrittView ersterSchritt = schritte.get(0);
			ersterSchritt.setId(sequenzBasisId.naechsteID());
			folgeschritteRenummerieren(ersterSchritt);
		}
	}

	/**
	 * @return Den Index des entfernten Schritts in der Sequenz. Dient der Wiedereingliederung beim Redo
	 */
	public int schrittEntfernen(AbstractSchrittView schritt) {
		int schrittIndex;
		if (schritt instanceof CatchSchrittView) {
			schrittIndex = catchBereich.catchEntfernen((CatchSchrittView)schritt);
		}
		else {
			if (schritte.size() == 1) {
				System.err.println("Letzten Schritt entfernen is nich!");
				return -1;
			}
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
				AbstractSchrittView vorgaengerSchritt = schritte.get(schrittIndex-1);
				schrittZwischenschieben(schritt, vorgaengerSchritt, Specman.instance());
			}
		}
		Specman.instance().diagrammAktualisieren(schritt);
	}

	public SchrittSequenzView findeSequenz(JTextComponent zuletztFokussierterText) {
		for (AbstractSchrittView schritt: schritte) {
			if (schritt.enthaelt(zuletztFokussierterText))
				return this;
			if (schritt.isStrukturiert()) {
				SchrittSequenzView sequenz = schritt.findeSequenz(zuletztFokussierterText);
				if (sequenz != null)
					return sequenz;
			}
		}
		if (catchBereich.findeEigenenSchritt(zuletztFokussierterText) != null)
			return this;
		return catchBereich.findeSequenz(zuletztFokussierterText);
	}

	public AbstractSchrittView findeEigenenSchritt(JTextComponent zuletztFokussierterText) {
		for (AbstractSchrittView schritt: schritte) {
			if (schritt.getText() == zuletztFokussierterText)
				return schritt;
		}
		return null;
	}
	
	public AbstractSchrittView findeSchritt(JTextComponent zuletztFokussierterText) {
		for (AbstractSchrittView schritt: schritte) {
			if (schritt.enthaelt(zuletztFokussierterText))
				return schritt;
			if (schritt.isStrukturiert()) {
				AbstractSchrittView unterschritt = schritt.findeSchritt(zuletztFokussierterText);
				if (unterschritt != null)
					return unterschritt;
			}
		}
		return catchBereich.findeSchritt(zuletztFokussierterText);
	}

	public SchrittSequenzModel_V001 generiereSchittSequenzModel(boolean formatierterText) {
		SchrittSequenzModel_V001 model = new SchrittSequenzModel_V001(
			sequenzBasisId,
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

	public void toggleBorderType(AbstractSchrittView schritt) {
		int stepIndex = stepIndex(schritt);
		JComponent currentStepComponent = schritt.getComponent();
		Component[] sequenceChildren = sequenzBereich.getComponents();
		int componentIndex;
		for (componentIndex = 0; componentIndex < sequenceChildren.length; componentIndex++) {
			if (sequenceChildren[componentIndex] == currentStepComponent) {
				sequenzBereich.remove(componentIndex);
				JComponent switchedStepComponent = schritt.toggleBorderType();
				CellConstraints constraints = constraints4step(stepIndex, schritt);
				sequenzBereich.add(switchedStepComponent, constraints, componentIndex);
				if (schritt.getDecorated() == None) {
					// If the step just lost its decoration, its text fields now have
					// to adjust to what indentions the parent step may require
					schritt.initInheritedTextFieldIndentions();
				}
				updateFollowingStepDecoration(stepIndex+1);
				updateLayoutRowspecsForAllsStepsAndGaps();
				return;
			}
		}
		throw new IllegalArgumentException("Schritt " + schritt + " is not part of " + this);
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
}
