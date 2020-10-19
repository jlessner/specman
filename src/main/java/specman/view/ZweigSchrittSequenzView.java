package specman.view;

import specman.EditorI;
import specman.SchrittID;
import specman.TextfeldShef;
import specman.model.v001.ZweigSchrittSequenzModel_V001;

import javax.swing.text.JTextComponent;

/**
 * SchrittSequenzView, die einen Zweig in einer Verweigung durch If, If/Else oder Case
 * repräsentiert. Über die normale Schritt-Sequenz hinaus hat ein Zweig einen
 * Überschriftentext, den im Kopfteil des VerzweigungsSchritts angezeigt wird.
 */
public class ZweigSchrittSequenzView extends SchrittSequenzView {

	TextfeldShef ueberschrift;
	
	public ZweigSchrittSequenzView(EditorI editor, ZweigSchrittSequenzModel_V001 model) {
		super(editor, model);
		ueberschriftInitialisieren(editor, model.ueberschrift != null ? model.ueberschrift.text : null);
	}

	public ZweigSchrittSequenzView(EditorI editor, SchrittID sequenzBasisId, String initialerText) {
		super(sequenzBasisId);
		ueberschriftInitialisieren(editor, initialerText);
	}

	private void ueberschriftInitialisieren(EditorI editor, String initialerText) {
		ueberschrift = new TextfeldShef(initialerText, null);
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
				catchBereich.klappen.isSelected(),
				catchBereich.umgehungBreite,
				ueberschrift.getTextMitAenderungsmarkierungen(formatierterText));
		populateModel(model, formatierterText);
		return model;
	}

	public boolean hatUeberschrift(JTextComponent textComponent) {
		return ueberschrift == textComponent;
	}
	
	public SchrittID naechsteNachbarSequenzID() {
		return sequenzBasisId.naechsteID();
	}
	
}
