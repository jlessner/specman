package specman.view;

import specman.Aenderungsart;
import specman.EditException;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.ZweigSchrittSequenzModel_V001;
import specman.pdf.Shape;
import specman.pdf.ShapeSequence;
import specman.textfield.Indentions;
import specman.textfield.InteractiveStepFragment;
import specman.textfield.EditContainer;

import java.awt.Color;

/**
 * SchrittSequenzView, die einen Zweig in einer Verweigung durch If, If/Else oder Case
 * repräsentiert. Über die normale Schritt-Sequenz hinaus hat ein Zweig einen
 * Überschriftentext, den im Kopfteil des VerzweigungsSchritts angezeigt wird.
 */
public class ZweigSchrittSequenzView extends SchrittSequenzView {
	EditContainer ueberschrift;

	public ZweigSchrittSequenzView(EditorI editor, AbstractSchrittView parent, ZweigSchrittSequenzModel_V001 model) {
		super(editor, parent, model);
		ueberschriftInitialisieren(editor, model.ueberschrift != null ? model.ueberschrift : null);
	}

	public ZweigSchrittSequenzView(EditorI editor, AbstractSchrittView parent, SchrittID sequenzBasisId, Aenderungsart aenderungsart, EditorContentModel_V001 initialerText) {
		super(parent, sequenzBasisId, aenderungsart);
		ueberschriftInitialisieren(editor, initialerText);
		this.aenderungsart = Specman.instance().initialArt();
	}

	private void ueberschriftInitialisieren(EditorI editor, EditorContentModel_V001 initialerText) {
		ueberschrift = new EditContainer(editor, initialerText, null);
		ueberschrift.addEditAreasFocusListener(editor);
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
				ueberschrift.editorContent2Model(formatierterText));
		populateModel(model, formatierterText);
		return model;
	}

	public boolean hatUeberschrift(InteractiveStepFragment fragment) {
		return ueberschrift.enthaelt(fragment);
	}
	
	public SchrittID naechsteNachbarSequenzID() {
		return sequenzBasisId.naechsteID();
	}

	@Override
	public void updateTextfieldDecorationIndentions(Indentions indentions) {
		super.updateTextfieldDecorationIndentions(indentions);
		ueberschrift.updateDecorationIndentions(indentions);
	}

	public EditContainer getUeberschrift() {
		return ueberschrift;
	}
	
	//Die Backgroundcolor in IfSchrittViews anpassen
	public void setBackground(Color bg) {
		sequenzBereich.setBackground(bg);
		panel.repaint(); // Damit die Linien nachgezeichnet werden
	}

	public void alsGeloeschtMarkieren(EditorI editor) {
		super.alsGeloeschtMarkieren(editor);
		ueberschrift.setGeloeschtMarkiertStil(null);
	}

	public void aenderungenVerwerfen(EditorI editor) throws EditException {
		super.aenderungenVerwerfen(editor);
		aenderungsmarkierungenEntfernen();
	}

	public void aenderungsmarkierungenEntfernen() {
		setAenderungsart(null);
		ueberschrift.aenderungsmarkierungenEntfernen(null);
	}

	public void ueberschriftAenderungenUebernehmen() { ueberschrift.aenderungsmarkierungenUebernehmen(); }

	public void ueberschriftAenderungenVerwerfen() {
		ueberschrift.aenderungsmarkierungenVerwerfen();
	}

}
