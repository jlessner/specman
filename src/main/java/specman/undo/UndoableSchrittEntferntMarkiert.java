package specman.undo;

import javax.swing.undo.CannotRedoException;

import specman.EditException;
import specman.EditorI;
import specman.Specman;
import specman.undo.manager.UndoRecording;
import specman.view.AbstractSchrittView;
public class UndoableSchrittEntferntMarkiert extends AbstractUndoableInteraction {

    private final AbstractSchrittView schritt;
    private final EditorI editor;

    public UndoableSchrittEntferntMarkiert(AbstractSchrittView schritt, EditorI editor) {
        this.schritt = schritt;
        this.editor = editor;
    }

    @Override
    public void undoEdit() throws EditException {
        try (UndoRecording ur = Specman.instance().pauseUndo()) {
            schritt.aenderungenVerwerfen(editor);
        }
    }

    @Override
    public void redoEdit() throws CannotRedoException {
        try (UndoRecording ur = Specman.instance().pauseUndo()) {
            schritt.alsGeloeschtMarkieren(editor);
        }
    }

}
