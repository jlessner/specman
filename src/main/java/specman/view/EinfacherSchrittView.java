package specman.view;

import specman.Aenderungsart;
import specman.EditorI;
import specman.StepID;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.EinfacherSchrittModel_V001;

import javax.swing.*;

public class EinfacherSchrittView extends AbstractSchrittView {

	public EinfacherSchrittView(EditorI editor, SchrittSequenzView parent, EditorContentModel_V001 initialerText, StepID id, Aenderungsart aenderungsart) {
		super(editor, parent, initialerText, id, aenderungsart);
	}

	public EinfacherSchrittView(EditorI editor, SchrittSequenzView parent, EinfacherSchrittModel_V001 model) {
		super(editor, parent, model.inhalt, model.id, model.aenderungsart);
	}

	@Override
	public JComponent getDecoratedComponent() { return decorated(editContainer); }

	@Override
	public EinfacherSchrittModel_V001 generiereModel(boolean formatierterText) {
		EinfacherSchrittModel_V001 model = new EinfacherSchrittModel_V001(
			id,
			getEditorContent(formatierterText),
			getBackground().getRGB(),
			aenderungsart,
			getQuellschrittID(),
			getDecorated()
		);
		return model;
	}

	@Override
	public JComponent getPanel() { return editContainer; }

	public specman.pdf.Shape getShape() {
		return decoratedShape(editContainer.getShape());
	}

}
