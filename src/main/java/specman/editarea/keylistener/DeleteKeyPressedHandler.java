package specman.editarea.keylistener;

import specman.editarea.TextEditArea;
import specman.editarea.document.WrappedPosition;

import java.awt.event.KeyEvent;

import static specman.editarea.markups.CharType.ParagraphBoundary;

class DeleteKeyPressedHandler extends AbstractRemovalKeyPressedHandler {
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

  protected void handleTextDeletion() {
    int deleteTo = (getSelectionStart() == getSelectionEnd())
      ? getSelectionEnd() + 1
      : getSelectionEnd();
    handleTextDeletion(getSelectionStart(), deleteTo);
    // If the deleted text reaches up to the very end of the text area's content,
    // we can't move the caret beyond that position.
    if (!getWrappedDocument().fromUI(deleteTo).exists()) {
      deleteTo--;
    }
    setCaretPosition(deleteTo);
  }

}
