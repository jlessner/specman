package specman.textfield;

import specman.EditorI;
import specman.Specman;
import specman.model.v001.Aenderungsmarkierung_V001;
import specman.model.v001.GeloeschtMarkierung_V001;
import specman.model.v001.TextMitAenderungsmarkierungen_V001;
import specman.view.AbstractSchrittView;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.CSS;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import static specman.textfield.TextStyles.INDIKATOR_GELB;
import static specman.textfield.TextStyles.INDIKATOR_GELOESCHT_MARKIERT;
import static specman.textfield.TextStyles.INDIKATOR_GRAU;
import static specman.textfield.TextStyles.INDIKATOR_SCHWARZ;
import static specman.textfield.TextStyles.font;
import static specman.textfield.TextStyles.geaendertStil;
import static specman.textfield.TextStyles.geloeschtStil;
import static specman.textfield.TextStyles.standardStil;

public class TextEditArea extends JEditorPane implements KeyListener {
  public TextEditArea(EditorI editor, String initialerText) {
    editor.instrumentWysEditor(this, initialerText, 0);
    putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    setFont(font);
    addKeyListener(this);
  }

  public void setStyle(MutableAttributeSet attr) {
    StyledDocument doc = (StyledDocument) getDocument();
    doc.setCharacterAttributes(0, getPlainText().length(), attr, false);
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
    Object currentFontColorValue  = inputAttributes.getAttribute(CSS.Attribute.COLOR);
    if (currentTextDecoration != null && currentTextDecoration.toString().equals(INDIKATOR_GELOESCHT_MARKIERT)
      && currentFontColorValue!=null &&currentFontColorValue.toString().equals(INDIKATOR_GRAU))
      return false && currentFontColorValue!=null && currentFontColorValue.toString().equals(INDIKATOR_GRAU);
    Object currentBackgroundColorValue = inputAttributes.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
    return currentBackgroundColorValue != null
      && currentBackgroundColorValue.toString().equalsIgnoreCase(INDIKATOR_SCHWARZ)
      && currentTextDecoration != null
      && currentTextDecoration.toString().equalsIgnoreCase(INDIKATOR_GELOESCHT_MARKIERT)
      && currentFontColorValue != null && currentFontColorValue.toString().equalsIgnoreCase(INDIKATOR_GRAU);
  }

  public void standardStilSetzenWennNochNichtVorhanden() {
    if (!ganzerSchrittGeloeschtStilGesetzt()) {
      StyledEditorKit k = (StyledEditorKit) getEditorKit();
      MutableAttributeSet inputAttributes = k.getInputAttributes();
      inputAttributes.addAttributes(standardStil);
    }
  }

  public void setPlainText(String plainText, int orientation) {
    switch (orientation) {
      case StyleConstants.ALIGN_CENTER:
        setText("<div align='center'>" + plainText + "</div>");
        break;
      case StyleConstants.ALIGN_RIGHT:
        setText("<div align='right'>" + plainText + "</div>");
        break;
      default:
        setText(plainText);
    }
  }

  public TextMitAenderungsmarkierungen_V001 getTextMitAenderungsmarkierungen(boolean formatierterText) {
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
    return new TextMitAenderungsmarkierungen_V001(text, aenderungen);
  }

  public java.util.List<Aenderungsmarkierung_V001> findeAenderungsmarkierungen(boolean nurErste) {
    java.util.List<Aenderungsmarkierung_V001> ergebnis = new ArrayList<Aenderungsmarkierung_V001>();
    StyledDocument doc = (StyledDocument)getDocument();
    for (Element e: doc.getRootElements()) {
      findeAenderungsmarkierungen(e, ergebnis, nurErste);
      if (ergebnis.size() > 0 && nurErste)
        break;
    }
    return ergebnis;
  }

  private void findeAenderungsmarkierungen(Element e, java.util.List<Aenderungsmarkierung_V001> ergebnis, boolean nurErste) {
    if (elementHatAenderungshintergrund(e)) {
      ergebnis.add(new Aenderungsmarkierung_V001(e.getStartOffset(), e.getEndOffset()));
      if (ergebnis.size() > 0 && nurErste)
        return;
    }
    if (ergebnis.size() == 0 || !nurErste) {
      for (int i = 0; i < e.getElementCount(); i++) {
        findeAenderungsmarkierungen(e.getElement(i), ergebnis, nurErste);
        if (ergebnis.size() > 0 && nurErste)
          break;
      }
    }
  }

