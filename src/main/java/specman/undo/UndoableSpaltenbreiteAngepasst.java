package specman.undo;

import specman.SpaltenContainerI;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoableSpaltenbreiteAngepasst extends AbstractUndoableInteraktion {
	private final SpaltenContainerI container;
	private final int veraenderung;
	private final int spalte;
	
	public UndoableSpaltenbreiteAngepasst(SpaltenContainerI container, int veraenderung, int spalte) {
		this.container = container;
		this.spalte = spalte;
		this.veraenderung = veraenderung;
	}

	@Override
	public void undoEdit() throws CannotUndoException {
		container.spaltenbreitenAnpassenNachMausDragging(-veraenderung, spalte);
	}

	@Override
	public void redoEdit() throws CannotRedoException {
		container.spaltenbreitenAnpassenNachMausDragging(veraenderung, spalte);
	}
	
}
