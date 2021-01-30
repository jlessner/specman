package specman.view;

import specman.EditorI;
import specman.SchrittID;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.WhileSchrittModel_V001;

public class WhileSchrittView extends SchleifenSchrittView {
	
	protected WhileSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id, boolean withDefaultContent) {
		super(editor, parent, initialerText, id, false);
		if (withDefaultContent) {
			initWiederholsequenz(einschrittigeInitialsequenz(editor, id.naechsteEbene()));
		}
	}

	public WhileSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id) {
		this(editor, parent, initialerText, id, true);
	}

	public WhileSchrittView(EditorI editor, SchrittSequenzView parent, WhileSchrittModel_V001 model) {
		super(editor, parent, model, false);
		initWiederholsequenzFromModel(editor, model);
	}

	public WhileSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText) {
		this(editor, parent, initialerText, (SchrittID) null);
	}
	
	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		WhileSchrittModel_V001 model = new WhileSchrittModel_V001(
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
