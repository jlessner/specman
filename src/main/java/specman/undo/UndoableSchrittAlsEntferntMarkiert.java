package specman.undo;

import javax.swing.JLabel;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import specman.view.AbstractSchrittView;
public class UndoableSchrittAlsEntferntMarkiert extends AbstractUndoableInteraktion{

    private final AbstractSchrittView schritt;
    private final JLabel textSchrittnummer;

    public UndoableSchrittAlsEntferntMarkiert(AbstractSchrittView schritt) {
        this.schritt = schritt;
        this.textSchrittnummer = schritt.getshef().schrittNummer;
    }

    @Override
    public void undo() throws CannotUndoException {
        undoSammler();
    }

    @Override
    public void redo() throws CannotRedoException {
        redoSammler();
    }

    public void undoSammler() {
        schritt.setNichtGeloeschtMarkiertStil();
    }

    public void redoSammler(){
        schritt.setGeloeschtMarkiertStil();
    }
}
