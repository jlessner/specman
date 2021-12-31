package specman.view;

import specman.Aenderungsart;
import specman.EditorI;
import specman.SchrittID;
import specman.model.v001.QuellSchrittModel_V001;
import specman.textfield.TextfieldShef;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class QuellSchrittView extends AbstractSchrittView{

    protected AbstractSchrittView zielschritt;

    public QuellSchrittView(EditorI editor, SchrittSequenzView parent, SchrittID id) {
        //TODO JL: der "." sorgt für eine Mindesthöhe des Quellschritts. Muss noch gesäubert werden.
        //Die Höhe des Schrittnummer-Labels sollte die Höhe bestimmen.
        super(editor, parent, ".", id, Aenderungsart.Quellschritt);
        setQuellStil();
        setBackground(TextfieldShef.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
        alsGeloeschtMarkieren(editor);
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

    public void setQuellStil() {
        getshef().setQuellStil(getZielschrittID());
        setAenderungsart(Aenderungsart.Quellschritt);
        getText().setEditable(false);
    }

    public void setZielschritt(AbstractSchrittView zielschritt) {
        if (zielschritt != null) {
            this.zielschritt = zielschritt;
        }
    }
}
