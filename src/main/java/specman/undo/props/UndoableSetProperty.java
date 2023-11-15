package specman.undo.props;

import specman.EditException;
import specman.undo.AbstractUndoableInteraction;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class UndoableSetProperty<PROPTYPE> extends AbstractUndoableInteraction {
  private final PROPTYPE undoValue;
  private PROPTYPE redoValue;
  private final Consumer<PROPTYPE> setter;

  public UndoableSetProperty(PROPTYPE undoValue, Consumer<PROPTYPE> setter, Supplier<PROPTYPE> getter) {
    this.undoValue = undoValue;
    this.redoValue = getter.get();
    this.setter = setter;
  }

  @Override
  protected void undoEdit() throws EditException {
    setter.accept(undoValue);
  }

  @Override
  protected void redoEdit() throws EditException {
    setter.accept(redoValue);
  }

}
