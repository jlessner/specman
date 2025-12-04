package specman;

import specman.undo.manager.SpecmanUndoManager;
import specman.undo.manager.UndoRecordingMode;

public class ScrollPause implements AutoCloseable {
  private final PausableViewport viewport;

  public ScrollPause(PausableViewport viewport) {
    this.viewport = viewport;
    this.viewport.pauseScrolling();
  }

  @Override
  public void close() {
    viewport.resumeScrolling();
  }
}
