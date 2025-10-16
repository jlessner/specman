package specman.editarea.keylistener;

import specman.EditorI;
import specman.Specman;
import specman.editarea.AbstractListItemEditArea;
import specman.editarea.EditContainer;
import specman.editarea.StepnumberLink;
import specman.editarea.TextEditArea;
import specman.editarea.document.WrappedDocument;
import specman.editarea.document.WrappedDocumentI;
import specman.editarea.document.WrappedElement;
import specman.editarea.document.WrappedPosition;
import specman.editarea.focusmover.CrossEditAreaFocusMoverFromText;
import specman.editarea.markups.MarkedChar;
import specman.editarea.markups.MarkedCharSequence;
import specman.editarea.markups.MarkupBackgroundStyleInitializer;
import specman.editarea.markups.MarkupRecovery;
import specman.model.v001.Markup_V001;
import specman.undo.UndoableStepnumberLinkRemoved;
import specman.undo.manager.UndoRecording;
import specman.view.AbstractSchrittView;

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.CSS;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.List;

import static specman.editarea.TextStyles.INDIKATOR_GELOESCHT_MARKIERT;
import static specman.editarea.TextStyles.INDIKATOR_GRAU;
import static specman.editarea.TextStyles.INDIKATOR_SCHWARZ;
import static specman.editarea.TextStyles.deletedStepnumberLinkStyle;
import static specman.editarea.TextStyles.geaendertTextBackground;
import static specman.editarea.TextStyles.geloeschtStil;
import static specman.editarea.TextStyles.standardStil;
import static specman.editarea.markups.CharType.ParagraphBoundary;
import static specman.editarea.markups.CharType.Whitespace;

public class TextEditAreaKeyListener implements KeyListener {
  private final TextEditArea textArea;

  public TextEditAreaKeyListener(TextEditArea textArea) {
    this.textArea = textArea;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.isControlDown() && e.getKeyCode() == 'V') {
      keyPastePressed(e);
    }
    if (e.isControlDown() && e.getKeyCode() == 'X') {
      markSelectedTextAsDeletedInModificationMode();
    }
    switch (e.getKeyCode()) {
      case KeyEvent.VK_BACK_SPACE -> keyBackspacePressed(e);
      case KeyEvent.VK_UP -> keyUpPressed(e);
      case KeyEvent.VK_DOWN -> keyDownPressed(e);
      case KeyEvent.VK_LEFT -> keyLeftPressed(e);
      case KeyEvent.VK_RIGHT -> keyRightPressed(e);
      case KeyEvent.VK_ENTER -> keyEnterPressed(e);
      default -> {
        if (shouldPreventActionInsideStepnumberLink()) {
          e.consume();
          return;
        }
      }
    }

