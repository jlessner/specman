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

//TODO
public class UndoableSchrittnummerEntfernt extends AbstractUndoableInteraktion{
    private final AbstractSchrittView schritt;
    //Hintergrund des Schrittes
    private Color alteHintergrundfarbe = TextfieldShef.Hintergrundfarbe_Standard;
    private Color neueHintergrundfarbe = TextfieldShef.Hintergrundfarbe_Geloescht;
    //Hintergrund der Schrittnummer
    private Color alteSchrittnummerHintergrundfarbe = TextfieldShef.Schriftfarbe_Geloescht;
    private Color neueSchrittnummerHintergrundfarbe = TextfieldShef.Schriftfarbe_Standard;
    //Schriftfarben der Schrittnummern
    private Color alteSchriftfarbeSchrittnummerfarbe = TextfieldShef.Schriftfarbe_Standard;
    private Color neueSchriftfarbeSchrittnummerfarbe = TextfieldShef.Schriftfarbe_Geloescht;
    //Rahmenfarbe der Schrittnummer
    private Color alteRahmenfarbe = TextfieldShef.Schriftfarbe_Geloescht;
    private Color neueRahmenfarbe = TextfieldShef.Hintergrundfarbe_Geloescht;
    //MutableAttributeSet für textliche Veränderungen innerhalb des Schrittes
    private MutableAttributeSet altesAttributeSet = TextfieldShef.standardStil;
    private MutableAttributeSet neuesAttributeSet = TextfieldShef.ganzerSchrittGeloeschtStil;

    private String test;

    //Wird benötigt um das Strike-Through der Schrittnummer auch bei Undo und Do zu behalten
    private final JLabel TextSchrittnummer;
    //Um die Zuweisung des Enums rueckgaengig zu machen
    private Aenderungsart neueAenderungsart = Aenderungsart.Geloescht;

    public UndoableSchrittnummerEntfernt(AbstractSchrittView schritt, JLabel TextSchrittnummer) {
        this.schritt = schritt;
        this.TextSchrittnummer = TextSchrittnummer;
    }

    @Override
    public void undo() throws CannotUndoException {
        schritt.getshef().setBackground(alteHintergrundfarbe);
        schritt.getshef().setStyle(schritt.getshef().getPlainText(),altesAttributeSet);
        schritt.getshef().schrittNummer.setBackground(alteSchrittnummerHintergrundfarbe);
        schritt.getshef().schrittNummer.setForeground(alteSchriftfarbeSchrittnummerfarbe);
        schritt.getshef().schrittNummer.setBorder(new MatteBorder(0, 2, 1, 1, alteRahmenfarbe));
        test = TextSchrittnummer.getText();
        //schritt.getshef().schrittNummer.setText(TextSchrittnummer.getText());
        schritt.getshef().schrittNummer.setText("<html><body><span style='text-decoration:none;'>"+TextSchrittnummer.getText()+"</span></body></html>");
        schritt.setAenderungsart(null);
        schritt.getText().setEditable(true);
    }

    @Override
    public void redo() throws CannotRedoException {
        schritt.getshef().setBackground(neueHintergrundfarbe);
        schritt.getshef().setStyle(schritt.getshef().getPlainText(), neuesAttributeSet);
        schritt.getshef().schrittNummer.setBackground(neueSchrittnummerHintergrundfarbe);
        schritt.getshef().schrittNummer.setForeground(neueSchriftfarbeSchrittnummerfarbe);
        schritt.getshef().schrittNummer.setBorder(new MatteBorder(0, 2, 1, 1, neueRahmenfarbe));
        schritt.getshef().schrittNummer.setText("<html><body><span style='text-decoration: line-through;'>"+TextSchrittnummer.getText()+"</span></body></html>");
        schritt.setAenderungsart(neueAenderungsart);
        schritt.getText().setEditable(false);
    }
}
