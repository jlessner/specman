package specman.editarea.keylistener;

import specman.EditorI;
import specman.Specman;
import specman.editarea.StepnumberLink;
import specman.editarea.TextEditArea;
import specman.editarea.document.WrappedDocument;
import specman.editarea.document.WrappedElement;
import specman.editarea.document.WrappedPosition;
import specman.undo.UndoableStepnumberLinkRemoved;
import specman.undo.manager.UndoRecording;
import specman.view.AbstractSchrittView;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import java.awt.event.KeyEvent;

import static specman.editarea.TextStyles.deletedStepnumberLinkStyle;
import static specman.editarea.TextStyles.geloeschtStil;
import static specman.editarea.markups.CharType.ParagraphBoundary;

class BackspaceKeyPressedHandler extends AbstractKeyEventHandler {
  BackspaceKeyPressedHandler(TextEditArea textArea, KeyEvent keyEvent) {
    super(textArea, keyEvent);
  }

  void handle() {
    WrappedPosition caretPos = getWrappedCaretPosition();
    if (dissolveEdiArea(textArea, caretPos)) {
      event.consume();
      return;
    }
    if (shouldPreventActionInsideStepnumberLink()) {
      skipToStepnumberLinkEnd();
      event.consume();
      return;
    }
    if (removeTrailingEmptyLine()) {
      event.consume();
      return;
    }
    if (isTrackingChanges()) {
      handleTextDeletion();
      event.consume();
    }
    else if (stepnumberLinkStyleSet(getWrappedSelectionEnd().dec())) {
      removeStepnumberLinkBefore();
      event.consume();
    }
    else if (!caretPos.isZero() && ParagraphBoundary.at(caretPos.dec())) {
      // We are about to merge two paragraphs, so must ensure markup recovery
      backupMarkupsAndRecoverAfterDefaultKeyOperation();
    }
  }

  /** This compensates a nasty glitch in JEditorPane: usually the underlying document
   * ends with an invisible newline. However, when the user types RETURN at the end of
   * the text, this causes a new empty line to appear in the UI without any change in
   * the document. The ending newline is now visible, so to say, and the use can not
   * get rid of it by default. So we check for this situation here and cleen up the
   * text by re-resetting the complete content which causes thr UI to be corrected. */
  private boolean removeTrailingEmptyLine() {
    WrappedPosition caretPositon = getWrappedCaretPosition();
    WrappedPosition startOffset = getWrappedSelectionStart();
    WrappedPosition endOffset = getWrappedSelectionEnd();

    if (endOffset.equals(startOffset) && caretPositon.isLast() && !caretPositon.isZero()) {
      try {
        int rowStart = Utilities.getRowStart(textArea, textArea.getCaretPosition());
        int rowEnd = Utilities.getRowEnd(textArea, textArea.getCaretPosition());
        if (rowStart == rowEnd) {
          try(UndoRecording ur = Specman.instance().composeUndo()) {
            cleanupText();
          }
          return true;
        }
      }
      catch(BadLocationException blx) {
        throw new RuntimeException(blx);
      }
    }
    return false;
  }

  /** When pressing BACKSPACE at the very beginning of a text edit area we check if this
   * will cause the area to be dissolved by merging its content to other areas. However,
   * we do that only of there is no text selected at this moment. A text selection will
   * cause the ordinary handling to take place. */
  private boolean dissolveEdiArea(TextEditArea textArea, WrappedPosition caretPos) {
    if (caretPos.isZero()) {
      if (textArea.getSelectionStart() == textArea.getSelectionEnd()) {
        return textArea.dissolveEditArea() != null;
      }
    }
    return false;
  }

  private void handleTextDeletion() {
    if (getSelectionStart() == getSelectionEnd()) {
      handleTextDeletion(getSelectionStart() - 1, getSelectionEnd());
    } else {
      handleTextDeletion(getSelectionStart(), getSelectionEnd());
    }
  }

