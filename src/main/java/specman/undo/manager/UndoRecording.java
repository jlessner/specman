package specman.undo.manager;

public class UndoRecording implements AutoCloseable {
  private final SpecmanUndoManager undoManager;

  public UndoRecording(SpecmanUndoManager undoManager, UndoRecordingMode mode) {
    this.undoManager = undoManager;
    undoManager.setRecordingMode(mode);
  }

  @Override
  public void close() {
    undoManager.setRecordingMode(UndoRecordingMode.Normal);
  }
}
