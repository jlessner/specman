package specman.editarea.keylistener;

import specman.editarea.TextEditArea;

import java.awt.event.KeyEvent;

public class LeftKeyPressedHandler extends AbstractRemovalKeyPressedHandler {
  LeftKeyPressedHandler(TextEditArea textArea, KeyEvent keyEvent) {
    super(textArea, keyEvent);
  }

  void handle() {
    if (event.isControlDown() && event.isAltDown()) {
      scrollToPreceedingEditAreaInHistory();
      event.consume();
    }
    else if (skipToStepnumberLinkStart()) {
      event.consume();
    }

  }

  private void scrollToPreceedingEditAreaInHistory() {

  }
}
