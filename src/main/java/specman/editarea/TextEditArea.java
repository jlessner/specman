package specman.editarea;

import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import specman.Aenderungsart;
import specman.EditorI;
import specman.Specman;
import specman.editarea.focusmover.CrossEditAreaFocusMoverFromText;
import specman.model.v001.AbstractEditAreaModel_V001;
import specman.model.v001.Aenderungsmarkierung_V001;
import specman.model.v001.GeloeschtMarkierung_V001;
import specman.model.v001.TextEditAreaModel_V001;
import specman.pdf.FormattedShapeText;
import specman.pdf.Shape;
import specman.undo.UndoableStepnumberLinkAdded;
import specman.undo.UndoableStepnumberLinkRemoved;
import specman.undo.manager.UndoRecording;
import specman.undo.props.UDBL;
import specman.view.AbstractSchrittView;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.CSS;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static specman.Aenderungsart.Geloescht;
import static specman.Aenderungsart.Hinzugefuegt;
import static specman.Aenderungsart.Untracked;
import static specman.editarea.HTMLTags.BODY_INTRO;
import static specman.editarea.HTMLTags.BODY_OUTRO;
import static specman.editarea.HTMLTags.HTML_INTRO;
import static specman.editarea.HTMLTags.HTML_OUTRO;
import static specman.editarea.TextStyles.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE;
import static specman.editarea.TextStyles.FONTSIZE;
import static specman.editarea.TextStyles.TEXT_BACKGROUND_COLOR_STANDARD;
import static specman.editarea.TextStyles.INDIKATOR_GELB;
import static specman.editarea.TextStyles.INDIKATOR_GELOESCHT_MARKIERT;
import static specman.editarea.TextStyles.INDIKATOR_GRAU;
import static specman.editarea.TextStyles.INDIKATOR_SCHWARZ;
import static specman.editarea.TextStyles.changedStepnumberLinkHTMLColor;
import static specman.editarea.TextStyles.deletedStepnumberLinkStyle;
import static specman.editarea.TextStyles.font;
import static specman.editarea.TextStyles.ganzerSchrittGeloeschtStil;
import static specman.editarea.TextStyles.geaendertTextBackground;
import static specman.editarea.TextStyles.geloeschtStil;
import static specman.editarea.TextStyles.quellschrittStil;
import static specman.editarea.TextStyles.standardStil;
import static specman.editarea.TextStyles.stepnumberLinkStyleColor;

public class TextEditArea extends JEditorPane implements EditArea, KeyListener {
    private WrappedElement hoveredElement;
    private Aenderungsart aenderungsart;

    public TextEditArea(TextEditAreaModel_V001 model, Font font) {
        this.aenderungsart = model.aenderungsart;
        Specman.instance().instrumentWysEditor(this, model.text, 0);
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        setFont(font);
        addKeyListener(this);
        addMouseListener();
        addMouseMotionListener();
        setBackground(aenderungsart.toBackgroundColor());
        registerToolTipManager();
        styleChangedTextSections(model);
    }

    private void styleChangedTextSections(TextEditAreaModel_V001 model) {
        new ChangeBackgroundStyleInitializer(this, model).styleChangedTextSections();
    }

