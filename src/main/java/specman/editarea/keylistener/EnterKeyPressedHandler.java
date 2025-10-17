package specman.editarea.keylistener;

import specman.Specman;
import specman.editarea.AbstractListItemEditArea;
import specman.editarea.EditContainer;
import specman.editarea.TextEditArea;
import specman.editarea.markups.MarkedCharSequence;
import specman.editarea.markups.MarkupBackgroundStyleInitializer;
import specman.editarea.markups.MarkupRecovery;
import specman.model.v001.Markup_V001;
import specman.undo.manager.UndoRecording;

import javax.swing.*;
import java.awt.event.KeyEvent;

class EnterKeyPressedHandler extends AbstractKeyEventHandler {
  EnterKeyPressedHandler(TextEditArea textArea, KeyEvent keyEvent) {
    super(textArea, keyEvent);
  }

  void handle() {
    if (!isEditable()) {
      event.consume();
      return;
    }
    EditContainer editContainer = textArea.getParent();
    if (!event.isShiftDown()) {
      if (editContainer.getParent() instanceof AbstractListItemEditArea) {
        AbstractListItemEditArea listItem = (AbstractListItemEditArea) editContainer.getParent();
        listItem.split(textArea);
        event.consume();
        return;
      }
      MarkedCharSequence changes = findMarkups();
      changes.insertParagraphBoundaryAt(getWrappedCaretPosition(), Specman.instance().aenderungenVerfolgen());
      // We start the Undo composition here and close it in the invokeLater section to cover both
      // - all the changes from JEditoPane when inserting a new paragraph and
      // - all changes required for changemark recovery
      UndoRecording ur = Specman.instance().composeUndo();
      SwingUtilities.invokeLater(() -> {
        java.util.List<Markup_V001> recoveredChangemarks = new MarkupRecovery(getWrappedDocument(), changes).recover();
        new MarkupBackgroundStyleInitializer(textArea, recoveredChangemarks).styleChangedTextSections();
        ur.close();
      });
    }
  }

}