  private boolean elementHatAenderungshintergrund(Element e) {
    AttributeSet attr = e.getAttributes();
    Object backgroundColorValue = attr.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
    return (backgroundColorValue != null && backgroundColorValue.toString().equals(INDIKATOR_GELB));
  }

  // TODO JL: Muss mit aenderungsmarkierungenVerwerfen zusammengelegt werden
  public java.util.List<Aenderungsmarkierung_V001> aenderungsmarkierungenUebernehmen() {
    java.util.List<Aenderungsmarkierung_V001> ergebnis = new ArrayList<>();
    StyledDocument doc = (StyledDocument) getDocument();
    List<GeloeschtMarkierung_V001> loeschungen = new ArrayList<>();
    for (Element e : doc.getRootElements()) {
      aenderungsmarkierungenUebernehmen(e, ergebnis, loeschungen);
    }
    for (int i = 0; i < loeschungen.size();i++){
      GeloeschtMarkierung_V001 loeschung = loeschungen.get((loeschungen.size()) -1 - i);
      try{
        doc.remove(loeschung.getVon(), loeschung.getBis());
      } catch (Exception e){
        e.printStackTrace();
      }
    }
    return ergebnis;
  }

  // TODO JL: Muss mit aenderungsmarkierungenUebernehmen zusammengelegt werden
  public java.util.List<Aenderungsmarkierung_V001> aenderungsmarkierungenVerwerfen() {
    java.util.List<Aenderungsmarkierung_V001> ergebnis = new ArrayList<>();
    StyledDocument doc = (StyledDocument) getDocument();
    List <GeloeschtMarkierung_V001> loeschungen = new ArrayList<>();
    for (Element e : doc.getRootElements()) {
      aenderungsmarkierungenVerwerfen(e, ergebnis, loeschungen);
    }
    for (int i = 0; i < loeschungen.size();i++){
      GeloeschtMarkierung_V001 loeschung = loeschungen.get((loeschungen.size()) -1 - i);
      try{
        doc.remove(loeschung.getVon(), loeschung.getBis());
      } catch (Exception e){
        e.printStackTrace();
      }
    }
    return ergebnis;
  }

  // TODO JL: Muss mit aenderungsmarkierungenVerwerfen zusammengelegt werden
  private void aenderungsmarkierungenUebernehmen(
    Element e,
    List<Aenderungsmarkierung_V001> ergebnis,
    List <GeloeschtMarkierung_V001> loeschungen) {
    StyledDocument doc = (StyledDocument) e.getDocument();
    if (elementHatAenderungshintergrund(e)) {
      if (elementHatDurchgestrichenenText(e)){
        loeschungen.add(new GeloeschtMarkierung_V001(e.getStartOffset(), e.getEndOffset()-e.getStartOffset()));
      }
      else{
        AttributeSet attribute = e.getAttributes();
        MutableAttributeSet entfaerbt = new SimpleAttributeSet();
        entfaerbt.addAttributes(attribute);
        StyleConstants.setBackground(entfaerbt, Color.white);
        doc.setCharacterAttributes(e.getStartOffset(),e.getEndOffset()-e.getStartOffset(),entfaerbt,true);
      }
      ergebnis.add(new Aenderungsmarkierung_V001(e.getStartOffset(), e.getEndOffset()));
    }

    for (int i = 0; i < e.getElementCount(); i++) {
      aenderungsmarkierungenUebernehmen(e.getElement(i), ergebnis, loeschungen);
    }
  }

  // TODO JL: Muss mit aenderungsmarkierungenUebernehmen zusammengelegt werden
  private void aenderungsmarkierungenVerwerfen(
    Element e,
    List<Aenderungsmarkierung_V001> ergebnis,
    List <GeloeschtMarkierung_V001> loeschungen) {
    StyledDocument doc = (StyledDocument) e.getDocument();
    if (elementHatAenderungshintergrund(e)) {
      if (!elementHatDurchgestrichenenText(e)){
        loeschungen.add(new GeloeschtMarkierung_V001(e.getStartOffset(), e.getEndOffset()-e.getStartOffset()));
      }
      else{
        AttributeSet attribute = e.getAttributes();
        MutableAttributeSet entfaerbt = new SimpleAttributeSet();
        entfaerbt.addAttributes(attribute);
        StyleConstants.setBackground(entfaerbt, Color.white);
        StyleConstants.setStrikeThrough(entfaerbt, false);
        doc.setCharacterAttributes(e.getStartOffset(),e.getEndOffset()-e.getStartOffset(),entfaerbt,true);
      }
      ergebnis.add(new Aenderungsmarkierung_V001(e.getStartOffset(), e.getEndOffset()));
    }
    for (int i = 0; i < e.getElementCount(); i++) {
      aenderungsmarkierungenVerwerfen(e.getElement(i), ergebnis, loeschungen);
    }
  }

