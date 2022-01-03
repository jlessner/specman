package specman.undo;

import specman.Aenderungsart;
import specman.textfield.TextfieldShef;
import specman.view.AbstractSchrittView;

import javax.swing.border.MatteBorder;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoableQuellschritt extends AbstractUndoableInteraktion{

    private final AbstractSchrittView schritt;

    public UndoableQuellschritt(AbstractSchrittView schritt) {
        this.schritt = schritt;
    }

    @Override
    public void undoEdit() throws CannotUndoException {
        schritt.getshef().setBackground(TextfieldShef.Hintergrundfarbe_Standard);
        schritt.getshef().setStyle(schritt.getshef().getPlainText(),TextfieldShef.standardStil);
        schritt.getshef().schrittNummer.setBackground(TextfieldShef.Hintergrundfarbe_Standard);
        schritt.getshef().schrittNummer.setForeground(TextfieldShef.Hintergrundfarbe_Standard);
        schritt.getshef().schrittNummer.setBorder(new MatteBorder(0, 2, 1, 1, TextfieldShef.Hintergrundfarbe_Geloescht));
        schritt.getshef().schrittNummer.setText("<html><body><span style='text-decoration:none;'>"+schritt.getshef().schrittNummer.getText()+"</span></body></html>");
        schritt.setAenderungsart(null);
        schritt.getText().setEditable(false);
    }

    @Override
    public void redoEdit() throws CannotRedoException {
        schritt.getshef().setBackground(TextfieldShef.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
        schritt.getshef().setStyle(schritt.getshef().getPlainText(), TextfieldShef.quellschrittStil);
        schritt.getshef().schrittNummer.setForeground(TextfieldShef.Schriftfarbe_Geloescht);
        schritt.getshef().schrittNummer.setBorder(new MatteBorder(0, 2, 1, 1, TextfieldShef.Hintergrundfarbe_Geloescht));
        schritt.setAenderungsart(Aenderungsart.Quellschritt);
        schritt.getText().setEditable(true);
    }
}
