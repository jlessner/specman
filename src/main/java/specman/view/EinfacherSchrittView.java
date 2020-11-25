package specman.view;

import specman.EditorI;
import specman.SchrittID;
import specman.model.v001.EinfacherSchrittModel_V001;

import javax.swing.*;
import java.awt.*;

public class EinfacherSchrittView extends AbstractSchrittView {

	public EinfacherSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id) {
		super(editor, parent, initialerText, id);
	}

	public EinfacherSchrittView(EditorI editor, SchrittSequenzView parent, EinfacherSchrittModel_V001 model) {
		super(editor, parent, model.inhalt.text, model.id);
		setBackground(new Color(model.farbe));
	}

	@Override
	public JComponent getComponent() { return decorated(text.asJComponent()); }

	@Override
	public EinfacherSchrittModel_V001 generiereModel(boolean formatierterText) {
		EinfacherSchrittModel_V001 model = new EinfacherSchrittModel_V001(
			id,
			getTextMitAenderungsmarkierungen(formatierterText),
			getBackground().getRGB()
		);
		return model;
	}

}
