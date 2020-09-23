package specman;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import specman.model.SchrittModel;
import specman.model.SchrittSequenzModel;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static specman.Specman.initialtext;

public class SchrittSequenzView {
	public static final String ZEILENLAYOUT_GAP = SchrittView.FORMLAYOUT_GAP;
	public static final String ZEILENLAYOUT_SCHRITT = "fill:pref";
	public static final String ZEILENLAYOUT_LETZTER_SCHRITT = "fill:pref:grow";
	public static final String ZEILENLAYOUT_CATCHBEREICH = "pref";

	SchrittID sequenzBasisId;
	final JPanel sequenzBereich;
	final CatchBereich catchBereich;
	final JPanel panel;
	public final List<SchrittView> schritte = new ArrayList<SchrittView>();
	final FormLayout sequenzbereichLayout;
	boolean schrittnummernSichtbar = true;
	final FormLayout huellLayout;
	
	public SchrittSequenzView() {
		this(new SchrittID(0));
	}
	
	public SchrittSequenzView(SchrittID sequenzBasisId) {
		panel = new JPanel();
		huellLayout = new FormLayout("10px:grow", ZEILENLAYOUT_LETZTER_SCHRITT + ", " + ZEILENLAYOUT_CATCHBEREICH);
		panel.setLayout(huellLayout);
		panel.setBackground(Specman.schrittHintergrund());
		this.sequenzBasisId = sequenzBasisId;
		sequenzBereich = new JPanel();
		sequenzBereich.setBackground(Color.black);
		sequenzbereichLayout = new FormLayout("10px:grow");
		sequenzBereich.setLayout(sequenzbereichLayout);
		panel.add(sequenzBereich, CC.xy(1, 1));
		catchBereich = new CatchBereich();
		panel.add(catchBereich, CC.xy(1, 2));
	}
	
	public SchrittSequenzView(EditorI editor, SchrittSequenzModel model) {
		this(model.id);
		for (SchrittModel schritt : model.schritte) {
			SchrittView schrittView = SchrittView.baueSchrittView(editor, schritt);
			schrittAnhaengen(schrittView, editor);
		}
		for (SchrittModel catchSchritt : model.catchBloecke) {
			CatchSchrittView schrittView = (CatchSchrittView)SchrittView.baueSchrittView(editor, catchSchritt);
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
		for (SchrittView schritt: schritte) {
			schritt.schrittnummerSichtbarkeitSetzen(sichtbar);
		}
	}

	public SchrittView einfachenSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("Neuer Schritt " + (schritte.size() + 1));
		EinfacherSchrittView schritt = new EinfacherSchrittView(editor, initialerText, naechsteSchrittID());
		return schrittAnhaengen(schritt, editor);
	}