  /**
   * This handles the deletion/mark as deletion of text. <p>
   * When looping backwards through the selected text, there's no need to worry
   * about the next text position being moved because of a deletion.
   * <p>
   * When deleting a range there can be the following items in it: <p>
   * - Normal text - White Background - Gets marked as deleted <p>
   * - Changed text - Yellow Background - Gets deleted <p>
   * - Marked as deleted text - Yellow Background with strikethrough - No changes
   */
  private void handleTextDeletion(int pStartOffset, int pEndOffset) {
    if (pStartOffset <= 0) {
      setCaretPosition(1);
      return;
    }

    WrappedPosition startOffset = getWrappedDocument().fromUI(pStartOffset);
    WrappedPosition endOffset = getWrappedDocument().fromUI(pEndOffset);

    EditorI editor = Specman.instance();

    try (UndoRecording ur = editor.composeUndo()) {
      for (WrappedPosition currentEndPosition = endOffset; currentEndPosition.greater(startOffset); ) { // The missing position-- is intended, see below
        WrappedElement element = getWrappedDocument().getCharacterElement(currentEndPosition.dec()); // -1 since we look at the previous character
        WrappedPosition linkStilStart = element.getStartOffset();
        WrappedPosition linkStilEnd = element.getEndOffset();
        WrappedPosition currentStartPosition = startOffset.max(linkStilStart);
        int length = currentEndPosition.distance(currentStartPosition);

        if (length < 1) {
          throw new RuntimeException("Deletion length <= 1. There seems to be a bug in this method().");
        }

        if (elementIsChangedButNotMarkedAsDeleted(element)) {
          if (stepnumberLinkChangedStyleSet(element)) {
            removeTextAndUnregisterStepnumberLinks(linkStilStart, linkStilEnd, editor);
          } else {
            removeTextAndUnregisterStepnumberLinks(currentStartPosition, currentEndPosition, editor);
          }
        } else {
          if (elementHatDurchgestrichenenText(element)) { // No need to reapply deletedStyle if it's already set
            if (stepnumberLinkChangedStyleSet(currentStartPosition)) {
              setCaretPosition(linkStilStart.unwrap());
            } else {
              setCaretPosition(currentStartPosition.unwrap());
            }
          } else if (stepnumberLinkNormalStyleSet(currentStartPosition)) {
            markRangeAsDeleted(linkStilStart, linkStilEnd.distance(linkStilStart), deletedStepnumberLinkStyle);
            setCaretPosition(linkStilStart.unwrap());
          } else {
            markRangeAsDeleted(currentStartPosition, length, geloeschtStil);
            setCaretPosition(currentStartPosition.unwrap());
          }
        }

        currentEndPosition = currentEndPosition.dec(length); // Skip already processed positions
      }
    }

  }

  public void removeStepnumberLinkBefore() {
    EditorI editor = Specman.instance();
    try (UndoRecording ur = editor.composeUndo()) {
      WrappedPosition position = getWrappedSelectionEnd().dec();
      WrappedPosition startOffset = getWrappedSelectionStart().min(getStartOffsetFromPosition(position));
      WrappedPosition endOffset = getEndOffsetFromPosition(position);
      removeTextAndUnregisterStepnumberLinks(startOffset, endOffset, editor);
    }
  }

  private void removeTextAndUnregisterStepnumberLinks(WrappedPosition startOffset, WrappedPosition endOffset, EditorI editor) {
    if (startOffset.greater(endOffset)) {
      throw new IllegalArgumentException("StartOffSet is greater than EndOffset - Make sure not to set the length as endOffset");
    }

    WrappedDocument doc = getWrappedDocument();

    for (WrappedPosition currentOffset = startOffset; currentOffset.less(endOffset); ) { // The missing currentOffset++ is intended
      WrappedPosition currentEndOffset = getEndOffsetFromPosition(currentOffset);
      int length = currentEndOffset.distance(currentOffset);
      WrappedElement element = doc.getCharacterElement(currentOffset);

      if (stepnumberLinkStyleSet(element)) {
        String stepnumberLinkID = getStepnumberLinkIDFromElement(currentOffset, currentEndOffset);
        if (!StepnumberLink.isStepnumberLinkDefect(stepnumberLinkID)) {
          AbstractSchrittView step = editor.findStepByStepID(stepnumberLinkID);
          step.unregisterStepnumberLink(textArea);
          editor.addEdit(new UndoableStepnumberLinkRemoved(step, textArea));
        }
      }

      currentOffset = currentOffset.inc(length); // Skip already processed positions
    }

    doc.remove(startOffset, endOffset.distance(startOffset));
  }

  private boolean stepnumberLinkChangedStyleSet(WrappedPosition position) {
    WrappedDocument doc = getWrappedDocument();
    return stepnumberLinkChangedStyleSet(doc.getCharacterElement(position));
  }

  private boolean elementIsChangedButNotMarkedAsDeleted(WrappedElement element) {
    return (elementHatAenderungshintergrund(element) || stepnumberLinkChangedStyleSet(element))
      && !elementHatDurchgestrichenenText(element);
  }

}