    private void addMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isControlDown()) {
                    if (stepnumberLinkNormalOrChangedStyleSet(getWrappedCaretPosition())) {
                        scrollToStepnumber();
                    }
                }
            }
        });
    }

    private void addMouseMotionListener() {
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                setHandCursorWhenHoveringStepnumberLink(e);
            }
        });
    }

    /**
     * Changes the cursor when pressing the CONTROL key and hovering over a StepnumberLink.
     * <p>
     * If the text cursor resides in a different TextEditArea than the mouse cursor it's not possible to
     * correctly check both conditions, since they are processed in two different TextEditAreas.
     * So we query if the CONTROL key is currently pressed via a KeyEventDispatcher from {@link Specman}.
     */
    private void setHandCursorWhenHoveringStepnumberLink(MouseEvent e) {
        Point p = new Point(e.getX(), e.getY());
        int textPosition = viewToModel2D(p);

        hoveredElement = getWrappedDocument().getCharacterElement(textPosition);

        EditorI editor = Specman.instance();
        Cursor cursorToUse;
        if (editor.isKeyPressed(KeyEvent.VK_CONTROL) && stepnumberLinkNormalOrChangedStyleSet(hoveredElement)) {
            cursorToUse = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        } else {
            cursorToUse = Cursor.getDefaultCursor();
        }
        editor.setCursor(cursorToUse);
    }

    private void registerToolTipManager() {
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.registerComponent(this);
    }

    @Override public String getToolTipText() {
        if (stepnumberLinkNormalOrChangedStyleSet(hoveredElement)) {
            return "STRG+Klicken um Link zu folgen";
        }
        return super.getToolTipText();
    }

    @Override
    public void addSchrittnummer(SchrittNummerLabel schrittNummer) {
        add(schrittNummer);
    }

    private void setStyleUDBL(MutableAttributeSet attr, Color backgroundColor, boolean editable) {
        getWrappedDocument().setCharacterAttributes(0, getPlainText().length(), attr, false);
        setEditableUDBL(editable);
        setBackgroundUDBL(backgroundColor);
    }

    private void setBackgroundUDBL(Color backgroundColor) { UDBL.setBackgroundUDBL(this, backgroundColor); }
    private void setEditableUDBL(boolean editable) { UDBL.setEditable(this, editable); }

    @Override
    public void setQuellStil() {
        setStyleUDBL(quellschrittStil, AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE, false);
    }

    @Override
    public void aenderungsmarkierungenEntfernen() {
        // Nothing to do for text areas - job is completely done in aenderungenVerwerfen/Uebernehmen
    }

    @Override
    public void setGeloeschtMarkiertStilUDBL() {
        aenderungenVerwerfen();
        setStyleUDBL(ganzerSchrittGeloeschtStil, AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE, false);
        setAenderungsartUDBL(Geloescht);
    }

    @Override
    public Component asComponent() { return this; }

    public String getPlainText() {
        try {
            return getText(0, getDocument().getLength());
        } catch (BadLocationException blx) {
            blx.printStackTrace();
            return null;
        }
    }

    public boolean ganzerSchrittGeloeschtStilGesetzt() {
        StyledEditorKit k = (StyledEditorKit) getEditorKit();
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

    private void standardStilSetzenWennNochNichtVorhanden() {
        if (!ganzerSchrittGeloeschtStilGesetzt()) {
            StyledEditorKit k = (StyledEditorKit) getEditorKit();
            MutableAttributeSet inputAttributes = k.getInputAttributes();
            inputAttributes.addAttributes(standardStil);
        }
    }

    public TextEditAreaModel_V001 getTextMitAenderungsmarkierungen(boolean formatierterText) {
        String text;
        java.util.List<Aenderungsmarkierung_V001> aenderungen = null;
        if (formatierterText) {
            // Wenn wir die Zeilenumbrüche nicht rausnehmen, dann entstehen später beim Laden u.U.
            // Leerzeichen an Zeilenenden, die im ursprünglichen Text nicht drin waren. Das ist doof,
            // weil dann die separat abgespeicherten Textintervalle der Aenderungsmarkierungen nicht mehr passen.
            text = getText().replace("\n", "");
            aenderungen = findeAenderungsmarkierungen(false);
        } else {
            text = getPlainText().replace("\n", " ").trim();
        }
        return new TextEditAreaModel_V001(text, getPlainText(), aenderungen, aenderungsart);
    }

    public java.util.List<Aenderungsmarkierung_V001> findeAenderungsmarkierungen(boolean nurErste) {
        java.util.List<Aenderungsmarkierung_V001> ergebnis = new ArrayList<>();
        WrappedDocument doc = getWrappedDocument();
        for (WrappedElement e : doc.getRootElements()) {
            findeAenderungsmarkierungen(e, ergebnis, nurErste);
            if (!ergebnis.isEmpty() && nurErste) {
                break;
            }
        }
        return ergebnis;
    }

    private void findeAenderungsmarkierungen(WrappedElement e, java.util.List<Aenderungsmarkierung_V001> ergebnis, boolean nurErste) {
        if (elementHatAenderungshintergrund(e)) {
            ergebnis.add(new Aenderungsmarkierung_V001(e.getStartOffset().position, e.getEndOffset().position));
            if (nurErste) {
                return;
            }
        }
        if (ergebnis.isEmpty() || !nurErste) {
            for (int i = 0; i < e.getElementCount(); i++) {
                findeAenderungsmarkierungen(e.getElement(i), ergebnis, nurErste);
                if (!ergebnis.isEmpty() && nurErste) {
                    break;
                }
            }
        }
    }

    // TODO JL: Muss mit aenderungsmarkierungenVerwerfen zusammengelegt werden
    public int aenderungenUebernehmen() {
        EditorI editor = Specman.instance();
        WrappedDocument doc = getWrappedDocument();
        int changesMade = aenderungsart.asNumChanges();

        List<GeloeschtMarkierung_V001> loeschungen = new ArrayList<>();
        for (WrappedElement e : doc.getRootElements()) {
            changesMade += aenderungsmarkierungenUebernehmen(e, loeschungen);
        }
        for (int i = 0; i < loeschungen.size(); i++) {
            GeloeschtMarkierung_V001 loeschung = loeschungen.get((loeschungen.size()) - 1 - i);
            try {
                WrappedPosition loeschungVon = doc.wrap(loeschung.getVon());
                WrappedPosition loeschungBis = doc.wrap(loeschung.getBis());
                removeTextAndUnregisterStepnumberLinks(loeschungVon, loeschungBis, editor);
                changesMade++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return changesMade;
    }

    // TODO JL: Muss mit aenderungsmarkierungenUebernehmen zusammengelegt werden
    public int aenderungenVerwerfen() {
        int changesReverted = aenderungsart.asNumChanges();
        if (aenderungsart == Hinzugefuegt) {
            if (!areaDetachedByMerge()) {
                getParent().removeEditAreaUDBL(this);
            }
        }
        else {
            EditorI editor = Specman.instance();

            WrappedDocument doc = getWrappedDocument();
            List<GeloeschtMarkierung_V001> loeschungen = new ArrayList<>();
            for (WrappedElement e : doc.getRootElements()) {
                changesReverted += aenderungsmarkierungenVerwerfen(e, loeschungen);
            }
            for (int i = 0; i < loeschungen.size(); i++) {
                GeloeschtMarkierung_V001 loeschung = loeschungen.get((loeschungen.size()) - 1 - i);
                try {
                    WrappedPosition loeschungVon = doc.wrap(loeschung.getVon());
                    WrappedPosition loeschungBis = doc.wrap(loeschung.getBis());
                    removeTextAndUnregisterStepnumberLinks(loeschungVon, loeschungBis, editor);
                    changesReverted++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        aenderungsart = Untracked;
        return changesReverted;
    }

    /** If an image edit area is removed while discarding changes, this text area
     * may be the directly following one in the same edit container and may be
     * merged with another text area directly above the image area. We can detect
     * this situation by this text area being no longer attached to its parent. */
    private boolean areaDetachedByMerge() { return getParent() == null; }

    // TODO JL: Muss mit aenderungsmarkierungenVerwerfen zusammengelegt werden
    private int aenderungsmarkierungenUebernehmen(WrappedElement e, List<GeloeschtMarkierung_V001> loeschungen) {
        int changesMade = 0;

        WrappedDocument doc = e.getDocument();
        if (elementHatAenderungshintergrund(e)) {
            if (elementHatDurchgestrichenenText(e)) {
                loeschungen.add(new GeloeschtMarkierung_V001(e.getStartOffset().position, e.getEndOffset().position));
            } else {
                AttributeSet attribute = e.getAttributes();
                MutableAttributeSet entfaerbt = new SimpleAttributeSet();
                entfaerbt.addAttributes(attribute);
                StyleConstants.setBackground(entfaerbt, stepnumberLinkChangedStyleSet(e) ? stepnumberLinkStyleColor : TEXT_BACKGROUND_COLOR_STANDARD);
                doc.setCharacterAttributes(e.getStartOffset(), e.getEndOffset().distance(e.getStartOffset()), entfaerbt, true);
                changesMade++;
            }

        }

        for (int i = 0; i < e.getElementCount(); i++) {
            changesMade += aenderungsmarkierungenUebernehmen(e.getElement(i), loeschungen);
        }

        aenderungsart = Untracked;
        return changesMade;
    }

    // TODO JL: Muss mit aenderungsmarkierungenUebernehmen zusammengelegt werden
    private int aenderungsmarkierungenVerwerfen(
            WrappedElement e,
            List<GeloeschtMarkierung_V001> loeschungen) {
        int changesReverted = 0;

        WrappedDocument doc = getWrappedDocument();
        if (elementHatAenderungshintergrund(e)) {
            if (!elementHatDurchgestrichenenText(e)) {
                loeschungen.add(new GeloeschtMarkierung_V001(e.getStartOffset().position, e.getEndOffset().position));
            } else {
                AttributeSet attribute = e.getAttributes();
                MutableAttributeSet entfaerbt = new SimpleAttributeSet();
                entfaerbt.addAttributes(attribute);
                StyleConstants.setBackground(entfaerbt, stepnumberLinkChangedStyleSet(e) ? stepnumberLinkStyleColor : TEXT_BACKGROUND_COLOR_STANDARD);
                StyleConstants.setStrikeThrough(entfaerbt, false);
                doc.setCharacterAttributes(e.getStartOffset(), e.getEndOffset().distance(e.getStartOffset()), entfaerbt, true);
                changesReverted++;
            }

        }
        for (int i = 0; i < e.getElementCount(); i++) {
            changesReverted += aenderungsmarkierungenVerwerfen(e.getElement(i), loeschungen);
        }

        return changesReverted;
    }

    private boolean elementHatDurchgestrichenenText(WrappedElement e) {
        AttributeSet attr = e.getAttributes();
        return StyleConstants.isStrikeThrough(attr);
    }

    private boolean elementHatAenderungshintergrund(WrappedElement e) {
        String backgroundColorValue = getBackgroundColorFromElement(e);
        if (backgroundColorValue != null) {
            return backgroundColorValue.equals(INDIKATOR_GELB) || backgroundColorValue.equals(changedStepnumberLinkHTMLColor);
        }
        return false;
    }

    public boolean aenderungsStilGesetzt() {
        StyledEditorKit k = (StyledEditorKit) getEditorKit();
        MutableAttributeSet inputAttributes = k.getInputAttributes();
        Object currentTextDecoration = inputAttributes.getAttribute(CSS.Attribute.TEXT_DECORATION);
        if (currentTextDecoration != null && currentTextDecoration.toString().equals(INDIKATOR_GELOESCHT_MARKIERT)) {
            return false;
        }
        Object currentBackgroundColorValue = inputAttributes.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
        return (currentBackgroundColorValue != null && currentBackgroundColorValue.toString().equalsIgnoreCase(INDIKATOR_GELB));
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

    private WrappedPosition getWrappedCaretPosition() {
        return getWrappedDocument().wrap(getCaretPosition());
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
                    addImage(image);
                }
            }
        }
        catch(Exception x) {
            x.printStackTrace();
        }
    }

    private void keyEnterPressed(KeyEvent e) {
        EditContainer editContainer = getParent();
        if (!e.isShiftDown() && editContainer.getParent() instanceof AbstractListItemEditArea) {
            ((AbstractListItemEditArea)editContainer.getParent()).split(this);
            e.consume();
            return;
        }
        addNewlineAtCaret(e);
    }

    /** The original code causes text background style loss in the edited line,
     * so we simply do what we need by our own. */
    private void addNewlineAtCaret(KeyEvent e) {
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                StyledDocument doc = (StyledDocument) getDocument();
//                doc.setCharacterAttributes(1, 5, geaendertTextBackground, false);
//            }
//        });
//        try {
//            StyledDocument doc = (StyledDocument) getDocument();
//            int caretPosition = getCaretPosition();
//            doc.insertString(caretPosition, "\n", null);
//            e.consume();
//        }
//        catch(BadLocationException blx) {
//            blx.printStackTrace();
//        }
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

    private void keyUpPressed(KeyEvent e) {
        new CrossEditAreaFocusMoverFromText(this).moveFocusToPreceedingEditArea();
    }

    private void keyDownPressed(KeyEvent e) {
        new CrossEditAreaFocusMoverFromText(this).moveFocusToSucceedingEditArea();
    }

    private void keyBackspacePressed(KeyEvent e) {
        if (getCaretPosition() == 0) {
            dissolveEditArea();
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
    }

    /** When pressing backspace at the very beginning of a text area, we check if this indicates that the
     * edit area should be dissolved in the sense that its is removed and its content is merged with the
     * one of a preceeding edit area. */
    private void dissolveEditArea() {
        getParent().tryDissolveEditArea(this);
    }

    private void handleTextDeletion() {
        if (getSelectionStart() == getSelectionEnd()) {
            handleTextDeletion(getSelectionStart() - 1, getSelectionEnd());
        } else {
            handleTextDeletion(getSelectionStart(), getSelectionEnd());
        }
    }

    private void markRangeAsDeleted(WrappedPosition deleteStart, int deleteLength, MutableAttributeSet deleteStyle) {
        getWrappedDocument().setCharacterAttributes(deleteStart, deleteLength, deleteStyle, false);
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

        WrappedPosition startOffset = getWrappedDocument().wrap(pStartOffset);
        WrappedPosition endOffset = getWrappedDocument().wrap(pEndOffset);

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

    private boolean elementIsChangedButNotMarkedAsDeleted(WrappedElement element) {
        return (elementHatAenderungshintergrund(element) || stepnumberLinkChangedStyleSet(element))
                && !elementHatDurchgestrichenenText(element);
    }

    private boolean shouldPreventActionInsideStepnumberLink() {
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
                    step.unregisterStepnumberLink(this);
                    editor.addEdit(new UndoableStepnumberLinkRemoved(step, this));
                }
            }

            currentOffset = currentOffset.inc(length); // Skip already processed positions
        }

        doc.remove(startOffset, endOffset.distance(startOffset));
    }

    private void removePreviousStepnumberLink() {
        EditorI editor = Specman.instance();
        try (UndoRecording ur = editor.composeUndo()) {
            WrappedPosition position = getWrappedSelectionEnd().dec();
            WrappedPosition startOffset = getWrappedSelectionStart().min(getStartOffsetFromPosition(position));
            WrappedPosition endOffset = getEndOffsetFromPosition(position);
            removeTextAndUnregisterStepnumberLinks(startOffset, endOffset, editor);
        }
    }

    private boolean skipToStepnumberLinkStart() {
        WrappedPosition selectionEnd = getWrappedSelectionEnd();
        if (stepnumberLinkNormalOrChangedStyleSet(selectionEnd.dec())) {
            setCaretPosition(getStartOffsetFromPosition(selectionEnd.dec()).unwrap());
            return true;
        }
        return false;
    }

    private boolean skipToStepnumberLinkEnd() {
        WrappedPosition selectionEnd = getWrappedSelectionEnd();
        if (stepnumberLinkNormalOrChangedStyleSet(selectionEnd)) {
            setCaretPosition(getEndOffsetFromPosition(selectionEnd).unwrap());
            return true;
        }
        return false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
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

    private void markSelectedTextAsDeletedInModificationMode() {
        if (!Specman.instance().aenderungenVerfolgen()) {
            return;
        }
        AbstractSchrittView textOwner = Specman.instance().findeSchritt(this);
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

    private WrappedPosition getWrappedSelectionEnd() {
        return getWrappedDocument().wrap(getSelectionEnd());
    }

    private WrappedPosition getWrappedSelectionStart() {
        return getWrappedDocument().wrap(getSelectionStart());
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public EditContainer getParent() {
        return (EditContainer) super.getParent();
    }

    public void addImage(BufferedImage image) {
        getParent().addImageUDBL(image, this);
    }

    public EditArea addTable(int columns, int rows, Aenderungsart aenderungsart) {
        return getParent().addTableUDBL(this, columns, rows, aenderungsart);
    }

    public EditArea toggleListItemUDBL(boolean ordered, Aenderungsart aenderungsart) {
        // TODO JL: Evt. noch schlauer machen. Toggle von Ordered -> Unordered und umgekehrt
        EditContainer editContainer = getParent();
        if (editContainer.getParent() instanceof UnorderedListItemEditArea) {
            UnorderedListItemEditArea unorderedListItemEditArea = (UnorderedListItemEditArea) editContainer.getParent();
            return unorderedListItemEditArea.getParent().dissolveListItemEditAreaUDBL(unorderedListItemEditArea, aenderungsart);
        }
        if (editContainer.getParent() instanceof OrderedListItemEditArea) {
            OrderedListItemEditArea orderedListItemEditArea = (OrderedListItemEditArea) editContainer.getParent();
            return orderedListItemEditArea.getParent().dissolveListItemEditAreaUDBL(orderedListItemEditArea, aenderungsart);
        }
        else {
            return getParent().addListItemUDBL(this, ordered, aenderungsart);
        }
    }

    @Override
    public AbstractEditAreaModel_V001 toModel(boolean formatierterText) {
        return getTextMitAenderungsmarkierungen(formatierterText);
    }

    @Override
    public void skalieren(int prozentNeu, int prozentAktuell) {
        setFont(font.deriveFont((float) FONTSIZE * prozentNeu / 100));
    }

    public TextEditArea split(int textPosition) {
        int textLength = getDocument().getLength();
        if (textLength > textPosition) {
            TextEditAreaModel_V001 splittedModel = new TextEditAreaModel_V001(getText(), getPlainText(), new ArrayList<>(), aenderungsart);
            TextEditArea splittedArea = new TextEditArea(splittedModel, this.getFont());
            remove(textPosition, textLength - textPosition);
            //int removeStart = splittedArea.newlineAt(0) ? 1 : 0;
            splittedArea.remove(0, textPosition);
            return splittedArea;
        }
        return null;
    }

    public TextEditArea copySection(int fromPosition, int toPosition) {
        TextEditAreaModel_V001 selectionModel = new TextEditAreaModel_V001(getText(), getPlainText(), new ArrayList<>(), aenderungsart);
        TextEditArea selectionArea = new TextEditArea(selectionModel, this.getFont());
        selectionArea.shrink(fromPosition, toPosition);
        return selectionArea.getLength() > 0 ? selectionArea : null;
    }

    public void shrink(int fromPosition, int toPosition) {
        int textLength = getDocument().getLength();
        if (toPosition < fromPosition) {
            remove(0, textLength);
        }
        else {
            if (textLength > toPosition) {
                remove(toPosition + 1, textLength - toPosition - 1);
            }
            if (fromPosition > 0) {
                remove(0, fromPosition);
            }
        }
    }

    public void remove(int offset, int len) {
        try {
            getDocument().remove(offset, len);
        }
        catch (BadLocationException blx) {
            throw new RuntimeException(blx);
        }
    }

    @Override
    public TextEditArea asTextArea() {
        return this;
    }

    @Override
    public boolean isTextArea() { return true; }

    @Override
    public ImageEditArea asImageArea() {
        return null;
    }

    public void appendText(String trailingText) {
        int endOfOldText = getDocument().getLength();
        String oldText = getText();
        String newText =
                oldText
                        .replace(HTML_OUTRO, "")
                        .replace(BODY_OUTRO, "")
                        + trailingText
                        .replace(HTML_INTRO, "")
                        .replace(BODY_INTRO, "");
        setText(newText);
        setCaretPosition(endOfOldText);
    }


    public void addStepnumberLink(AbstractSchrittView referencedStep) {
        EditorI editor = Specman.instance();
        try (UndoRecording ur = editor.composeUndo()) {
            String stepnumberText = referencedStep.getId().toString();

            WrappedDocument doc = getWrappedDocument();
            WrappedPosition caretPos = getWrappedCaretPosition();

            // Add space between two stepnumberlinks to prevent merging them
            if (stepnumberLinkNormalOrChangedStyleSet(caretPos.dec())) {
                doc.insertString(caretPos, " ", null);
                caretPos = caretPos.inc();
            }

            AttributeSet previousAttribute = doc.getCharacterElement(caretPos).getAttributes();
            MutableAttributeSet stepnumberAttribute = new SimpleAttributeSet(previousAttribute);
            stepnumberAttribute.addAttributes(TextStyles.stepnumberLinkStyle);
            StyleConstants.setBackground(stepnumberAttribute,
                    isTrackingChanges() ? TextStyles.changedStepnumberLinkColor : TextStyles.stepnumberLinkStyleColor);

            doc.insertString(caretPos, stepnumberText, stepnumberAttribute);

            referencedStep.registerStepnumberLink(this);
            editor.addEdit(new UndoableStepnumberLinkAdded(referencedStep, this));
        }
    }

    private boolean stepnumberLinkNormalStyleSet(WrappedPosition position) {
        return stepnumberLinkNormalStyleSet(getWrappedDocument().getCharacterElement(position));
    }

    private boolean stepnumberLinkNormalStyleSet(WrappedElement element) {
        String color = getBackgroundColorFromElement(element);
        return color != null && color.equalsIgnoreCase(TextStyles.stepnumberLinkStyleHTMLColor);
    }


    private boolean stepnumberLinkChangedStyleSet(WrappedPosition position) {
        WrappedDocument doc = getWrappedDocument();
        return stepnumberLinkChangedStyleSet(doc.getCharacterElement(position));
    }

    private boolean stepnumberLinkChangedStyleSet(WrappedElement element) {
        String color = getBackgroundColorFromElement(element);
        return color != null && color.equalsIgnoreCase(TextStyles.changedStepnumberLinkHTMLColor);
    }


    private boolean stepnumberLinkNormalOrChangedStyleSet(WrappedPosition position) {
        return stepnumberLinkNormalOrChangedStyleSet(getWrappedDocument().getCharacterElement(position));
    }

    private boolean stepnumberLinkNormalOrChangedStyleSet(WrappedElement element) {
        return element != null && (stepnumberLinkNormalStyleSet(element) || stepnumberLinkChangedStyleSet(element));
    }


    private String getBackgroundColorFromElement(WrappedElement element) {
        Object backgroundColorValue = element.getAttributes().getAttribute(CSS.Attribute.BACKGROUND_COLOR);
        return backgroundColorValue != null ? backgroundColorValue.toString() : null;
    }

    private WrappedPosition getStartOffsetFromPosition(WrappedPosition position) {
        return getWrappedDocument().getCharacterElement(position).getStartOffset();
    }

    private WrappedPosition getEndOffsetFromPosition(WrappedPosition position) {
        return getWrappedDocument().getCharacterElement(position).getEndOffset();
    }

    private boolean isTrackingChanges() {
        return Specman.instance().aenderungenVerfolgen() && isEditable();
    }

    @Override
    public boolean enthaeltAenderungsmarkierungen() {
        return aenderungsart.istAenderung()
          || !findeAenderungsmarkierungen(true).isEmpty();
    }

    private void scrollToStepnumber() {
        EditorI editor = Specman.instance();

        WrappedDocument doc = getWrappedDocument();
        WrappedElement element = doc.getCharacterElement(getCaretPosition());

        String stepnumberLinkID = getStepnumberLinkIDFromElement(element.getStartOffset(), element.getEndOffset());

        if (StepnumberLink.isStepnumberLinkDefect(stepnumberLinkID)) {
            JOptionPane.showMessageDialog(this,
                    "Der Schritt, auf den der SchrittnummerLink verwiesen hat, existiert nicht mehr.",
                    "Springen nicht möglich", JOptionPane.ERROR_MESSAGE);
            return;
        }
        AbstractSchrittView step = editor.findStepByStepID(stepnumberLinkID);
        step.scrollTo();
    }

    /**
     * Marks a StepnumberLink as defect.
     */
    public void markStepnumberLinkAsDefect(String id) {
        updateStepnumberLink(id,id + StepnumberLink.STEPNUMBER_DEFECT_MARK);
    }

    public void updateStepnumberLink(String oldID, String newID) {
        for (WrappedElement e : getWrappedDocument().getRootElements()) {
            if (replaceStepnumberLink(e, oldID, newID)) {
                return;
            }
        }
        throw new RuntimeException("Could not find old StepnumberLink " + oldID + " in TextArea '" + getPlainText() + "'."
                + " This indicates a missing unregisterStepnumberLink() call.");
    }

    private boolean replaceStepnumberLink(WrappedElement e, String oldID, String newID) {
        WrappedDocument doc = getWrappedDocument();
        if (stepnumberLinkNormalOrChangedStyleSet(e)) {
            String stepnumberLinkID = getStepnumberLinkIDFromElement(e);
            if (stepnumberLinkID.equals(oldID)) {
                CompoundUndoManager.beginCompoundEdit(doc.getCore());

                AttributeSet previousAttribute = doc.getCharacterElement(e.getStartOffset()).getAttributes();
                doc.remove(e.getStartOffset(), e.getEndOffset().distance(e.getStartOffset()));
                doc.insertString(e.getStartOffset(), newID, previousAttribute);

                CompoundUndoManager.endCompoundEdit(doc.getCore());
                return true;
            }
        }
        for (int i = 0; i < e.getElementCount(); i++) {
            if (replaceStepnumberLink(e.getElement(i), oldID, newID)) {
                return true;
            }
        }
        return false;
    }

    public List<WrappedElement> findStepnumberLinks() {
        List<WrappedElement> stepnumberLinks = new ArrayList<>();
        for (WrappedElement e : getWrappedDocument().getRootElements()) {
            stepnumberLinks.addAll(findStepnumberLinks(e));
        }
        return stepnumberLinks;
    }

    private List<WrappedElement> findStepnumberLinks(WrappedElement e) {
        List<WrappedElement> stepnumberLinks = new ArrayList<>();

        if (stepnumberLinkNormalOrChangedStyleSet(e)) {
            stepnumberLinks.add(e);
        }
        for (int i = 0; i < e.getElementCount(); i++) {
            stepnumberLinks.addAll(findStepnumberLinks(e.getElement(i)));
        }
        return stepnumberLinks;
    }

    public void findStepnumberLinkIDs(HashMap<TextEditArea, List<String>> stepnumberLinkMap) {
        List<String> stepnumberLinkIDs = findStepnumberLinks()
            .stream()
            .map(this::getStepnumberLinkIDFromElement)
            .collect(Collectors.toList());
        if (!stepnumberLinkIDs.isEmpty()) {
            stepnumberLinkMap.put(this, stepnumberLinkIDs);
        }
    }

    private String getStepnumberLinkIDFromElement(WrappedElement element) {
        return getStepnumberLinkIDFromElement(element.getStartOffset(), element.getEndOffset());
    }

    private String getStepnumberLinkIDFromElement(WrappedPosition startOffset, WrappedPosition endOffset) {
        return getWrappedDocument().getText(startOffset, endOffset.distance(startOffset));
    }

    @Override public void setEditBackgroundUDBL(Color bg) {
        setBackgroundUDBL(bg);
    }

    @Override
    public void setEditDecorationIndentions(Indentions indentions) {
        Border border = new EmptyBorder(
          indentions.topBorder(),
          indentions.leftBorder(),
          indentions.bottomBorder(),
          indentions.rightBorder());
        setBorder(border);
    }

    @Override
    public boolean enthaelt(InteractiveStepFragment fragment) { return this == fragment; }

    @Override public Aenderungsart getAenderungsart() { return aenderungsart; }
    @Override public void setAenderungsart(Aenderungsart aenderungsart) { this.aenderungsart = aenderungsart; }
    private void setAenderungsartUDBL(Aenderungsart aenderungsart) { UDBL.setAenderungsart(this, aenderungsart); }

    public Shape getShape() {
        return new Shape(this).withText(new FormattedShapeText(this));
    }

    public int getLength() { return getDocument().getLength(); }

    public Integer getFirstLineHeight() {
        try {
            return (int)modelToView2D(1).getHeight();
        }
        catch(BadLocationException blc) {
            return null;
        }
    }

    private WrappedElement currentParagraphElement() {
        return getWrappedDocument().getParagraphElement(getCaretPosition());
    }

    public String getTextRX(int offs, int len) {
        try {
            return getText(offs, len);
        }
        catch(BadLocationException blx) {
            throw new RuntimeException(blx);
        }
    }

    public boolean newlineAt(int pos) {
        return getTextRX(pos, 1).startsWith("\n");
    }

    public WrappedDocument getWrappedDocument() {
        return new WrappedDocument((StyledDocument) getDocument());
    }
}