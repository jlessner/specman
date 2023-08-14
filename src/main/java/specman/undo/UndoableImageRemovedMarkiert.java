package specman.undo;

import specman.EditException;
import specman.textfield.ImageEditArea;

import javax.swing.undo.CannotRedoException;

public class UndoableImageRemovedMarkiert extends AbstractUndoableInteraction {

    private final ImageEditArea imageEditArea;

    public UndoableImageRemovedMarkiert(ImageEditArea imageEditArea) {
        this.imageEditArea = imageEditArea;
    }

    @Override
    public void undoEdit() throws EditException {
        imageEditArea.unmarkAsDeleted();
    }

    @Override
    public void redoEdit() throws CannotRedoException {
        imageEditArea.markAsDeleted();
    }

}