  private boolean elementHatDurchgestrichenenText (Element e){
    AttributeSet attr = e.getAttributes();
    return StyleConstants.isStrikeThrough(attr);
  }

  public boolean aenderungsStilGesetzt() {
    StyledEditorKit k = (StyledEditorKit) getEditorKit();
    MutableAttributeSet inputAttributes = k.getInputAttributes();
    Object currentTextDecoration = inputAttributes.getAttribute(CSS.Attribute.TEXT_DECORATION);
    if (currentTextDecoration != null && currentTextDecoration.toString().equals(INDIKATOR_GELOESCHT_MARKIERT))
      return false;
    Object currentBackgroundColorValue = inputAttributes.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
    return currentBackgroundColorValue != null
      && currentBackgroundColorValue.toString().equalsIgnoreCase(INDIKATOR_GELB);
  }

  public void aenderungsStilSetzenWennNochNichtVorhanden() {
    // Durch die folgende If-Abfrage verhindert man, dass die als geÃƒÂ¤ndert
    // markierten Buchstaben
    // alle einzelne Elements werden. Wenn an der aktuellen Position schon gelbe
    // Hintegrundfarbe
    // eingestellt ist, dann ÃƒÂ¤ndern wir den aktuellen Style gar nicht mehr.
    if (!aenderungsStilGesetzt()) {
      StyledEditorKit k = (StyledEditorKit) getEditorKit();
      MutableAttributeSet inputAttributes = k.getInputAttributes();
      StyleConstants.setStrikeThrough(inputAttributes, false); // Falls noch Geloescht-Stil herrschte
      inputAttributes.addAttributes(geaendertStil);
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (Specman.instance().aenderungenVerfolgen() && Specman.instance().hauptSequenz.findeSchritt(this).getText().isEditable()) {
      //Specman.instance().hauptSequenz.findeSchritt(editorPane).setAenderungsart(Aenderungsart.Bearbeitet);
      StyledDocument doc = (StyledDocument) getDocument();
      int p0 = getSelectionStart();
      int p1 = getSelectionEnd();
      if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
        if (p0 != p1) {
          // Eigentlich muss man das hier komplizierter machen. Sind nÃƒÂ¤mlich in der
          // Selektion Zeichen enthalten, die als geÃƒÂ¤ndert markiert sind, dann muss
          // man diese entfernen statt sie als gelÃƒÂ¶scht zu markieren.
          setCaretPosition(p0);
          doc.setCharacterAttributes(p0, p1 - p0, geloeschtStil, false);
        } else {
          if (aenderungsStilGesetzt())
            return;
          int caretPos = getCaretPosition();
          if (caretPos > 0) {
            setCaretPosition(caretPos - 1);
            doc.setCharacterAttributes(caretPos - 1, 1, geloeschtStil, false);
          }
        }
        e.consume();
        return;
      }
      aenderungsStilSetzenWennNochNichtVorhanden();
    }
    else {
      standardStilSetzenWennNochNichtVorhanden();
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {
    if (!Specman.instance().aenderungenVerfolgen()) {
      return;
    }
    AbstractSchrittView textOwner = Specman.instance().hauptSequenz.findeSchritt(this);
    if (textOwner != null && textOwner.getText().isEditable()) {
      StyledDocument doc = (StyledDocument) getDocument();
      int p0 = getSelectionStart();
      int p1 = getSelectionEnd();
      if (p0 != p1) {
        setCaretPosition(p0);
        doc.setCharacterAttributes(p0, p1 - p0, geloeschtStil, false);
        setSelectionStart(p1);
        // Jetzt ist am Ende der vorherigen Selektion noch der Geloescht-Stil gesetzt
        // D.h. die Durchstreichung muss noch weg fÃ¯Â¿Â½r das neue Zeichen, das grade
        // eingefÃ¯Â¿Â½gt werden soll
        StyledEditorKit k = (StyledEditorKit)getEditorKit();
        MutableAttributeSet inputAttributes = k.getInputAttributes();
        StyleConstants.setStrikeThrough(inputAttributes, false);
      }
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }

}