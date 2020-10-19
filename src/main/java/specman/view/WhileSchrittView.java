package specman.view;

import specman.EditorI;
import specman.SchrittID;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.WhileSchrittModel_V001;

public class WhileSchrittView extends SchleifenSchrittView {
	
	public WhileSchrittView(EditorI editor, String initialerText, SchrittSequenzView wiederholSequenz, SchrittID id) {
		super(editor, initialerText, wiederholSequenz, id, false);
	}

	public WhileSchrittView(EditorI editor, String initialerText, SchrittID id) {
		this(editor, initialerText, einschrittigeInitialsequenz(editor, id.naechsteEbene()), id);
	}

	public WhileSchrittView(EditorI editor, WhileSchrittModel_V001 model) {
		super(editor, model, false);
	}

	public WhileSchrittView(EditorI editor, String initialerText) {
		this(editor, initialerText, (SchrittID) null);
	}
	
	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		WhileSchrittModel_V001 model = new WhileSchrittModel_V001(
			id,
			getTextMitAenderungsmarkierungen(formatierterText),
			getBackground().getRGB(),
			klappen.isSelected(),
			wiederholSequenz.generiereSchittSequenzModel(formatierterText),
			linkerBalken.getWidth());
		return model;
	}

}
