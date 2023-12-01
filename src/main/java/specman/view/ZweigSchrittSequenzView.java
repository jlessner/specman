package specman.view;

import specman.Aenderungsart;
import specman.EditException;
import specman.EditorI;
import specman.SchrittID;
import specman.Specman;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.ZweigSchrittSequenzModel_V001;
import specman.editarea.EditContainer;
import specman.editarea.Indentions;
import specman.editarea.InteractiveStepFragment;

import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.util.List;

import static specman.Aenderungsart.Untracked;

/**
 * SchrittSequenzView, die einen Zweig in einer Verweigung durch If, If/Else oder Case
 * repräsentiert. Über die normale Schritt-Sequenz hinaus hat ein Zweig einen
 * Überschriftentext, den im Kopfteil des VerzweigungsSchritts angezeigt wird.
 */
public class ZweigSchrittSequenzView extends SchrittSequenzView {
	EditContainer ueberschrift;

	public ZweigSchrittSequenzView(EditorI editor, AbstractSchrittView parent, ZweigSchrittSequenzModel_V001 model) {
		super(editor, parent, model);
		ueberschriftInitialisieren(editor, model.ueberschrift != null ? model.ueberschrift : null, null);
	}

	public ZweigSchrittSequenzView(EditorI editor, AbstractSchrittView parent, SchrittID sequenzBasisId, Aenderungsart aenderungsart, EditorContentModel_V001 initialerText) {
		super(parent, sequenzBasisId, aenderungsart);
		ueberschriftInitialisieren(editor, initialerText, null);
		this.aenderungsart = Specman.initialArt();
	}

	protected void ueberschriftInitialisieren(EditorI editor, EditorContentModel_V001 initialerText, String initialeSchrittnummer) {
		ueberschrift = new EditContainer(editor, initialerText, initialeSchrittnummer);
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

	public void alsGeloeschtMarkierenUDBL(EditorI editor) {
		super.alsGeloeschtMarkierenUDBL(editor);
		ueberschrift.setGeloeschtMarkiertStilUDBL(null);
	}

	public int aenderungenVerwerfen(EditorI editor) throws EditException {
		int changesReverted = super.aenderungenVerwerfen(editor);
		aenderungsmarkierungenEntfernen();
		return changesReverted;
	}

	public void aenderungsmarkierungenEntfernen() {
		ueberschrift.aenderungsmarkierungenEntfernen(null);
		setAenderungsart(Untracked);
	}

	public int ueberschriftAenderungenUebernehmen() {
		return ueberschrift.aenderungenUebernehmen();
	}

	public int ueberschriftAenderungenVerwerfen() {
		return ueberschrift.aenderungenVerwerfen();
	}

	@Override
	public List<JTextComponent> getTextAreas() {
		List<JTextComponent> result = ueberschrift.getTextAreas();
		result.addAll(super.getTextAreas());
		return result;
	}
}