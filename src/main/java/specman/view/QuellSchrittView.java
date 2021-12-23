package specman.view;

import specman.Aenderungsart;
import specman.EditorI;
import specman.SchrittID;
import specman.model.v001.QuellSchrittModel_V001;

import javax.swing.*;
import java.awt.*;

public class QuellSchrittView extends AbstractSchrittView{

    protected AbstractSchrittView zielschritt;

    public QuellSchrittView(EditorI editor, SchrittSequenzView parent, String initialerText, SchrittID id, Aenderungsart aenderungsart) {
        super(editor, parent, initialerText, id, aenderungsart);
    }

    @Override
    public JComponent getComponent() { return decorated(text.asJComponent()); }

    public QuellSchrittView(EditorI editor, SchrittSequenzView parent, QuellSchrittModel_V001 model) {
        super(editor, parent, model.inhalt.text, model.id, model.aenderungsart);
        setBackground(new Color(model.farbe));
    }

    @Override
    public QuellSchrittModel_V001 generiereModel(boolean formatierterText) {
        QuellSchrittModel_V001 model = new QuellSchrittModel_V001(
            id,
            getTextMitAenderungsmarkierungen(formatierterText),
            getBackground().getRGB(),
            aenderungsart,
            getZielschrittID(),
            getDecorated()
        );
        return model;
    }

    @Override
    public JComponent getPanel() {
        return text.asJComponent();
    }

    public SchrittID getZielschrittID(){
        return zielschritt!=null?zielschritt.getId():null;
    }


    public void setZielschritt(AbstractSchrittView zielschritt) {
        if (zielschritt != null) {
            this.zielschritt = zielschritt;
        }
    }
}
