package specman.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import specman.EditException;
import specman.EditorI;
import specman.view.AbstractSchrittView;
public class UndoableSchrittEntferntMarkiert extends AbstractUndoableInteraktion{

    private final AbstractSchrittView schritt;
    private final EditorI editor;

    public UndoableSchrittEntferntMarkiert(AbstractSchrittView schritt, EditorI editor) {
        this.schritt = schritt;
        this.editor = editor;
    }

    @Override
    public void undoEdit() throws EditException {
        try {
            editor.pauseUndoRecording();
            schritt.aenderungenVerwerfen(editor);
        }
        finally {
            editor.resumeUndoRecording();
        }
    }

    @Override
    public void redoEdit() throws CannotRedoException {
        editor.pauseUndoRecording();
        schritt.alsGeloeschtMarkieren(editor);
        editor.resumeUndoRecording();
    }

}
