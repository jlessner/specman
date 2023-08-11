package specman.view;

import specman.Aenderungsart;
import specman.EditorI;
import specman.SchrittID;
import specman.model.v001.EditorContent_V001;
import specman.model.v001.EinfacherSchrittModel_V001;

import javax.swing.*;
import java.awt.*;

public class EinfacherSchrittView extends AbstractSchrittView {

	public EinfacherSchrittView(EditorI editor, SchrittSequenzView parent, EditorContent_V001 initialerText, SchrittID id, Aenderungsart aenderungsart) {
		super(editor, parent, initialerText, id, aenderungsart);
	}

	public EinfacherSchrittView(EditorI editor, SchrittSequenzView parent, EinfacherSchrittModel_V001 model) {
		super(editor, parent, model.inhalt, model.id, model.aenderungsart);
		setBackground(new Color(model.farbe));
	}

	@Override
	public JComponent getComponent() { return decorated(text); }

	@Override
	public EinfacherSchrittModel_V001 generiereModel(boolean formatierterText) {
		EinfacherSchrittModel_V001 model = new EinfacherSchrittModel_V001(
			id,
			getTextMitAenderungsmarkierungen(formatierterText),
			getBackground().getRGB(),
			aenderungsart,
			getQuellschrittID(),
			getDecorated()
		);
		return model;
	}

	@Override
	public JComponent getPanel() { return text; }

}
