package specman.textfield;

import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import specman.Aenderungsart;
import specman.EditorI;
import specman.Specman;
import specman.model.v001.AbstractEditAreaModel_V001;
import specman.model.v001.Aenderungsmarkierung_V001;
import specman.model.v001.GeloeschtMarkierung_V001;
import specman.model.v001.TextEditAreaModel_V001;
import specman.undo.UndoableStepnumberLinkAdded;
import specman.undo.UndoableStepnumberLinkRemoved;
import specman.undo.manager.UndoRecording;
import specman.view.AbstractSchrittView;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.CSS;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static specman.textfield.HTMLTags.BODY_INTRO;
import static specman.textfield.HTMLTags.BODY_OUTRO;
import static specman.textfield.HTMLTags.HTML_INTRO;
import static specman.textfield.HTMLTags.HTML_OUTRO;
import static specman.textfield.TextStyles.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE;
import static specman.textfield.TextStyles.FONTSIZE;
import static specman.textfield.TextStyles.Hintergrundfarbe_Standard;
import static specman.textfield.TextStyles.INDIKATOR_GELB;
import static specman.textfield.TextStyles.INDIKATOR_GELOESCHT_MARKIERT;
import static specman.textfield.TextStyles.INDIKATOR_GRAU;
import static specman.textfield.TextStyles.INDIKATOR_SCHWARZ;
import static specman.textfield.TextStyles.changedStepnumberLinkHTMLColor;
import static specman.textfield.TextStyles.deletedStepnumberLinkStyle;
import static specman.textfield.TextStyles.font;
import static specman.textfield.TextStyles.ganzerSchrittGeloeschtStil;
import static specman.textfield.TextStyles.geaendertStil;
import static specman.textfield.TextStyles.geloeschtStil;
import static specman.textfield.TextStyles.quellschrittStil;
import static specman.textfield.TextStyles.standardStil;
import static specman.textfield.TextStyles.stepnumberLinkStyleColor;

public class TextEditArea extends JEditorPane implements EditArea, KeyListener {
    private boolean isMousePressed = false;
    private boolean alreadyScrolledDuringCurrentMouseclick = false;
    private final static String STEPNUMBER_DEFECT_MARK = "?";

    public TextEditArea(EditorI editor, String initialerText, Color initialBackground) {
        editor.instrumentWysEditor(this, initialerText, 0);
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        setFont(font);
        addKeyListener(this);
        addMouseListener();
        //addMouseMotionListener();
        setBackground(initialBackground);
    }

