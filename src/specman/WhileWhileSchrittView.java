package specman;

import specman.model.SchrittModel;
import specman.model.WhileWhileSchrittModel;

/**
 * Spezielle Anzeige einer While-Schleife mit einem abschließenden unteren Balken.
 * Im Nassi-Shneiderman-Standard steht in dieser Form unten noch einmal die Prüfbedingung
 * drin, aber das lassen wir mal weg. Das sieht komisch aus.
 * 
 * @author less02
 */
class WhileWhileSchrittView extends SchleifenSchrittView {
	
	public WhileWhileSchrittView(EditorI editor, String initialwrText, SchrittSequenzView wiederholSequenz, SchrittID id) {
		super(editor, initialwrText, wiederholSequenz, id, true);
	}

	public WhileWhileSchrittView(EditorI editor, String initialwrText, SchrittID id) {
		this(editor, initialwrText, einschrittigeInitialsequenz(editor, id.naechsteEbene()), id);
	}

	public WhileWhileSchrittView(EditorI editor, WhileWhileSchrittModel model) {
		super(editor, model, true);
	}

	public WhileWhileSchrittView(EditorI editor) {
		this(editor, null, (SchrittID) null);
	}
	
	@Override
	public SchrittModel generiereModel(boolean formatierterText) {
		WhileWhileSchrittModel model = new WhileWhileSchrittModel();
		model.inhalt = getTextMitAenderungsmarkierungen(formatierterText);
		model.id = id;
		model.farbe = getBackground().getRGB();
		model.wiederholSequenz = wiederholSequenz.generiereSchittSequenzModel(formatierterText);
		model.zugeklappt = klappen.isSelected();
		model.balkenbreite = linkerBalken.getWidth();
		return model;
	}

}
