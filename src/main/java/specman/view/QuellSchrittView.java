package specman.view;

import specman.Aenderungsart;
import specman.EditorI;
import specman.SchrittID;
import specman.model.v001.QuellSchrittModel_V001;
import specman.textfield.TextfieldShef;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static specman.textfield.TextStyles.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE;

public class QuellSchrittView extends AbstractSchrittView{

    protected AbstractSchrittView zielschritt;

    public QuellSchrittView(EditorI editor, SchrittSequenzView parent, SchrittID id) {
        //TODO JL: der "." sorgt für eine Mindesthöhe des Quellschritts. Muss noch gesäubert werden.
        //Die Höhe des Schrittnummer-Labels sollte die Höhe bestimmen.
        super(editor, parent, ".", id, Aenderungsart.Quellschritt);
        setQuellStil();
        setBackground(AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
        alsGeloeschtMarkieren(editor);
    }

    @Override
    public JComponent getComponent() { return decorated(text); }

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
    public JComponent getPanel() { return text; }

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
