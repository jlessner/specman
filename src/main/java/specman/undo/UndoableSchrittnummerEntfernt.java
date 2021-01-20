package specman.undo;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.border.MatteBorder;
import javax.swing.text.MutableAttributeSet;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import specman.Aenderungsart;
import specman.textfield.TextfieldShef;
import specman.view.AbstractSchrittView;
public class UndoableSchrittnummerEntfernt extends AbstractUndoableInteraktion{

    private final AbstractSchrittView schritt;
    private final JLabel TextSchrittnummer;

    public UndoableSchrittnummerEntfernt(AbstractSchrittView schritt, JLabel TextSchrittnummer) {
        this.schritt = schritt;
        this.TextSchrittnummer = TextSchrittnummer;
    }

    @Override
    public void undo() throws CannotUndoException {
        undoSammler();
    }

    @Override
    public void redo() throws CannotRedoException {
        redoSammler();
    }

    public void undoSammler(){
        schritt.getshef().setBackground(TextfieldShef.Hintergrundfarbe_Standard);
        schritt.getshef().setStyle(schritt.getshef().getPlainText(),TextfieldShef.standardStil);
        schritt.getshef().schrittNummer.setBackground(TextfieldShef.Schriftfarbe_Geloescht);
        schritt.getshef().schrittNummer.setForeground(TextfieldShef.Schriftfarbe_Standard);
        schritt.getshef().schrittNummer.setBorder(new MatteBorder(0, 2, 1, 1, TextfieldShef.Schriftfarbe_Geloescht));
        schritt.getshef().schrittNummer.setText("<html><body><span style='text-decoration:none;'>"+TextSchrittnummer.getText()+"</span></body></html>");
        schritt.setAenderungsart(null);
        schritt.getText().setEnabled(true);
    }

    public void redoSammler(){
        schritt.getshef().setBackground(TextfieldShef.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
        schritt.getshef().setStyle(schritt.getshef().getPlainText(), TextfieldShef.ganzerSchrittGeloeschtStil);
        schritt.getshef().schrittNummer.setBackground(TextfieldShef.Schriftfarbe_Standard);
        schritt.getshef().schrittNummer.setForeground(TextfieldShef.Schriftfarbe_Geloescht);
        schritt.getshef().schrittNummer.setBorder(new MatteBorder(0, 2, 1, 1, TextfieldShef.Hintergrundfarbe_Geloescht));
        schritt.getshef().schrittNummer.setText("<html><body><span style='text-decoration: line-through;'>"+TextSchrittnummer.getText()+"</span></body></html>");
        schritt.setAenderungsart(Aenderungsart.Geloescht);
        schritt.getText().setEnabled(false);
    }
}
