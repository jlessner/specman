package specman.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import specman.EditorI;
import specman.view.AbstractSchrittView;
public class UndoableSchrittAlsEntferntMarkiert extends AbstractUndoableInteraktion{

    private final AbstractSchrittView schritt;
    private final EditorI editor;

    public UndoableSchrittAlsEntferntMarkiert(AbstractSchrittView schritt, EditorI editor) {
        this.schritt = schritt;
        this.editor = editor;
    }

    @Override
    public void undo() throws CannotUndoException {
        schritt.aenderungenVerwerfen(editor);
    }

    @Override
    public void redo() throws CannotRedoException {
        schritt.alsGeloeschtMarkieren(editor);
    }

}