	public SchrittView whileSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("Neue Schleife " + (schritte.size() + 1));
		WhileSchrittView schritt = new WhileSchrittView(editor, initialerText, naechsteSchrittID());
		return schrittAnhaengen(schritt, editor);
	}

	public SchrittView whileWhileSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("Neue Schleife " + (schritte.size() + 1));
		WhileWhileSchrittView schritt = new WhileWhileSchrittView(editor, initialerText, naechsteSchrittID());
		return schrittAnhaengen(schritt, editor);
	}

	public SchrittView ifElseSchrittAnhaengen(EditorI editor) {
		String initialerText = TextfeldShef.center("If-Else-" + (schritte.size()+1));
		IfElseSchrittView schritt = new IfElseSchrittView(editor, initialerText, naechsteSchrittID());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittAnhaengen(schritt, editor);
	}

	public SchrittView ifSchrittAnhaengen(EditorI editor) {
		String initialerText = TextfeldShef.center("If-Else-" + (schritte.size()+1));
		IfSchrittView schritt = new IfSchrittView(editor, initialerText, naechsteSchrittID());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittAnhaengen(schritt, editor);
	}

	public SchrittView caseSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("Case-" + (schritte.size()+1));
		CaseSchrittView schritt = new CaseSchrittView(editor, initialerText, naechsteSchrittID());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittAnhaengen(schritt, editor);
	}

	public SchrittView subsequenzSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("<b>Subsequenz " + (schritte.size()+1) + "<b>");
		SubsequenzSchrittView schritt = new SubsequenzSchrittView(editor, initialerText, naechsteSchrittID());
		return schrittAnhaengen(schritt, editor);
	}

	public SchrittView breakSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("<b>Exception " + (schritte.size()+1) + "<b>");
		BreakSchrittView schritt = new BreakSchrittView(editor, initialerText, naechsteSchrittID());
		return schrittAnhaengen(schritt, editor);
	}

	public SchrittView catchSchrittAnhaengen(EditorI editor) {
		String initialerText = initialtext("<b>Catch " + (schritte.size()+1) + "<b>");
		CatchSchrittView schritt = new CatchSchrittView(editor, initialerText, naechsteSchrittID(), null);
		return catchAnhaengen(schritt, editor);
	}

	private void letzterSchrittWirdHoehenverbraucher() {
		for (int i = 0; i < schritte.size() - 1; i++) {
			sequenzbereichLayout.setRowSpec(i*2 + 1, RowSpec.decode(ZEILENLAYOUT_SCHRITT));
		}
		sequenzbereichLayout.setRowSpec((schritte.size()-1)*2 + 1, RowSpec.decode(ZEILENLAYOUT_LETZTER_SCHRITT));
	}
	
	public SchrittView schrittAnhaengen(final SchrittView schritt, EditorI editor) {
		schritt.schrittnummerSichtbarkeitSetzen(schrittnummernSichtbar);
		if (schritte.size() != 0) {
			sequenzbereichLayout.appendRow(RowSpec.decode(ZEILENLAYOUT_GAP));
		}
		sequenzbereichLayout.appendRow(RowSpec.decode(ZEILENLAYOUT_SCHRITT));
		sequenzBereich.add(schritt.getComponent(), CC.xy(1, schritte.size() * 2 + 1));
		schritte.add(schritt);
		letzterSchrittWirdHoehenverbraucher();
		return schritt;
	}

	private SchrittView catchAnhaengen(CatchSchrittView schritt, EditorI editor) {
		catchBereich.catchAnhaengen(schritt, editor);
		return schritt;
	}

	public SchrittView einfachenSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		SchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("Neuer Schritt " + (schritte.size() + 1));
		EinfacherSchrittView schritt = new EinfacherSchrittView(editor, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public SchrittView whileSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		SchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("Neue Schleife " + (schritte.size() + 1));
		WhileSchrittView schritt = new WhileSchrittView(editor, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public SchrittView whileWhileSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		SchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("Neue Schleife " + (schritte.size() + 1));
		WhileWhileSchrittView schritt = new WhileWhileSchrittView(editor, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public SchrittView ifElseSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		SchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = TextfeldShef.center("Neue Bedingung " + (schritte.size()+1));
		IfElseSchrittView schritt = new IfElseSchrittView(editor, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public SchrittView ifSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		SchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = TextfeldShef.center("Neue Bedingung " + (schritte.size()+1));
		IfSchrittView schritt = new IfSchrittView(editor, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public SchrittView caseSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		SchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("Case-" + (schritte.size()+1));
		CaseSchrittView schritt = new CaseSchrittView(editor, initialerText, naechsteSchrittID());
		schritt.initialeSchritteAnhaengen(editor);
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public SchrittView subsequenzSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		SchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("<b>Subsequenz " + (schritte.size()+1) + "<b>");
		SubsequenzSchrittView schritt = new SubsequenzSchrittView(editor, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public SchrittView breakSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		SchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("<b>Exception " + (schritte.size()+1) + "<b>");
		BreakSchrittView schritt = new BreakSchrittView(editor, initialerText, vorgaengerSchritt.folgeIDInGleicherSequenz());
		return schrittZwischenschieben(schritt, vorgaengerSchritt, editor);
	}

	public SchrittView catchSchrittZwischenschieben(JTextComponent zuletztFokussierterText, EditorI editor) {
		SchrittView vorgaengerSchritt = findeEigenenSchritt(zuletztFokussierterText);
		String initialerText = initialtext("<b>Catch " + (schritte.size()+1) + "<b>");
		CatchSchrittView schritt = new CatchSchrittView(editor, initialerText, naechsteSchrittID(), null);
		return catchAnhaengen(schritt, editor);
	}

	public SchrittView schrittZwischenschieben(SchrittView schritt, SchrittView vorgaengerSchritt, EditorI editor) {
		schritt.schrittnummerSichtbarkeitSetzen(schrittnummernSichtbar);
		
		int i = 1;
		for (SchrittView vorgaenger: schritte) {
			if (vorgaenger == vorgaengerSchritt)
				break;
			i++;
		}

		sequenzbereichLayout.appendRow(RowSpec.decode(ZEILENLAYOUT_GAP));
		sequenzbereichLayout.appendRow(RowSpec.decode(ZEILENLAYOUT_SCHRITT));
		
		sequenzBereich.add(schritt.getComponent(), CC.xy(1, i * 2 + 1));

		for (int n = i; n < schritte.size(); n++) {
			SchrittView nachfolger = schritte.get(n);
			sequenzbereichLayout.setConstraints(nachfolger.getComponent(), CC.xy(1, (n+1) * 2 + 1));
		}

		schritte.add(i, schritt);
		letzterSchrittWirdHoehenverbraucher();
		folgeschritteRenummerieren(schritt);
		return schritt;
	}

	public void folgeschritteRenummerieren(SchrittView schritt) {
		int i = schritte.indexOf(schritt);
		for (i++; i<schritte.size(); i++) {
			SchrittView folgeschritt = schritte.get(i);
			folgeschritt.setId(schritt.folgeIDInGleicherSequenz());
			schritt = folgeschritt;
		}
	}

	public void renummerieren(SchrittID sequenzBasisId) {
		this.sequenzBasisId = sequenzBasisId;
		if (schritte.size() > 0) {
			SchrittView ersterSchritt = schritte.get(0);
			ersterSchritt.setId(sequenzBasisId.naechsteID());
			folgeschritteRenummerieren(ersterSchritt);
		}
	}

	/**
	 * @return Den Index des entfernten Schritts in der Sequenz. Dient der Wiedereingliederung beim Redo
	 */
	public int schrittEntfernen(SchrittView schritt) {
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
			letzterSchrittWirdHoehenverbraucher();
			renummerieren(sequenzBasisId);
		}
		// TODO: schritte.get(schrittIndex-1) funktioniert nicht richtig, wenn man einen Catch-Schritt entfernt
		SchrittView naechsterFokus = schritte.get(schrittIndex == 0 ? schrittIndex : schrittIndex-1);
		Specman.instance().diagrammAktualisieren(naechsterFokus);
		return schrittIndex;
	}

	public void schrittHinzufuegen(SchrittView schritt, int schrittIndex) {
		if (schritt instanceof CatchSchrittView) {
			System.err.println("Noch nicht implementiert!");
		}
		else {
			if (schrittIndex == schritte.size()) {
				schrittAnhaengen(schritt, Specman.instance());
			}
			else {
				SchrittView vorgaengerSchritt = schritte.get(schrittIndex-1);
				schrittZwischenschieben(schritt, vorgaengerSchritt, Specman.instance());
			}
		}
		Specman.instance().diagrammAktualisieren(schritt);
	}

	public SchrittSequenzView findeSequenz(JTextComponent zuletztFokussierterText) {
		for (SchrittView schritt: schritte) {
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

	public SchrittView findeEigenenSchritt(JTextComponent zuletztFokussierterText) {
		for (SchrittView schritt: schritte) {
			if (schritt.getText() == zuletztFokussierterText)
				return schritt;
		}
		return null;
	}
	
	public SchrittView findeSchritt(JTextComponent zuletztFokussierterText) {
		for (SchrittView schritt: schritte) {
			if (schritt.enthaelt(zuletztFokussierterText))
				return schritt;
			if (schritt.isStrukturiert()) {
				SchrittView unterschritt = schritt.findeSchritt(zuletztFokussierterText);
				if (unterschritt != null)
					return unterschritt;
			}
		}
		return catchBereich.findeSchritt(zuletztFokussierterText);
	}

	public SchrittSequenzView findeElternSequenz(SchrittView kindSchritt) {
		for (SchrittView schritt: schritte) {
			if (schritt == kindSchritt)
				return this;
			if (schritt.isStrukturiert()) {
				SchrittSequenzView untersequenz = schritt.findeElternSequenz(kindSchritt);
				if (untersequenz != null)
					return untersequenz;
			}
		}
		return catchBereich.findeElternSequenz(this, kindSchritt);
	}

	public SchrittSequenzModel generiereSchittSequenzModel(boolean formatierterText) {
		SchrittSequenzModel model = newModel();
		model.id = sequenzBasisId;
		model.catchBloeckeZugeklappt = catchBereich.klappen.isSelected();
		model.catchBloeckeUmgehungBreite = catchBereich.umgehungBreite;
		for (SchrittView view : schritte) {
			model.schritte.add(view.generiereModel(formatierterText));
		}
		for (CatchSchrittView view : catchBereich.catchBloecke) {
			model.catchBloecke.add(view.generiereModel(formatierterText));
		}
		return model;
	}

	protected SchrittSequenzModel newModel() {
		return new SchrittSequenzModel();
	}
	
	public boolean enthaeltAenderungsmarkierungen() {
		for (SchrittView schritt: schritte) {
			if (schritt.enthaeltAenderungsmarkierungen())
				return true;
		}
		return false;
	}

	public void setVisible(boolean auf) {
		sequenzBereich.setVisible(auf);
	}

	public BreakSchrittView findeBreakSchritt(String catchText) {
		for (SchrittView schritt: schritte) {
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

	public void entfernen(SchrittView container) {
		for (SchrittView schritt: schritte)
			schritt.entfernen(this);
		catchBereich.entfernen(this);
	}

	public void zusammenklappenFuerReview() {
		for (SchrittView schritt: schritte) {
			schritt.zusammenklappenFuerReview();
		}
		catchBereich.zusammenklappenFuerReview();
	}

	public void skalieren(int prozentNeu, int prozentAktuell) {
		for (SchrittView schritt: schritte) {
			schritt.skalieren(prozentNeu, prozentAktuell);
		}
		catchBereich.skalieren(prozentNeu, prozentAktuell);
	}

}
