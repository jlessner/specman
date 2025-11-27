package specman.view;

import specman.Aenderungsart;
import specman.EditorI;
import specman.StepID;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.WhileWhileSchrittModel_V001;

/**
 * Spezielle Anzeige einer While-Schleife mit einem abschließenden unteren Balken.
 * Im Nassi-Shneiderman-Standard steht in dieser Form unten noch einmal die Prüfbedingung
 * drin, aber das lassen wir mal weg. Das sieht komisch aus.
 *
 * @author less02
 */
public class WhileWhileSchrittView extends SchleifenSchrittView {

	public WhileWhileSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialerText, StepID id, Aenderungsart aenderungsart, boolean withDefaultContent) {
		super(editor, parent, initialerText, id, aenderungsart, true);
		if (withDefaultContent) {
			initWiederholsequenz(einschrittigeInitialsequenz(editor, id.naechsteEbene()));
		}
	}

	public WhileWhileSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialwrText, StepID id, Aenderungsart aenderungsart) {
		this(editor, parent, initialwrText, id, aenderungsart, true);
	}

	public WhileWhileSchrittView(EditorI editor, SchrittSequenzView parent, WhileWhileSchrittModel_V001 model) {
		super(editor, parent, model, true);
	}

	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		WhileWhileSchrittModel_V001 model = new WhileWhileSchrittModel_V001(
			id,
			getEditorContent(formatierterText),
			getBackground().getRGB(),
			aenderungsart,
			klappen.isSelected(),
			wiederholSequenz.generiereSchrittSequenzModel(formatierterText),
			linkerBalken.getWidth(),
			getQuellschrittID(),
			getDecorated());
		return model;
	}

}