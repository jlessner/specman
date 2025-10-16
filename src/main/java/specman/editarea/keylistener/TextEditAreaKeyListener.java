package specman.editarea.keylistener;

import specman.EditorI;
import specman.Specman;
import specman.editarea.AbstractListItemEditArea;
import specman.editarea.EditContainer;
import specman.editarea.StepnumberLink;
import specman.editarea.TextEditArea;
import specman.editarea.document.WrappedDocument;
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

public class TextEditAreaKeyListener extends AbstractKeyEventHandler implements KeyListener {
  public TextEditAreaKeyListener(TextEditArea textArea) {
    super(textArea);
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.isControlDown() && e.getKeyCode() == 'V') {
      new KeyPastePressedHandler(textArea).keyPastePressed();
    }
    if (e.isControlDown() && e.getKeyCode() == 'X') {
      markSelectedTextAsDeletedInModificationMode();
    }
    switch (e.getKeyCode()) {
      case KeyEvent.VK_BACK_SPACE -> new KeyBackspacePressedHandler(textArea).keyBackspacePressed(e);
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

  @Override
  public void keyReleased(KeyEvent e) {
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

}
