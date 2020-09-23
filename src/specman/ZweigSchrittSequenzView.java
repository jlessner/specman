package specman;

import specman.model.ZweigSchrittSequenzModel;

import javax.swing.text.JTextComponent;

/**
 * SchrittSequenzView, die einen Zweig in einer Verweigung durch If, If/Else oder Case
 * repräsentiert. Über die normale Schritt-Sequenz hinaus hat ein Zweig einen
 * Überschriftentext, den im Kopfteil des VerzweigungsSchritts angezeigt wird.
 */
public class ZweigSchrittSequenzView extends SchrittSequenzView {

	TextfeldShef ueberschrift;
	
	public ZweigSchrittSequenzView(EditorI editor, ZweigSchrittSequenzModel model) {
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

	public ZweigSchrittSequenzModel generiereZweigSchrittSequenzModel(boolean formatierterText) {
		ZweigSchrittSequenzModel model = (ZweigSchrittSequenzModel)super.generiereSchittSequenzModel(formatierterText);
		model.ueberschrift = ueberschrift.getTextMitAenderungsmarkierungen(formatierterText);
		return model;
	}

	@Override
	protected ZweigSchrittSequenzModel newModel() {
		return new ZweigSchrittSequenzModel();
	}

	public boolean hatUeberschrift(JTextComponent textComponent) {
		return ueberschrift == textComponent;
	}
	
	public SchrittID naechsteNachbarSequenzID() {
		return sequenzBasisId.naechsteID();
	}
	
}
