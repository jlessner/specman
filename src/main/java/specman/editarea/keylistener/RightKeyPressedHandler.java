package specman.editarea.keylistener;

import specman.Specman;
import specman.editarea.TextEditArea;

import java.awt.event.KeyEvent;

public class RightKeyPressedHandler extends AbstractRemovalKeyPressedHandler {
  RightKeyPressedHandler(TextEditArea textArea, KeyEvent keyEvent) {
    super(textArea, keyEvent);
  }

  void handle() {
    if (event.isControlDown() && event.isAltDown()) {
      Specman.instance().scrollForwardInEditHistory();
      event.consume();
    }
    else if (skipToStepnumberLinkEnd()) {
      event.consume();
    }

  }

}
