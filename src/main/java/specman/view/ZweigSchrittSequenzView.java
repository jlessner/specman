package specman.view;

import specman.Aenderungsart;
import specman.EditorI;
import specman.SchrittID;
import specman.model.v001.ZweigSchrittSequenzModel_V001;
import specman.textfield.Indentions;
import specman.textfield.TextfieldShef;

import javax.swing.text.JTextComponent;

/**
 * SchrittSequenzView, die einen Zweig in einer Verweigung durch If, If/Else oder Case
 * repräsentiert. Über die normale Schritt-Sequenz hinaus hat ein Zweig einen
 * Überschriftentext, den im Kopfteil des VerzweigungsSchritts angezeigt wird.
 */
public class ZweigSchrittSequenzView extends SchrittSequenzView {

	TextfieldShef ueberschrift;
	

	public ZweigSchrittSequenzView(EditorI editor, AbstractSchrittView parent, ZweigSchrittSequenzModel_V001 model) {
		super(editor, parent, model);
		ueberschriftInitialisieren(editor, model.ueberschrift != null ? model.ueberschrift.text : null);
	}

	public ZweigSchrittSequenzView(EditorI editor, AbstractSchrittView parent, SchrittID sequenzBasisId, Aenderungsart aenderungsart, String initialerText) {
		super(parent, sequenzBasisId, aenderungsart);
		ueberschriftInitialisieren(editor, initialerText);
	}

	private void ueberschriftInitialisieren(EditorI editor, String initialerText) {
		ueberschrift = new TextfieldShef(editor, initialerText, null);
		ueberschrift.addFocusListener(editor);
	}

	@Override
	public void skalieren(int prozentNeu, int prozentAktuell) {
		super.skalieren(prozentNeu, prozentAktuell);
		ueberschrift.skalieren(prozentNeu, prozentAktuell);
	}

	public ZweigSchrittSequenzModel_V001 generiereZweigSchrittSequenzModel(boolean formatierterText) {
		ZweigSchrittSequenzModel_V001 model = new ZweigSchrittSequenzModel_V001(
				sequenzBasisId,
				aenderungsart,
				catchBereich.klappen.isSelected(),
				catchBereich.umgehungBreite,
				ueberschrift.getTextMitAenderungsmarkierungen(formatierterText));
		populateModel(model, formatierterText);
		return model;
	}

	public boolean hatUeberschrift(JTextComponent textComponent) {
		return ueberschrift.getTextComponent() == textComponent;
	}
	
	public SchrittID naechsteNachbarSequenzID() {
		return sequenzBasisId.naechsteID();
	}

	@Override
	public void updateTextfieldDecorationIndentions(Indentions indentions) {
		super.updateTextfieldDecorationIndentions(indentions);
		ueberschrift.updateDecorationIndentions(indentions);
	}
}