    if (isTrackingChanges()) {
      aenderungsStilSetzenWennNochNichtVorhanden();
    } else {
      standardStilSetzenWennNochNichtVorhanden();
    }
  }

  /** If there is a whitespace directly in front or behind the caret position,
   * we do not want to insert another whitespace there. The same is true if the
   * caret is at the very beginning of the document or at the beginning of a paragraph. */
  private void keySpaceTyped(KeyEvent e) {
    WrappedDocument doc = getWrappedDocument();
    WrappedPosition caret = getWrappedCaretPosition();
    if (caret.isZero() ||
      Whitespace.at(caret) ||
      Whitespace.at(caret.dec()) ||
      ParagraphBoundary.at(caret.dec())) {
      e.consume();
    }
  }

  private void keyPastePressed(KeyEvent e) {
    try {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable contents = clipboard.getContents(null);
      if (contents != null) {
        // If we got string content on the clipboard, force it to become plain text for the JEditorPane
        // paste operation. There are text sources like Microsoft Word which cause a complete mess
        // in the resulting HTML otherwise.
        if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          String stringOnly = (String)contents.getTransferData(DataFlavor.stringFlavor);
          contents = new StringSelection(stringOnly);
          clipboard.setContents(contents, null);
        }
        if (contents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
          BufferedImage image = (BufferedImage) contents.getTransferData(DataFlavor.imageFlavor);
          textArea.addImage(image);
        }
      }
    }
    catch(Exception x) {
      x.printStackTrace();
    }
  }

  private void keyEnterPressed(KeyEvent e) {
    if (!isEditable()) {
      e.consume();
      return;
    }
    EditContainer editContainer = textArea.getParent();
    if (!e.isShiftDown()) {
      if (editContainer.getParent() instanceof AbstractListItemEditArea) {
        AbstractListItemEditArea listItem = (AbstractListItemEditArea) editContainer.getParent();
        listItem.split(textArea);
        e.consume();
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

  public MarkedCharSequence findMarkups() {
    MarkedCharSequence seq = new MarkedCharSequence();
    WrappedDocument doc = getWrappedDocument();
    for (WrappedPosition p = doc.fromModel(0); p.exists(); p = p.inc()) {
      MarkedChar c = new MarkedChar(doc, p);
      seq.add(c);
    }
    return seq;
  }

  private void keyRightPressed(KeyEvent e) {
    if (skipToStepnumberLinkEnd()) {
      e.consume();
    }
  }

  private void keyLeftPressed(KeyEvent e) {
    if (skipToStepnumberLinkStart()) {
      e.consume();
    }
  }

  private boolean skipToStepnumberLinkStart() { return textArea.skipToStepnumberLinkStart(); }

  private void keyUpPressed(KeyEvent e) {
    new CrossEditAreaFocusMoverFromText(textArea).moveFocusToPreceedingEditArea();
  }

  private void keyDownPressed(KeyEvent e) {
    new CrossEditAreaFocusMoverFromText(textArea).moveFocusToSucceedingEditArea();
  }

  private void keyBackspacePressed(KeyEvent e) {
    WrappedPosition caretPos = getWrappedCaretPosition();
    if (caretPos.isZero()) {
      textArea.dissolveEditArea();
      return;
    }
    if (shouldPreventActionInsideStepnumberLink()) {
      skipToStepnumberLinkEnd();
      e.consume();
      return;
    }
    if (isTrackingChanges()) {
      handleTextDeletion();
      e.consume();
    }
    else if (stepnumberLinkNormalOrChangedStyleSet(getWrappedSelectionEnd().dec())) {
      removePreviousStepnumberLink();
      e.consume();
    }
    else if (ParagraphBoundary.at(caretPos.dec())) {
      // We are about to merge two paragraphs, so must ensure markup recovery
      MarkedCharSequence marksBackup = findMarkups();
      UndoRecording ur = Specman.instance().composeUndo();
      SwingUtilities.invokeLater(() -> {
        List<Markup_V001> recoveredChangemarks = new MarkupRecovery(getWrappedDocument(), marksBackup).recover();
        new MarkupBackgroundStyleInitializer(textArea, recoveredChangemarks).styleChangedTextSections();
        ur.close();
      });
    }
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

  @Override
  public void keyTyped(KeyEvent e) {
    if (e.getKeyChar() == KeyEvent.VK_SPACE) {
      keySpaceTyped(e);
    }
    if (e.getKeyCode() == 0) {
      // This is indicator for some control action like copy or paste rather than entering or deleting text.
      // In this case we skip the following special behaviour logic. Control actions should have already
      // been handled in keyPressed().
      return;
    }
    if (shouldPreventActionInsideStepnumberLink()) {
      e.consume();
      return;
    }
    markSelectedTextAsDeletedInModificationMode();
  }

  public boolean shouldPreventActionInsideStepnumberLink() {
    if (stepnumberLinkNormalOrChangedStyleSet(getWrappedSelectionStart()) || stepnumberLinkNormalOrChangedStyleSet(getWrappedSelectionEnd())) {
      if (isCaretInsideSelection()) {
        return true;
      }

      for (WrappedPosition i = getWrappedSelectionStart(); i.less(getWrappedSelectionEnd()); i.inc()) {
        if (stepnumberLinkNormalOrChangedStyleSet(i)) {
          if (getStartOffsetFromPosition(i).less(getWrappedSelectionStart()) ||
            getEndOffsetFromPosition(i).greater(getWrappedSelectionEnd())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean isCaretInsideSelection() {
    WrappedPosition linkStyleStart = getStartOffsetFromPosition(getWrappedSelectionEnd());
    WrappedPosition linkStyleEnd = getEndOffsetFromPosition(getWrappedSelectionEnd());
    return getWrappedSelectionStart().equals(getWrappedSelectionEnd()) &&
      getWrappedSelectionEnd().less(linkStyleEnd) &&
      getWrappedSelectionStart().greater(linkStyleStart);
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }

  public void removePreviousStepnumberLink() {
    EditorI editor = Specman.instance();
    try (UndoRecording ur = editor.composeUndo()) {
      WrappedPosition position = getWrappedSelectionEnd().dec();
      WrappedPosition startOffset = getWrappedSelectionStart().min(getStartOffsetFromPosition(position));
      WrappedPosition endOffset = getEndOffsetFromPosition(position);
      removeTextAndUnregisterStepnumberLinks(startOffset, endOffset, editor);
    }
  }

  public void markSelectedTextAsDeletedInModificationMode() {
    if (!Specman.instance().aenderungenVerfolgen()) {
      return;
    }
    AbstractSchrittView textOwner = Specman.instance().findeSchritt(textArea);
    if (textOwner != null && isEditable()) {
      WrappedPosition selectionStart = getWrappedSelectionStart();
      WrappedPosition selectionEnd = getWrappedSelectionEnd();

      if (!selectionStart.equals(selectionEnd)) {
        if (stepnumberLinkNormalStyleSet(selectionStart)) {
          markRangeAsDeleted(selectionStart, selectionEnd.distance(selectionStart), deletedStepnumberLinkStyle);
        } else {
          markRangeAsDeleted(selectionStart, selectionEnd.distance(selectionStart), geloeschtStil);
        }

        setSelectionStart(selectionEnd.unwrap());
        // Jetzt ist am Ende der vorherigen Selektion noch der Geloescht-Stil gesetzt
        // D.h. die Durchstreichung muss noch weg für das neue Zeichen, das gerade
        // eingefügt werden soll
        StyledEditorKit k = (StyledEditorKit) getEditorKit();
        MutableAttributeSet inputAttributes = k.getInputAttributes();
        StyleConstants.setStrikeThrough(inputAttributes, false);
      }
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

      if (stepnumberLinkNormalOrChangedStyleSet(element)) {
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

  private boolean skipToStepnumberLinkEnd() {
    WrappedPosition selectionEnd = getWrappedSelectionEnd();
    if (stepnumberLinkNormalOrChangedStyleSet(selectionEnd)) {
      setCaretPosition(getEndOffsetFromPosition(selectionEnd).unwrap());
      return true;
    }
    return false;
  }

  private void markRangeAsDeleted(WrappedPosition deleteStart, int deleteLength, MutableAttributeSet deleteStyle) {
    getWrappedDocument().setCharacterAttributes(deleteStart, deleteLength, deleteStyle, false);
  }

  public void standardStilSetzenWennNochNichtVorhanden() {
    if (!ganzerSchrittGeloeschtStilGesetzt()) {
      StyledEditorKit k = (StyledEditorKit) getEditorKit();
      MutableAttributeSet inputAttributes = k.getInputAttributes();
      inputAttributes.addAttributes(standardStil);
    }
  }

  public void aenderungsStilSetzenWennNochNichtVorhanden() {
    // Durch die folgende If-Abfrage verhindert man, dass die als geändert
    // markierten Buchstaben alle einzelne Elements werden.
    // Wenn an der aktuellen Position schon gelbe Hintegrundfarbe
    // eingestellt ist, dann Ändern wir den aktuellen Style gar nicht mehr.
    if (!aenderungsStilGesetzt() && !stepnumberLinkNormalStyleSet(getWrappedCaretPosition())) {
      StyledEditorKit k = (StyledEditorKit) getEditorKit();
      MutableAttributeSet inputAttributes = k.getInputAttributes();
      StyleConstants.setStrikeThrough(inputAttributes, false); // Falls noch Gelöscht-Stil herrschte
      inputAttributes.addAttributes(geaendertTextBackground);
    }
  }

  public boolean ganzerSchrittGeloeschtStilGesetzt() {
    StyledEditorKit k = getEditorKit();
    MutableAttributeSet inputAttributes = k.getInputAttributes();
    Object currentTextDecoration = inputAttributes.getAttribute(CSS.Attribute.TEXT_DECORATION);
    Object currentFontColorValue = inputAttributes.getAttribute(CSS.Attribute.COLOR);
    if (currentTextDecoration != null && currentTextDecoration.toString().equals(INDIKATOR_GELOESCHT_MARKIERT)
      && currentFontColorValue != null && currentFontColorValue.toString().equals(INDIKATOR_GRAU)) {
      return false;
    }
    Object currentBackgroundColorValue = inputAttributes.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
    return currentBackgroundColorValue != null
      && currentBackgroundColorValue.toString().equalsIgnoreCase(INDIKATOR_SCHWARZ)
      && currentTextDecoration != null
      && currentTextDecoration.toString().equalsIgnoreCase(INDIKATOR_GELOESCHT_MARKIERT)
      && currentFontColorValue != null && currentFontColorValue.toString().equalsIgnoreCase(INDIKATOR_GRAU);
  }

  private boolean aenderungsStilGesetzt() { return textArea.aenderungsStilGesetzt(); }
  private boolean elementHatAenderungshintergrund(WrappedElement element) { return textArea.elementHatAenderungshintergrund(element); }
  private boolean elementHatDurchgestrichenenText(WrappedElement element) { return textArea.elementHatDurchgestrichenenText(element); }
  private boolean stepnumberLinkChangedStyleSet(WrappedElement element) { return textArea.stepnumberLinkChangedStyleSet(element); }
  private String getStepnumberLinkIDFromElement(WrappedPosition start, WrappedPosition end) { return textArea.getStepnumberLinkIDFromElement(start, end); }
  private void setCaretPosition(int position) { textArea.setCaretPosition(position); }
  private boolean stepnumberLinkNormalOrChangedStyleSet(WrappedPosition i) { return textArea.stepnumberLinkNormalOrChangedStyleSet(i); }
  private boolean stepnumberLinkNormalOrChangedStyleSet(WrappedElement e) { return textArea.stepnumberLinkNormalOrChangedStyleSet(e); }
  private WrappedPosition getWrappedSelectionStart() { return textArea.getWrappedSelectionStart(); }
  private WrappedPosition getEndOffsetFromPosition(WrappedPosition position) { return textArea.getEndOffsetFromPosition(position); }
  private WrappedPosition getStartOffsetFromPosition(WrappedPosition position) { return textArea.getStartOffsetFromPosition(position); }
  private WrappedPosition getWrappedSelectionEnd() { return textArea.getWrappedSelectionEnd(); }
  private WrappedPosition getWrappedCaretPosition() { return textArea.getWrappedCaretPosition(); }
  private boolean isTrackingChanges() { return textArea.isTrackingChanges(); }
  private WrappedDocument getWrappedDocument() { return textArea.getWrappedDocument(); }
  private boolean stepnumberLinkNormalStyleSet(WrappedPosition position) { return textArea.stepnumberLinkNormalOrChangedStyleSet(position); }
  private int getSelectionEnd() { return textArea.getSelectionEnd(); }
  private int getSelectionStart() { return textArea.getSelectionStart(); }
  private void setSelectionStart(int position) { textArea.setSelectionStart(position); }
  private boolean isEditable() { return textArea.isEditable(); }
  private StyledEditorKit getEditorKit() { return (StyledEditorKit) textArea.getEditorKit(); }

}
