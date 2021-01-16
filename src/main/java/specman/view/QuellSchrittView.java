package specman.view;

import specman.Aenderungsart;
import specman.EditorI;
import specman.SchrittID;
import specman.model.v001.QuellSchrittModel_V001;

import javax.swing.*;
import java.awt.*;

public class QuellSchrittView extends AbstractSchrittView{

    protected SchrittID referenzId;

    public QuellSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id, Aenderungsart aenderungsart, SchrittID referenzId) {
        super(editor, parent, initialerText, id, aenderungsart);
        this.referenzId=referenzId;
    }

    public QuellSchrittView(EditorI editor, SchrittSequenzView parent, QuellSchrittModel_V001 model) {
        super(editor, parent, model.inhalt.text, model.id, model.aenderungsart);
        setBackground(new Color(model.farbe));
        this.referenzId=referenzId;
    }

    @Override
    public JComponent getComponent() { return decorated(text.asJComponent()); }

    @Override
    public QuellSchrittModel_V001 generiereModel(boolean formatierterText) {
        QuellSchrittModel_V001 model = new QuellSchrittModel_V001(
                id,
                getTextMitAenderungsmarkierungen(formatierterText),
                getBackground().getRGB(),
                aenderungsart,
                referenzId
        );
        return model;
    }

    @Override
    public JComponent getPanel() {
        // TODO Auto-generated method stub
        return text.asJComponent();
    }

    public SchrittID getReferenzId() {
        return referenzId;
    }

    public void setReferenzId(SchrittID id) {
        this.id = id;
        text.setId(id.toString());
    }
}