    private void addMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                isMousePressed = true;
                alreadyScrolledDuringCurrentMouseclick = false;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                isMousePressed = false;
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
                /*Point p = e.getPoint(); // TODO Change cursor when hovering over stepnumberlink

                List<Element> stepnumberLinks = findStepnumberLinks();
                for (Element stepnumberLink : stepnumberLinks) {
                    stepnumberLink.getDocument().getStartPosition();
                    SwingUtilities.convertPointToScreen();
                    Component f = (Component) stepnumberLink;
                    Point less = f.getLocationOnScreen();
                }*/
            }
        });
    }

    @Override
    public void addSchrittnummer(SchrittNummerLabel schrittNummer) {
        add(schrittNummer);
    }

    @Override
    public void pack(int availableWidth) {
    }

    private void setStyle(MutableAttributeSet attr, Color backgroundColor, boolean editable) {
        StyledDocument doc = (StyledDocument) getDocument();
        doc.setCharacterAttributes(0, getPlainText().length(), attr, false);
        setEditable(editable);
        setBackground(backgroundColor);
    }

    private boolean hasStyle(MutableAttributeSet attr, Color backgroundColor, boolean editable) {
        StyledDocument doc = (StyledDocument) getDocument();
        AttributeSet attributes = doc.getCharacterElement(0).getAttributes();
        return attributes.containsAttributes(attr) && isEditable() == editable && getBackground() == backgroundColor;
    }

    @Override
    public void setQuellStil() {
        setStyle(quellschrittStil, AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE, false);
    }

    @Override
    public void setStandardStil() {
        if (!hasStandardStyle()) {
            setStyle(standardStil, Hintergrundfarbe_Standard, true);
        }
    }

    private boolean hasStandardStyle() {
        return hasStyle(standardStil, Hintergrundfarbe_Standard, true);
    }

    @Override
    public void markAsDeleted() {
        aenderungsmarkierungenVerwerfen();
        setStyle(ganzerSchrittGeloeschtStil, AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE, false);
    }

    @Override
    public Component asComponent() {
        return this;
    }

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
        return new TextEditAreaModel_V001(text, aenderungen);
    }

    public java.util.List<Aenderungsmarkierung_V001> findeAenderungsmarkierungen(boolean nurErste) {
        java.util.List<Aenderungsmarkierung_V001> ergebnis = new ArrayList<>();
        StyledDocument doc = (StyledDocument) getDocument();
        for (Element e : doc.getRootElements()) {
            findeAenderungsmarkierungen(e, ergebnis, nurErste);
            if (!ergebnis.isEmpty() && nurErste) {
                break;
            }
        }
        return ergebnis;
    }

    private void findeAenderungsmarkierungen(Element e, java.util.List<Aenderungsmarkierung_V001> ergebnis, boolean nurErste) {
        if (elementHatAenderungshintergrund(e)) {
            ergebnis.add(new Aenderungsmarkierung_V001(e.getStartOffset(), e.getEndOffset()));
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
    public void aenderungsmarkierungenUebernehmen() {
        EditorI editor = Specman.instance();
        StyledDocument doc = (StyledDocument) getDocument();
        List<GeloeschtMarkierung_V001> loeschungen = new ArrayList<>();
        for (Element e : doc.getRootElements()) {
            aenderungsmarkierungenUebernehmen(e, loeschungen);
        }
        for (int i = 0; i < loeschungen.size(); i++) {
            GeloeschtMarkierung_V001 loeschung = loeschungen.get((loeschungen.size()) - 1 - i);
            try {
                removeTextAndUnregisterStepnumberLinks(loeschung.getVon(), loeschung.getBis(), editor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // TODO JL: Muss mit aenderungsmarkierungenUebernehmen zusammengelegt werden
    public void aenderungsmarkierungenVerwerfen() {
        EditorI editor = Specman.instance();
        StyledDocument doc = (StyledDocument) getDocument();
        List<GeloeschtMarkierung_V001> loeschungen = new ArrayList<>();
        for (Element e : doc.getRootElements()) {
            aenderungsmarkierungenVerwerfen(e, loeschungen);
        }
        for (int i = 0; i < loeschungen.size(); i++) {
            GeloeschtMarkierung_V001 loeschung = loeschungen.get((loeschungen.size()) - 1 - i);
            try {
                removeTextAndUnregisterStepnumberLinks(loeschung.getVon(), loeschung.getBis(), editor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // TODO JL: Muss mit aenderungsmarkierungenVerwerfen zusammengelegt werden
    private void aenderungsmarkierungenUebernehmen(
            Element e,
            List<GeloeschtMarkierung_V001> loeschungen) {
        StyledDocument doc = (StyledDocument) e.getDocument();
        if (elementHatAenderungshintergrund(e)) {
            if (elementHatDurchgestrichenenText(e)) {
                loeschungen.add(new GeloeschtMarkierung_V001(e.getStartOffset(), e.getEndOffset()));
            } else {
                AttributeSet attribute = e.getAttributes();
                MutableAttributeSet entfaerbt = new SimpleAttributeSet();
                entfaerbt.addAttributes(attribute);
                StyleConstants.setBackground(entfaerbt, stepnumberLinkChangedStyleSet(e) ? stepnumberLinkStyleColor : Hintergrundfarbe_Standard);
                doc.setCharacterAttributes(e.getStartOffset(), e.getEndOffset() - e.getStartOffset(), entfaerbt, true);
            }

        }

        for (int i = 0; i < e.getElementCount(); i++) {
            aenderungsmarkierungenUebernehmen(e.getElement(i), loeschungen);
        }
    }

    // TODO JL: Muss mit aenderungsmarkierungenUebernehmen zusammengelegt werden
    private void aenderungsmarkierungenVerwerfen(
            Element e,
            List<GeloeschtMarkierung_V001> loeschungen) {
        StyledDocument doc = (StyledDocument) e.getDocument();
        if (elementHatAenderungshintergrund(e)) {
            if (!elementHatDurchgestrichenenText(e)) {
                loeschungen.add(new GeloeschtMarkierung_V001(e.getStartOffset(), e.getEndOffset()));
            } else {
                AttributeSet attribute = e.getAttributes();
                MutableAttributeSet entfaerbt = new SimpleAttributeSet();
                entfaerbt.addAttributes(attribute);
                StyleConstants.setBackground(entfaerbt, stepnumberLinkChangedStyleSet(e) ? stepnumberLinkStyleColor : Hintergrundfarbe_Standard);
                StyleConstants.setStrikeThrough(entfaerbt, false);
                doc.setCharacterAttributes(e.getStartOffset(), e.getEndOffset() - e.getStartOffset(), entfaerbt, true);
            }

        }
        for (int i = 0; i < e.getElementCount(); i++) {
            aenderungsmarkierungenVerwerfen(e.getElement(i), loeschungen);
        }
    }

    private boolean elementHatDurchgestrichenenText() {
        StyledEditorKit k = (StyledEditorKit) getEditorKit();
        MutableAttributeSet inputAttributes = k.getInputAttributes();
        Object currentTextDecoration = inputAttributes.getAttribute(CSS.Attribute.TEXT_DECORATION);
        return currentTextDecoration != null && currentTextDecoration.toString().equals(INDIKATOR_GELOESCHT_MARKIERT);
    }

    private boolean elementHatDurchgestrichenenText(Element e) {
        AttributeSet attr = e.getAttributes();
        return StyleConstants.isStrikeThrough(attr);
    }

    private boolean elementHatAenderungshintergrund(Element e) {
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
        if (!aenderungsStilGesetzt() && !stepnumberLinkNormalStyleSet()) {
            StyledEditorKit k = (StyledEditorKit) getEditorKit();
            MutableAttributeSet inputAttributes = k.getInputAttributes();
            StyleConstants.setStrikeThrough(inputAttributes, false); // Falls noch Gelöscht-Stil herrschte
            inputAttributes.addAttributes(geaendertStil);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_BACK_SPACE -> {
                if (shouldPreventActionInsideStepnumberLink()) {
                    skipToStepnumberLinkEnd();
                    e.consume();
                    return;
                }
                if (isTrackingChanges()) {
                    markPendingRemovalAsDeleted();
                    e.consume();
                    return;
                } else if (stepnumberLinkNormalOrChangedStyleSet(getSelectionEnd() - 1)) {
                    removePreviousStepnumberLink();
                    e.consume();
                    return;
                }
            }
            case KeyEvent.VK_LEFT -> {
                if (skipToStepnumberLinkStart()) {
                    e.consume();
                    return;
                }
            }
            case KeyEvent.VK_RIGHT -> {
                if (skipToStepnumberLinkEnd()) {
                    e.consume();
                    return;
                }
            }
            case KeyEvent.VK_CONTROL -> {
                if (isMousePressed && !alreadyScrolledDuringCurrentMouseclick && stepnumberLinkNormalOrChangedStyleSet(getCaretPosition())) {
                    scrollToStepnumber();
                }
            }
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

    private void markPendingRemovalAsDeleted() {
        if (stepnumberLinkChangedStyleSet() && !elementHatDurchgestrichenenText()) {
            // Changed stepnumberlinks need to be removed instead of marked as deleted
            removePreviousStepnumberLink();
            return;
        }

        if (getSelectionStart() != getSelectionEnd()) {
            markSelectionAsDeleted();
        } else {
            markCurrentPositionAsDeleted();
        }
    }

    private void markCurrentPositionAsDeleted() {
        StyledDocument doc = (StyledDocument) getDocument();
        int position = getCaretPosition() - 1;
        if (position < 0) {
            return;
        }

        Element element = doc.getCharacterElement(position);

        if (elementIsChangedButNotMarkedAsDeleted(element)) {
            deleteText(position, 1, doc);
        } else {
            int linkStilStart = getStartOffsetFromPosition(position);
            int linkStilEnd = getEndOffsetFromPosition(position);

            if (elementHatDurchgestrichenenText(element)) { // No need to reapply deletedStyle if it's already set
                if (stepnumberLinkChangedStyleSet(position)) {
                    setCaretPosition(linkStilStart);
                } else {
                    setCaretPosition(position);
                }
            } else if (stepnumberLinkNormalStyleSet(position)) {
                int length = linkStilEnd - linkStilStart;
                markRangeAsDeleted(linkStilStart, length, deletedStepnumberLinkStyle);
            } else {
                markRangeAsDeleted(position, 1, geloeschtStil);
            }
        }
    }

    private void markRangeAsDeleted(int deleteStart, int deleteLength, MutableAttributeSet deleteStyle) {
        StyledDocument doc = (StyledDocument) getDocument();

        setCaretPosition(deleteStart);
        doc.setCharacterAttributes(deleteStart, deleteLength, deleteStyle, false);
    }

    /**
     * When deleting a range there can be the following items in it:
     * - Normal text - White Background - Gets marked as deleted
     * - Changed text - Yellow Background - Gets deleted
     * - Marked as deleted text - Yellow Background with strikethrough - No changes
     */
    private void markSelectionAsDeleted() {
        StyledDocument doc = (StyledDocument) getDocument();
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        TreeMap<Integer, Integer> positionsToDelete = new TreeMap<>();

        CompoundUndoManager.beginCompoundEdit(doc);

        for (int position = selectionStart; position < selectionEnd; ) { // The missing position++ is intended, see below
            int endPosition = Math.min(selectionEnd, getEndOffsetFromPosition(position));
            int length = endPosition - position;
            Element element = doc.getCharacterElement(position);

            if (elementIsChangedButNotMarkedAsDeleted(element)) {
                positionsToDelete.put(position, length);
            } else {
                if (stepnumberLinkNormalStyleSet(position)) {
                    markRangeAsDeleted(position, length, deletedStepnumberLinkStyle);
                } else if (!elementHatDurchgestrichenenText(element)) { // No need to reapply deletedStyle if it's already set
                    markRangeAsDeleted(position, length, geloeschtStil);
                }
            }

            position += length; // Skip already processed positions
        }

        int offset = 0;
        for (Map.Entry<Integer, Integer> positionSet : positionsToDelete.entrySet()) {
            int position = positionSet.getKey();
            int length = positionSet.getValue();
            deleteText(position - offset, length, doc);
            offset += length;
        }

        setCaretPosition(selectionEnd - offset); // Set caret at new end of selection

        CompoundUndoManager.endCompoundEdit(doc);
    }

    private boolean elementIsChangedButNotMarkedAsDeleted(Element element) {
        return (elementHatAenderungshintergrund(element) || stepnumberLinkChangedStyleSet(element))
                && !elementHatDurchgestrichenenText(element);
    }

    private void deleteText(int position, int length, StyledDocument doc) {
        try {
            doc.remove(position, length);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean shouldPreventActionInsideStepnumberLink() {
        if (stepnumberLinkNormalOrChangedStyleSet(getSelectionStart()) || stepnumberLinkNormalOrChangedStyleSet(getSelectionEnd())) {
            if (isCaretInsideSelection()) {
                return true;
            }

            for (int i = getSelectionStart(); i < getSelectionEnd(); i++) {
                if (stepnumberLinkNormalOrChangedStyleSet(i)) {
                    if (getStartOffsetFromPosition(i) < getSelectionStart() || getEndOffsetFromPosition(i) > getSelectionEnd()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isCaretInsideSelection() {
        int linkStyleStart = getStartOffsetFromPosition(getSelectionEnd());
        int linkStyleEnd = getEndOffsetFromPosition(getSelectionEnd());
        return getSelectionStart() == getSelectionEnd() && getSelectionEnd() < linkStyleEnd && getSelectionStart() > linkStyleStart;
    }

    private void removeTextAndUnregisterStepnumberLinks(int startOffset, int endOffset, EditorI editor) {
        StyledDocument doc = (StyledDocument) getDocument();

        for (int currentOffset = startOffset; currentOffset < endOffset; ) { // The missing currentOffset++ is intended
            int currentEndOffset = getEndOffsetFromPosition(currentOffset);
            int length = currentEndOffset - currentOffset;
            Element element = doc.getCharacterElement(currentOffset);

            if (stepnumberLinkNormalOrChangedStyleSet(element)) {
                String stepnumberLinkID = getStepnumberLinkIDFromElement(currentOffset, currentEndOffset);
                if (!isStepnumberLinkDefect(stepnumberLinkID)) {
                    AbstractSchrittView step = editor.findStepByStepID(stepnumberLinkID);
                    step.unregisterStepnumberLink(this);
                    editor.addEdit(new UndoableStepnumberLinkRemoved(step, this));
                }
            }

            currentOffset += length; // Skip already processed positions
        }

        deleteText(startOffset, endOffset - startOffset, doc);
    }

    private void removePreviousStepnumberLink() {
        EditorI editor = Specman.instance();
        try (UndoRecording ur = editor.composeUndo()) {
            int position = getSelectionEnd() - 1;
            int startOffset = Math.min(getSelectionStart(), getStartOffsetFromPosition(position));
            int endOffset = getEndOffsetFromPosition(position);
            removeTextAndUnregisterStepnumberLinks(startOffset, endOffset, editor);
        }
    }

    private boolean skipToStepnumberLinkStart() {
        int selectionEnd = getSelectionEnd();
        if (stepnumberLinkNormalOrChangedStyleSet(selectionEnd - 1)) {
            setCaretPosition(getStartOffsetFromPosition(selectionEnd - 1));
            return true;
        }
        return false;
    }

    private boolean skipToStepnumberLinkEnd() {
        int selectionEnd = getSelectionEnd();
        if (stepnumberLinkNormalOrChangedStyleSet(selectionEnd)) {
            setCaretPosition(getEndOffsetFromPosition(selectionEnd));
            return true;
        }
        return false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (shouldPreventActionInsideStepnumberLink()) {
            e.consume();
            return;
        }

        if (!Specman.instance().aenderungenVerfolgen()) {
            return;
        }
        AbstractSchrittView textOwner = Specman.instance().hauptSequenz.findeSchritt(this);
        if (textOwner != null && isEditable()) {
            int selectionStart = getSelectionStart();
            int selectionEnd = getSelectionEnd();

            if (selectionStart != selectionEnd) {
                if (stepnumberLinkNormalStyleSet(selectionStart)) {
                    markRangeAsDeleted(selectionStart, selectionEnd - selectionStart, deletedStepnumberLinkStyle);
                } else {
                    markRangeAsDeleted(selectionStart, selectionEnd - selectionStart, geloeschtStil);
                }

                setSelectionStart(selectionEnd);
                // Jetzt ist am Ende der vorherigen Selektion noch der Geloescht-Stil gesetzt
                // D.h. die Durchstreichung muss noch weg für das neue Zeichen, das gerade
                // eingefügt werden soll
                StyledEditorKit k = (StyledEditorKit) getEditorKit();
                MutableAttributeSet inputAttributes = k.getInputAttributes();
                StyleConstants.setStrikeThrough(inputAttributes, false);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public EditContainer getParent() {
        return (EditContainer) super.getParent();
    }

    public void addImage(File imageFile, Aenderungsart aenderungsart) {
        getParent().addImage(imageFile, this, aenderungsart);
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
        try {
            int textLength = getDocument().getLength();
            if (textLength > textPosition) {
                TextEditArea splittedArea = new TextEditArea(Specman.instance(), getText(), this.getBackground());
                getDocument().remove(textPosition, textLength - textPosition);
                splittedArea.getDocument().remove(0, textPosition);
                return splittedArea;
            }
            return null;
        } catch (BadLocationException blx) {
            throw new RuntimeException(blx);
        }
    }

    @Override
    public TextEditArea asTextArea() {
        return this;
    }

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
            StyledDocument doc = (StyledDocument) getDocument();
            int caretPos = getCaretPosition();

            // Add space between two stepnumberlinks to prevent merging them
            if (stepnumberLinkNormalOrChangedStyleSet(caretPos - 1)) {
                try {
                    doc.insertString(caretPos, " ", null);
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }
                caretPos++;
            }

            AttributeSet previousAttribute = doc.getCharacterElement(caretPos).getAttributes();
            MutableAttributeSet stepnumberAttribute = new SimpleAttributeSet(previousAttribute);
            stepnumberAttribute.addAttributes(TextStyles.stepnumberLinkStyle);
            StyleConstants.setBackground(stepnumberAttribute,
                    isTrackingChanges() ? TextStyles.changedStepnumberLinkColor : TextStyles.stepnumberLinkStyleColor);

            try {
                doc.insertString(caretPos, stepnumberText, stepnumberAttribute);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }

            referencedStep.registerStepnumberLink(this);
            editor.addEdit(new UndoableStepnumberLinkAdded(referencedStep, this));
        }
    }

    private boolean stepnumberLinkNormalStyleSet() {
        StyledEditorKit k = (StyledEditorKit) getEditorKit();
        Object backgroundColor = k.getInputAttributes().getAttribute(CSS.Attribute.BACKGROUND_COLOR);
        if (backgroundColor != null) {
            String value = backgroundColor.toString();
            return value.equals(TextStyles.stepnumberLinkStyleHTMLColor);
        }
        return false;
    }

    private boolean stepnumberLinkNormalStyleSet(int position) {
        StyledDocument doc = (StyledDocument) getDocument();
        return stepnumberLinkNormalStyleSet(doc.getCharacterElement(position));
    }

    private boolean stepnumberLinkNormalStyleSet(Element element) {
        String color = getBackgroundColorFromElement(element);
        return color != null && color.equalsIgnoreCase(TextStyles.stepnumberLinkStyleHTMLColor);
    }


    private boolean stepnumberLinkChangedStyleSet() {
        StyledEditorKit k = (StyledEditorKit) getEditorKit();
        Object backgroundColor = k.getInputAttributes().getAttribute(CSS.Attribute.BACKGROUND_COLOR);
        if (backgroundColor != null) {
            String value = backgroundColor.toString();
            return value.equals(TextStyles.changedStepnumberLinkHTMLColor);
        }
        return false;
    }

    private boolean stepnumberLinkChangedStyleSet(int position) {
        StyledDocument doc = (StyledDocument) getDocument();
        return stepnumberLinkChangedStyleSet(doc.getCharacterElement(position));
    }

    private boolean stepnumberLinkChangedStyleSet(Element element) {
        String color = getBackgroundColorFromElement(element);
        return color != null && color.equalsIgnoreCase(TextStyles.changedStepnumberLinkHTMLColor);
    }


    private boolean stepnumberLinkNormalOrChangedStyleSet(int position) {
        StyledDocument doc = (StyledDocument) getDocument();
        return stepnumberLinkNormalOrChangedStyleSet(doc.getCharacterElement(position));
    }

    private boolean stepnumberLinkNormalOrChangedStyleSet(Element element) {
        return stepnumberLinkNormalStyleSet(element) || stepnumberLinkChangedStyleSet(element);
    }


    private String getBackgroundColorFromElement(Element element) {
        Object backgroundColorValue = element.getAttributes().getAttribute(CSS.Attribute.BACKGROUND_COLOR);
        return backgroundColorValue != null ? backgroundColorValue.toString() : null;
    }

    private int getStartOffsetFromPosition(int position) {
        StyledDocument doc = (StyledDocument) getDocument();
        return doc.getCharacterElement(position).getStartOffset();
    }

    private int getEndOffsetFromPosition(int position) {
        StyledDocument doc = (StyledDocument) getDocument();
        return doc.getCharacterElement(position).getEndOffset();
    }

    private boolean isTrackingChanges() {
        return Specman.instance().aenderungenVerfolgen() && isEditable();
    }

    @Override
    public boolean enthaeltAenderungsmarkierungen() {
        return !findeAenderungsmarkierungen(true).isEmpty();
    }

    private void scrollToStepnumber() {
        EditorI editor = Specman.instance();
        alreadyScrolledDuringCurrentMouseclick = true;

        StyledDocument doc = (StyledDocument) getDocument();
        Element element = doc.getCharacterElement(getCaretPosition());

        String stepnumberLinkID = getStepnumberLinkIDFromElement(element.getStartOffset(), element.getEndOffset());

        if (isStepnumberLinkDefect(stepnumberLinkID)) {
            JOptionPane.showMessageDialog(this,
                    "Der Schritt, auf den der SchrittnummerLink verwiesen hat, existiert nicht mehr.",
                    "Springen nicht möglich", JOptionPane.ERROR_MESSAGE);
            return;
        }
        AbstractSchrittView step = editor.findStepByStepID(stepnumberLinkID);
        step.scrollTo();
    }

    public void markStepnumberLinkAsDefect(String id) {
        updateStepnumberLink(id, id + STEPNUMBER_DEFECT_MARK);
    }

    public void updateStepnumberLink(String oldID, String newID) {
        StyledDocument doc = (StyledDocument) getDocument();
        for (Element e : doc.getRootElements()) {
            if (replaceStepnumberLink(e, oldID, newID)) {
                return;
            }
        }
        throw new RuntimeException("Could not find old StepnumberLink " + oldID + " in TextArea '" + getPlainText() + "'."
                + " This indicates a missing unregisterStepnumberLink() call.");
    }

    private boolean replaceStepnumberLink(Element e, String oldID, String newID) {
        StyledDocument doc = (StyledDocument) getDocument();
        if (stepnumberLinkNormalOrChangedStyleSet(e)) {
            try {
                String stepnumberLinkID = getStepnumberLinkIDFromElement(e);
                if (stepnumberLinkID.equals(oldID)) {
                    CompoundUndoManager.beginCompoundEdit(doc);

                    AttributeSet previousAttribute = doc.getCharacterElement(e.getStartOffset()).getAttributes();
                    doc.remove(e.getStartOffset(), e.getEndOffset() - e.getStartOffset());
                    doc.insertString(e.getStartOffset(), newID, previousAttribute);

                    CompoundUndoManager.endCompoundEdit(doc);
                    return true;
                }
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
        }
        for (int i = 0; i < e.getElementCount(); i++) {
            if (replaceStepnumberLink(e.getElement(i), oldID, newID)) {
                return true;
            }
        }
        return false;
    }

    public List<Element> findStepnumberLinks() {
        StyledDocument doc = (StyledDocument) getDocument();
        List<Element> stepnumberLinks = new ArrayList<>();
        for (Element e : doc.getRootElements()) {
            stepnumberLinks.addAll(findStepnumberLinks(e));
        }

        return stepnumberLinks;
    }

    private List<Element> findStepnumberLinks(Element e) {
        List<Element> stepnumberLinks = new ArrayList<>();

        if (stepnumberLinkNormalOrChangedStyleSet(e)) {
            stepnumberLinks.add(e);
        }
        for (int i = 0; i < e.getElementCount(); i++) {
            stepnumberLinks.addAll(findStepnumberLinks(e.getElement(i)));
        }
        return stepnumberLinks;
    }

    public List<String> findStepnumberLinkIDs() {
        return findStepnumberLinks()
                .stream()
                .map(this::getStepnumberLinkIDFromElement)
                .collect(Collectors.toList());
    }

    private String getStepnumberLinkIDFromElement(Element element) {
        return getStepnumberLinkIDFromElement(element.getStartOffset(), element.getEndOffset());
    }

    private String getStepnumberLinkIDFromElement(int startOffset, int endOffset) {
        StyledDocument doc = (StyledDocument) getDocument();
        try {
            return doc.getText(startOffset, endOffset - startOffset);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isStepnumberLinkDefect(String stepnumberLinkID) {
        return stepnumberLinkID.endsWith(STEPNUMBER_DEFECT_MARK);
    }
}