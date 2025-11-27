package specman.view;

import specman.Aenderungsart;
import specman.EditorI;
import specman.StepID;
import specman.model.v001.AbstractSchrittModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.WhileSchrittModel_V001;

public class WhileSchrittView extends SchleifenSchrittView {
	
	protected WhileSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialerText, StepID id, Aenderungsart aenderungsart, boolean withDefaultContent) {
		super(editor, parent, initialerText, id, aenderungsart, false);
		if (withDefaultContent) {
			initWiederholsequenz(einschrittigeInitialsequenz(editor, id.naechsteEbene()));
		}
	}

	public WhileSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialerText, StepID id, Aenderungsart aenderungsart) {
		this(editor, parent, initialerText, id, aenderungsart, true);
	}

	public WhileSchrittView(EditorI editor, SchrittSequenzView parent, WhileSchrittModel_V001 model) {
		super(editor, parent, model, false);
	}

	public WhileSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialerText) {
		this(editor, parent, initialerText, (StepID) null, null);
	}
	
	@Override
	public AbstractSchrittModel_V001 generiereModel(boolean formatierterText) {
		WhileSchrittModel_V001 model = new WhileSchrittModel_V001(
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
