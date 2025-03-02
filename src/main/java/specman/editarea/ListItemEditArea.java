package specman.editarea;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import specman.Aenderungsart;
import specman.Specman;
import specman.model.v001.AbstractEditAreaModel_V001;
import specman.pdf.Shape;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.List;

public class ListItemEditArea extends JPanel implements EditArea {
  private EditContainer content;
  private Aenderungsart aenderungsart;
  private JLabel itemNumber;

  public ListItemEditArea(TextEditArea initialContent, Aenderungsart aenderungsart) {
    this.aenderungsart = aenderungsart;
    this.content = new EditContainer(Specman.instance(), initialContent, null);
    this.setBackground(aenderungsart.toBackgroundColor());
    this.itemNumber = new JLabel("o");
    this.itemNumber.setOpaque(true);
    itemNumber.setBackground(aenderungsart.toBackgroundColor());
    initLayout();
  }

  private void initLayout() {
    FormLayout layout = new FormLayout("10px, 10px, default:grow", "fill:pref:grow");
    setLayout(layout);
    add(itemNumber, CC.xy(1, 1));
    add(content, CC.xy(3, 1));
  }

  @Override
  public void addSchrittnummer(SchrittNummerLabel schrittNummer) {
  }

  /** Nothing to do here in text areas */
  @Override public void pack(int availableWidth) {}

  @Override
  public void setGeloeschtMarkiertStilUDBL() {
    content.setGeloeschtMarkiertStilUDBL(null);
  }

  @Override
  public Component asComponent() { return this; }

  @Override
  public AbstractEditAreaModel_V001 toModel(boolean formatierterText) {
    return null;
  }

  @Override
  public String getPlainText() { return content.getPlainText(); }

  @Override
  public void skalieren(int prozentNeu, int prozentAktuell) {

  }

  @Override
  public int aenderungenUebernehmen() {
    return 0;
  }

  @Override
  public int aenderungenVerwerfen() {
    return 0;
  }

  @Override
  public TextEditArea asTextArea() {
    return null;
  }

  @Override
  public boolean isTextArea() {
    return false;
  }

  @Override
  public ImageEditArea asImageArea() {
    return null;
  }

  @Override
  public void setQuellStil() {

  }

  @Override
  public void aenderungsmarkierungenEntfernen() {

  }

  @Override
  public boolean enthaeltAenderungsmarkierungen() {
    return false;
  }

  @Override
  public void findStepnumberLinkIDs(HashMap<TextEditArea, List<String>> stepnumberLinkMap) {

  }

  @Override
  public Shape getShape() {
    return null;
  }

  @Override
  public void setEditBackgroundUDBL(Color bg) {

  }

  @Override
  public void setEditDecorationIndentions(Indentions indentions) {

  }

  @Override
  public boolean enthaelt(InteractiveStepFragment fragment) {
    return false;
  }

  @Override
  public void setAenderungsart(Aenderungsart aenderungsart) {

  }

  @Override
  public Aenderungsart getAenderungsart() {
    return null;
  }

  @Override
  public String getText() {
    return null;
  }

  @Override
  public synchronized void addFocusListener(FocusListener l) {
    content.addEditAreasFocusListener(l);
  }

  @Override
  public synchronized void addComponentListener(ComponentListener l) {
    content.addEditComponentListener(l);
  }

}
