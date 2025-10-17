package specman.editarea.keylistener;

import specman.editarea.TextEditArea;
import specman.editarea.document.WrappedPosition;

import java.awt.event.KeyEvent;

import static specman.editarea.markups.CharType.ParagraphBoundary;

class DeleteKeyPressedHandler extends AbstractKeyEventHandler{
  DeleteKeyPressedHandler(TextEditArea textArea, KeyEvent keyEvent) {
    super(textArea, keyEvent);
  }

  void handle() {
    WrappedPosition caretPos = getWrappedCaretPosition();
    // TODO: similar behaviour as in BackspaceKeyPressedHandler
    if (isTrackingChanges()) {
      handleTextDeletion();
      event.consume();
    }
//    else if (stepnumberLinkStyleSet(getWrappedSelectionEnd().dec())) {
//      removePreviousStepnumberLink();
//      event.consume();
//    }
    else if (ParagraphBoundary.at(caretPos)) {
      // We are about to merge two paragraphs, so must ensure markup recovery
      backupMarkupsAndRecoverAfterDefaultKeyOperation();
    }
  }

  private void handleTextDeletion() {
//    if (getSelectionStart() == getSelectionEnd()) {
//      handleTextDeletion(getSelectionStart() - 1, getSelectionEnd());
//    } else {
//      handleTextDeletion(getSelectionStart(), getSelectionEnd());
//    }
  }

}
