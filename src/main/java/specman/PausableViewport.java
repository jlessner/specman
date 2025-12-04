package specman;

import specman.editarea.TextEditArea;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.util.Date;

/** Removing text from {@link HTMLDocument}s implicetely causes updates of the view
 * position by default to make the addressed editor pane automatically visible.
 * However, Specman performs quite a number of document operations which are
 * not supposed to cause scrolling. Calling {@link #pauseScrolling()} allows to
 * intercept scrolling for a while.
 * <p>
 * E.g. when the current model is stored to file, all {@link TextEditArea}s are cleaned
 * up which causes the replacement of the whole content (see method {@link TextEditArea#cleanupText()}.
 * If this would change the view position, saving whould cause a scroll throw the complete
 * model. */
public class PausableViewport extends JViewport {
  private boolean scrollingEnabled = true;

  public void pauseScrolling() {
    if (scrollingEnabled) {
      scrollingEnabled = false;
    }
  }

  public void resumeScrolling() {
    if (!scrollingEnabled) {
      SwingUtilities.invokeLater(() -> {
        if (!scrollingEnabled) {
          scrollingEnabled = true;
        }
      });
    }
  }

  @Override
  public void setViewPosition(Point p) {
    if (scrollingEnabled) {
      super.setViewPosition(p);
    }
  }

}

