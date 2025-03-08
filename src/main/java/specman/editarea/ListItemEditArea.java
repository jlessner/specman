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
import specman.pdf.CircleShape;
import specman.pdf.FormattedShapeText;
import specman.undo.manager.UndoRecording;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.List;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static specman.editarea.TextStyles.DIAGRAMM_LINE_COLOR;

public class ListItemEditArea extends JPanel implements EditArea {
  static final int DEFAULT_PROMPT_SPACE = 20;
  static final int DEFAULT_PROMPT_RADIUS = 3;
  private EditContainer content;
  private Aenderungsart aenderungsart;
  private JPanel itemPrompt;
  private int promptRadius;
  private int promptSpace;
  private FormLayout layout;

  public ListItemEditArea(TextEditArea initialContent, Aenderungsart aenderungsart) {
    this.aenderungsart = aenderungsart;
    this.content = new EditContainer(Specman.instance(), initialContent, null);
    initLayout();
  }

  public ListItemEditArea(ListItemEditAreaModel_V001 model) {
    this.aenderungsart = model.aenderungsart;
    this.content = new EditContainer(Specman.instance(), model.content, null);
    initLayout();
  }

  private void initLayout() {
    this.setBackground(aenderungsart.toBackgroundColor());
    this.promptRadius = DEFAULT_PROMPT_RADIUS;
    this.promptSpace = DEFAULT_PROMPT_SPACE;
    this.itemPrompt = new JPanel() {
      @Override
      public void paint(Graphics g) {
        super.paint(g);
        drawPrompt((Graphics2D)g);
      }
    };
    this.itemPrompt.setOpaque(true);
    itemPrompt.setBackground(aenderungsart.toBackgroundColor());

    layout = new FormLayout(promptSpace + "px, default:grow", "fill:pref:grow");
    setLayout(layout);
    add(itemPrompt, CC.xy(1, 1));
    add(content, CC.xy(2, 1));
  }

  private void drawPrompt(Graphics2D g) {
    Point prompCenter = promptCenter();
    Shape circle = new Ellipse2D.Double(prompCenter.x - promptRadius, prompCenter.y - promptRadius, 2.0 * promptRadius, 2.0 * promptRadius);
    g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
    g.setColor(DIAGRAMM_LINE_COLOR);
    g.fill(circle);
  }

  private Point promptCenter() {
    Integer firstLineHeight = content.getFirstLineHeight();
    if (firstLineHeight == null) {
      firstLineHeight = promptSpace;
    }
    return new Point(
    promptSpace / 2 + DEFAULT_PROMPT_RADIUS,
      firstLineHeight / 2 + DEFAULT_PROMPT_RADIUS
    );
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
    EditorContentModel_V001 contentModel = content.editorContent2Model(formatierterText);
    return new ListItemEditAreaModel_V001(contentModel, aenderungsart);
  }

  @Override
  public String getPlainText() { return content.getPlainText(); }

  @Override
  public void skalieren(int prozentNeu, int prozentAktuell) {
    content.skalieren(prozentNeu, prozentAktuell);
    promptRadius = DEFAULT_PROMPT_RADIUS * prozentNeu / 100;
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
  public specman.pdf.Shape getShape() {
    return new specman.pdf.Shape(this)
      .add(new CircleShape(promptCenter(), promptRadius))
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

  public void split(TextEditArea initiatingEditArea) {
    EditorI editor = Specman.instance();
    try (UndoRecording ur = editor.composeUndo()) {
      int initiatingCarretPosition = initiatingEditArea.getCaretPosition();
      TextEditArea splitTextEditArea = initiatingEditArea.split(initiatingCarretPosition);
      if (splitTextEditArea == null) {
        splitTextEditArea = new TextEditArea(new TextEditAreaModel_V001(""), initiatingEditArea.getFont());
      }
      ListItemEditArea splitListItemEditArea = new ListItemEditArea(splitTextEditArea, aenderungsart);
      int splitAreaIndex = content.indexOf(initiatingEditArea);
      List<EditArea> removedAreas = content.removeEditAreaComponents(splitAreaIndex + 1);
      splitListItemEditArea.addEditAreas(removedAreas);
      this.getParent().addListItem(this, splitListItemEditArea);
      editor.diagrammAktualisieren(null);
      splitTextEditArea.requestFocus();
    }
  }

  private void addEditAreas(List<EditArea> areas) {
    content.addEditAreas(areas);
  }

}
