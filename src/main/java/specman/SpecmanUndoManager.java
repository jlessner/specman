package specman;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/** Derivation of a Swing undo manager displaying an asterisk in the
 * current Specman title bar as an indicator for unsaved changes */
public class SpecmanUndoManager extends UndoManager {
    private static final String UNSAVED_CHANGES_INDICATOR = " *";

    private final Specman specman;

    public SpecmanUndoManager(Specman specman) {
        this.specman = specman;
    }

    @Override
    public synchronized boolean addEdit(UndoableEdit anEdit) {
        boolean success = super.addEdit(anEdit);
        if (success) {
            updateUnsavedChangesIndicatorInTitleBar();
        }
        return success;
    }

    @Override
    public synchronized void discardAllEdits() {
        super.discardAllEdits();
        updateUnsavedChangesIndicatorInTitleBar();
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        updateUnsavedChangesIndicatorInTitleBar();
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        updateUnsavedChangesIndicatorInTitleBar();
    }

    private void updateUnsavedChangesIndicatorInTitleBar() {
        String currentTitle = specman.getTitle();
        if (canUndo()) {
            if (!currentTitle.endsWith(UNSAVED_CHANGES_INDICATOR)) {
                specman.setTitle(currentTitle + UNSAVED_CHANGES_INDICATOR);
            }
        }
        else {
            if (currentTitle.endsWith(UNSAVED_CHANGES_INDICATOR)) {
                specman.setTitle(currentTitle.substring(0, currentTitle.length() - UNSAVED_CHANGES_INDICATOR.length()));
            }
        }
    }
}
