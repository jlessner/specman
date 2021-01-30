package specman.view;

import specman.EditorI;
import specman.SchrittID;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.WhileWhileSchrittModel_V001;

/**
 * Spezielle Anzeige einer While-Schleife mit einem abschließenden unteren Balken.
 * Im Nassi-Shneiderman-Standard steht in dieser Form unten noch einmal die Prüfbedingung
 * drin, aber das lassen wir mal weg. Das sieht komisch aus.
 * 
 * @author less02
 */
public class WhileWhileSchrittView extends SchleifenSchrittView {
	
	public WhileWhileSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id, boolean withDefaultContent) {
		super(editor, parent, initialerText, id, true);
		if (withDefaultContent) {
			initWiederholsequenz(einschrittigeInitialsequenz(editor, id));
		}
	}

	public WhileWhileSchrittView(EditorI editor, SchrittSequenzView parent, String initialwrText, SchrittID id) {
		this(editor, parent, initialwrText, id, true);
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
			getDecorated(),
			klappen.isSelected(),
			wiederholSequenz.generiereSchittSequenzModel(formatierterText),
			linkerBalken.getWidth());
		return model;
	}

}
