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
	
	public WhileWhileSchrittView(EditorI editor, String initialwrText, SchrittSequenzView wiederholSequenz, SchrittID id) {
		super(editor, initialwrText, wiederholSequenz, id, true);
	}

	public WhileWhileSchrittView(EditorI editor, String initialwrText, SchrittID id) {
		this(editor, initialwrText, einschrittigeInitialsequenz(editor, id.naechsteEbene()), id);
	}

	public WhileWhileSchrittView(EditorI editor, WhileWhileSchrittModel_V001 model) {
		super(editor, model, true);
	}

	public WhileWhileSchrittView(EditorI editor) {
		this(editor, null, (SchrittID) null);
	}
	
	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		WhileWhileSchrittModel_V001 model = new WhileWhileSchrittModel_V001(
			id,
			getTextMitAenderungsmarkierungen(formatierterText),
			getBackground().getRGB(),
			klappen.isSelected(),
			wiederholSequenz.generiereSchittSequenzModel(formatierterText),
			linkerBalken.getWidth());
		return model;
	}

}
