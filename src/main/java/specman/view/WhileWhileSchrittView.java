package specman.view;

import specman.Aenderungsart;
import specman.EditorI;
import specman.SchrittID;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.EditorContent_V001;
import specman.model.v001.WhileWhileSchrittModel_V001;

/**
 * Spezielle Anzeige einer While-Schleife mit einem abschließenden unteren Balken.
 * Im Nassi-Shneiderman-Standard steht in dieser Form unten noch einmal die Prüfbedingung
 * drin, aber das lassen wir mal weg. Das sieht komisch aus.
 * 
 * @author less02
 */
public class WhileWhileSchrittView extends SchleifenSchrittView {
	
	public WhileWhileSchrittView(EditorI editor, SchrittSequenzView parent, EditorContent_V001 initialerText, SchrittID id, Aenderungsart aenderungsart, boolean withDefaultContent) {
		super(editor, parent, initialerText, id, aenderungsart, true);
		if (withDefaultContent) {
			initWiederholsequenz(einschrittigeInitialsequenz(editor, id, aenderungsart));
		}
	}

	public WhileWhileSchrittView(EditorI editor, SchrittSequenzView parent, EditorContent_V001 initialwrText, SchrittID id, Aenderungsart aenderungsart) {
		this(editor, parent, initialwrText, id, aenderungsart, true);
	}

	public WhileWhileSchrittView(EditorI editor, SchrittSequenzView parent, WhileWhileSchrittModel_V001 model) {
		super(editor, parent, model, true);
		initWiederholsequenzFromModel(editor, model);
	}

	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		WhileWhileSchrittModel_V001 model = new WhileWhileSchrittModel_V001(
			id,
			getTextMitAenderungsmarkierungen(formatierterText),
			getBackground().getRGB(),
			aenderungsart,
			klappen.isSelected(),
			wiederholSequenz.generiereSchittSequenzModel(formatierterText),
			linkerBalken.getWidth(),
			getQuellschrittID(),
			getDecorated());
		return model;
	}

}
