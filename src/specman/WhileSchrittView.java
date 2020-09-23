package specman;

import specman.model.SchrittModel;
import specman.model.WhileSchrittModel;

class WhileSchrittView extends SchleifenSchrittView {
	
	public WhileSchrittView(EditorI editor, String initialerText, SchrittSequenzView wiederholSequenz, SchrittID id) {
		super(editor, initialerText, wiederholSequenz, id, false);
	}

	public WhileSchrittView(EditorI editor, String initialerText, SchrittID id) {
		this(editor, initialerText, einschrittigeInitialsequenz(editor, id.naechsteEbene()), id);
	}

	public WhileSchrittView(EditorI editor, WhileSchrittModel model) {
		super(editor, model, false);
	}

	public WhileSchrittView(EditorI editor, String initialerText) {
		this(editor, initialerText, (SchrittID) null);
	}
	
	@Override
	public SchrittModel generiereModel(boolean formatierterText) {
		WhileSchrittModel model = new WhileSchrittModel();
		model.inhalt = getTextMitAenderungsmarkierungen(formatierterText);
		model.id = id;
		model.farbe = getBackground().getRGB();
		model.wiederholSequenz = wiederholSequenz.generiereSchittSequenzModel(formatierterText);
		model.zugeklappt = klappen.isSelected();
		model.balkenbreite = linkerBalken.getWidth();
		return model;
	}

}
