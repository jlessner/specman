package specman.undo;

import javax.swing.undo.UndoableEdit;

abstract public class AbstractUndoableInteraktion implements UndoableEdit {

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public void die() {}

	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		return false;
	}

	@Override
	public boolean isSignificant() {
		return true;
	}

	@Override
	public String getPresentationName() {
		return null;
	}

	@Override
	public String getUndoPresentationName() {
		return null;
	}

	@Override
	public String getRedoPresentationName() {
		return null;
	}

	
}
