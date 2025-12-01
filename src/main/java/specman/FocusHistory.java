package specman;

import specman.editarea.EditArea;
import specman.editarea.EditContainer;

import java.util.ArrayList;
import java.util.List;

/** The focus history records the user's navigation through the model and allows
 * him to go back and forth along this history by pressing CTRL+ALT+Right/Left
 * as this is known from intelliJ. This is of interest for larger models which
 * may takes wider jumps during editing. */
public class FocusHistory {
  private static final int MAX_HISTORY_SIZE = 10;

  private List<EditContainer> history = new ArrayList<>();

  /** Null means: user didn't explicitely navigate to a certain point in history.
   * Will automatically be reset when a new entry is added. */
  private Integer currentPositionInHistory;

  void clear() {
    history.clear();
    currentPositionInHistory = null;
  }

  void append(EditContainer editContainer) {
    if (editContainer != currentEditContainerInHistory()) {
      discardForwardHistory();
      history.add(editContainer);
      if (history.size() > MAX_HISTORY_SIZE) {
        history.remove(0);
      }
    }
  }

  private EditContainer lastEditAreaInHistory() {
    return history.isEmpty() ? null : history.get(history.size() - 1);
  }

  EditContainer navigateBack() {
    if (historyNavigable()) {
      if (currentPositionInHistory == null) {
        currentPositionInHistory = history.size() - 1;
      }
      if (currentPositionInHistory > 0) {
        currentPositionInHistory--;
      }
    }
    return currentEditContainerInHistory();
  }

  EditContainer navigateForward() {
    if (currentPositionInHistory != null) {
      if (currentPositionInHistory < history.size() - 1) {
        currentPositionInHistory++;
      }
    }
    return currentEditContainerInHistory();
  }

  private boolean historyNavigable() {
    return history.size() > 1;
  }

  /** When we add a new entry while the user as navigated to a particular
   * point in history, we discard all entries following that point and
   * reset the pointer. Imaging there is a history
   * <pre>A B C D</pre>
   * and the user navigated back from D to B. Now we add E, so the forward
   * history beyond B is discarded and the resulting history is
   * <pre>A B E</pre>
   * That's how the feature works in intelliJ. */
  private void discardForwardHistory() {
    if (currentPositionInHistory != null) {
      history = history.subList(0, currentPositionInHistory + 1);
      currentPositionInHistory = null;
    }
  }

  private EditContainer currentEditContainerInHistory() {
    if (currentPositionInHistory != null) {
      return history.get(currentPositionInHistory);
    }
    return lastEditAreaInHistory();
  }
}
