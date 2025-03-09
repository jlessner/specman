package specman.editarea;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import specman.Aenderungsart;
import specman.EditorI;
import specman.Specman;
import specman.model.v001.AbstractEditAreaModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.ListItemEditAreaModel_V001;
import specman.model.v001.TextEditAreaModel_V001;
import specman.undo.UndoableListItemSplitted;
import specman.undo.manager.UndoRecording;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.List;

abstract public class AbstractListItemEditArea extends JPanel implements EditArea {
  static final int DEFAULT_PROMPT_SPACE = 20;
  protected EditContainer content;
  protected Aenderungsart aenderungsart;
  protected int promptSpace;
  protected JPanel itemPrompt;
  protected FormLayout layout;

  public AbstractListItemEditArea(TextEditArea initialContent, Aenderungsart aenderungsart) {
    this.aenderungsart = aenderungsart;
    this.content = new EditContainer(Specman.instance(), initialContent, null);
    initLayout();
  }

  public AbstractListItemEditArea(ListItemEditAreaModel_V001 model) {
    this.aenderungsart = model.aenderungsart;
    this.content = new EditContainer(Specman.instance(), model.content, null);
    initLayout();
  }

  protected void initLayout() {
    this.setBackground(aenderungsart.toBackgroundColor());
    this.promptSpace = DEFAULT_PROMPT_SPACE * Specman.instance().getZoomFactor() / 100;

    layout = new FormLayout(promptSpace + "px, default:grow", "fill:pref:grow");
    setLayout(layout);
    add(content, CC.xy(2, 1));

    this.itemPrompt = new JPanel() {
      @Override
      public void paint(Graphics g) {
        super.paint(g);
        drawPrompt((Graphics2D)g);
      }
    };
    this.itemPrompt.setOpaque(true);
    itemPrompt.setBackground(aenderungsart.toBackgroundColor());

    add(itemPrompt, CC.xy(1, 1));
  }

  protected abstract void drawPrompt(Graphics2D g);

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
    EditorContentModel_V001 contentModel = content.editorContent2Model(formatierterText);
    return new ListItemEditAreaModel_V001(contentModel, ordered(), aenderungsart);
  }

  abstract protected boolean ordered();

  @Override
  public String getPlainText() { return content.getPlainText(); }

  @Override
  public void skalieren(int prozentNeu, int prozentAktuell) {
    content.skalieren(prozentNeu, prozentAktuell);
    promptSpace = DEFAULT_PROMPT_SPACE * prozentNeu / 100;
    layout.setColumnSpec(1, ColumnSpec.decode(promptSpace + "px"));
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
  public specman.pdf.Shape getShape() {
    return new specman.pdf.Shape(this)
      .add(content.getShape());
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
    return aenderungsart;
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

  @Override
  public EditContainer getParent() {
    return (EditContainer) super.getParent();
  }

  public EditContainer getContent() {
    return content;
  }

  @Override
  public void requestFocus() { content.requestFocus(); }

  abstract protected AbstractListItemEditArea createSplittedItem(TextEditArea splitTextEditArea);

  private void addEditAreas(List<EditArea> areas) {
    content.addEditAreas(areas);
  }

  public void split(TextEditArea initiatingEditArea) {
    EditorI editor = Specman.instance();
    try (UndoRecording ur = editor.composeUndo()) {
      int initiatingCarretPosition = initiatingEditArea.getCaretPosition();
      TextEditArea splitTextEditArea = initiatingEditArea.split(initiatingCarretPosition);
      if (splitTextEditArea == null) {
        splitTextEditArea = new TextEditArea(new TextEditAreaModel_V001(""), initiatingEditArea.getFont());
      }
      AbstractListItemEditArea splitListItemEditArea =  createSplittedItem(splitTextEditArea);
      moveEditAreas(initiatingEditArea, splitListItemEditArea);
      editor.addEdit(new UndoableListItemSplitted(this, initiatingEditArea, splitListItemEditArea));
      editor.diagrammAktualisieren(splitListItemEditArea.content.getFirstEditArea());
    }
  }

  public void moveEditAreas(TextEditArea initiatingEditArea, AbstractListItemEditArea splitListItemEditArea) {
    int splitAreaIndex = content.indexOf(initiatingEditArea);
    List<EditArea> removedAreas = content.removeEditAreaComponents(splitAreaIndex + 1);
    splitListItemEditArea.addEditAreas(removedAreas);
    this.getParent().addListItem(this, splitListItemEditArea);
  }

  @Override public boolean isListItemArea() { return true; }

  @Override public AbstractListItemEditArea asListItemArea() { return this; };

  public TextEditArea appendText(String text) {
    TextEditArea lastArea = content.getLastEditArea().asTextArea();
    if (lastArea == null) {
      lastArea = new TextEditArea(new TextEditAreaModel_V001(text), getFont());
      content.appendTextEditArea(lastArea);
    }
    else {
      lastArea.appendText(text);
    }
    return lastArea;
  }

  public EditArea getFirstEditArea() { return content.getFirstEditArea(); }

  public void removeEditAreaComponent(EditArea editArea) { content.removeEditAreaComponent(editArea); }

  public List<EditArea> removeEditAreaComponents(int fromIndex) { return content.removeEditAreaComponents(fromIndex); }

  public EditArea getLastEditArea() { return content.getLastEditArea(); }
}
