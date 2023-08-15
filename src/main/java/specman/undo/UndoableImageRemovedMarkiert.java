package specman.undo;

import specman.Aenderungsart;
import specman.EditException;
import specman.textfield.ImageEditArea;

import javax.swing.undo.CannotRedoException;

public class UndoableImageRemovedMarkiert extends AbstractUndoableInteraction {

    private final ImageEditArea imageEditArea;
    private final Aenderungsart originalChangetype;

    public UndoableImageRemovedMarkiert(ImageEditArea imageEditArea, Aenderungsart originalChangetype) {
        this.imageEditArea = imageEditArea;
        this.originalChangetype = originalChangetype;
    }

    @Override
    public void undoEdit() throws EditException {
        imageEditArea.unmarkAsDeleted(originalChangetype);
    }

    @Override
    public void redoEdit() throws CannotRedoException {
        imageEditArea.markAsDeleted();
    }

}
