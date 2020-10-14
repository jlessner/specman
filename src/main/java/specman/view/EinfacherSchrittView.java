package specman.view;

import specman.EditorI;
import specman.SchrittID;
import specman.model.SchrittModel;

import java.awt.*;

public class EinfacherSchrittView extends AbstractSchrittView {

	public EinfacherSchrittView(EditorI editor) {
		this(editor, null, (SchrittID) null);
	}

	public EinfacherSchrittView(EditorI editor, String initialerText, SchrittID id) {
		super(editor, initialerText, id);
	}
	
	public EinfacherSchrittView(EditorI editor, SchrittModel model) {
		this(editor, model.inhalt.text, model.id);
		setBackground(new Color(model.farbe));
	}

	@Override
	public Component getComponent() {
		return text;
	}

	@Override
	public SchrittModel generiereModel(boolean formatierterText) {
		SchrittModel model = new SchrittModel();
		model.inhalt = getTextMitAenderungsmarkierungen(formatierterText);
		model.id = id;
		model.farbe = getBackground().getRGB();
		return model;
	}

}
