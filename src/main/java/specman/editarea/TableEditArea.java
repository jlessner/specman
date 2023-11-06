package specman.editarea;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import specman.Aenderungsart;
import specman.Specman;
import specman.model.v001.AbstractEditAreaModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.TableEditAreaModel_V001;
import specman.pdf.Shape;
import specman.view.AbstractSchrittView;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static specman.editarea.TextStyles.DIAGRAMM_LINE_COLOR;
import static specman.view.AbstractSchrittView.FORMLAYOUT_GAP;

public class TableEditArea extends JPanel implements EditArea {
  private static final int BORDER_THICKNESS = 5;
  private static final String TABLELINE_GAP = FORMLAYOUT_GAP;
  private static final String TABLELAYOUT_ROWSPEC = AbstractSchrittView.ZEILENLAYOUT_INHALT_SICHTBAR;
  private static final String TABLELAYOUT_COLSPEC = "pref:grow";

  private List<List<EditContainer>> cells = new ArrayList<>();
  private FormLayout tableLayout;
  private Aenderungsart aenderungsart;
  private final List<FocusListener> editAreasFocusListeners = new ArrayList<>();
  private final List<ComponentListener> editAreasComponentListeners = new ArrayList<>();

  public TableEditArea(int columns, int rows, Aenderungsart aenderungsart) {
    this.aenderungsart = aenderungsart;
    setBackground(DIAGRAMM_LINE_COLOR);
    initBorder();
    createLayout(columns, rows);
    addInitialCells(columns, rows);
  }

  public TableEditArea(TableEditAreaModel_V001 model) {
    this.aenderungsart = model.aenderungsart;
    setBackground(DIAGRAMM_LINE_COLOR);
    int rows = model.cells.size();
    int columns = model.cells.get(0).size();
    createLayout(columns, rows);
    addCells(model.cells);
    setEditBackground(null);
  }

  private void initBorder() {
    Color borderColor = aenderungsart.toBackgroundColor();
    int borderThickness = (int)((float)BORDER_THICKNESS * (float)Specman.instance().getZoomFactor() / 100f);
    setBorder(new LineBorder(borderColor, borderThickness));
  }

  @Override
  public void setEditBackground(Color bg) {
    for (List<EditContainer> row: cells) {
      row.forEach(cell -> cell.setBackground(aenderungsart.toBackgroundColor()));
    }
    initBorder();
  }

  @Override
  public synchronized void addFocusListener(FocusListener l) {
    editAreasFocusListeners.add(l);
    for (List<EditContainer> row: cells) {
      row.forEach(cell -> cell.addEditAreasFocusListener(l));
    }
  }

  @Override
  public synchronized void addComponentListener(ComponentListener l) {
    editAreasComponentListeners.add(l);
    for (List<EditContainer> row: cells) {
      row.forEach(cell -> cell.addEditComponentListener(l));
    }
  }

  @Override
  public void setEditDecorationIndentions(Indentions indentions) {

  }

  private void addCells(List<List<EditorContentModel_V001>> model) {
    for (int r = 0; r < model.size(); r++) {
      List<EditorContentModel_V001> rowModel = model.get(r);
      cells.add(new ArrayList<>());
      for (EditorContentModel_V001 cellModel: rowModel) {
        addCell(r, new EditContainer(Specman.instance(), cellModel, null));
      }
    }
  }

  private void addInitialCells(int columns, int rows) {
    for (int r = 0; r < rows; r++) {
      cells.add(new ArrayList<>());
    }
    for (int c = 0; c < columns; c++) {
      addCell(0, "<b>Spalte " + (c+1) + "</b>");
    }
    for (int r = 1; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        addCell(r, "Zelle " + (c+1) + "." + r);
      }
    }
  }

  private void addCell(int rowIndex, EditContainer cell) {
    List<EditContainer> row = cells.get(rowIndex);
    int columnIndex = row.size();
    row.add(cell);
    add(cell, CC.xy(2 + columnIndex * 2, 2 + rowIndex * 2));
  }

  private void addCell(int rowIndex, String content) {
    addCell(rowIndex, new EditContainer(Specman.instance(), content, null));
  }

  private void createLayout(int columns, int rows) {
    String columnSpecs = TABLELINE_GAP;
    for (int i = 0; i < columns; i++) {
      columnSpecs += "," + TABLELAYOUT_COLSPEC + "," + TABLELINE_GAP;
    }
    String rowSpecs = TABLELINE_GAP;
    for (int i = 0; i < columns; i++) {
      rowSpecs += "," + TABLELAYOUT_ROWSPEC + "," + TABLELINE_GAP;
    }
    tableLayout = new FormLayout(rowSpecs, columnSpecs);
    setLayout(tableLayout);
  }

  @Override
  public void markAsDeleted() {

  }

  @Override
  public AbstractEditAreaModel_V001 toModel(boolean formatierterText) {
    List<List<EditorContentModel_V001>> cellsModel = new ArrayList<>();
    for (List<EditContainer> row: cells) {
      List<EditorContentModel_V001> rowModel = row
        .stream()
        .map(ec -> ec.editorContent2Model(formatierterText))
        .collect(Collectors.toList());
      cellsModel.add(rowModel);
    }
    return new TableEditAreaModel_V001(cellsModel, aenderungsart);
  }

  @Override
  public void skalieren(int prozentNeu, int prozentAktuell) {
    cells
      .stream()
      .forEach(row -> row.stream()
        .forEach(cell -> cell.skalieren(prozentNeu, prozentAktuell)));
    initBorder();
  }

  @Override
  public int aenderungsmarkierungenUebernehmen() {
    return 0;
  }

  @Override
  public int aenderungsmarkierungenVerwerfen() {
    return 0;
  }

  @Override public void setQuellStil() {

  }

  @Override
  public void setStandardStil() {

  }

  @Override
  public boolean enthaelt(InteractiveStepFragment fragment) {
    for (List<EditContainer> row: cells) {
      if (row.stream().anyMatch(cell -> cell.enthaelt(fragment))) {
        return true;
      };
    }
    return false;
  }

  @Override
  public boolean enthaeltAenderungsmarkierungen() {
    return false;
  }

  @Override
  public List<String> findStepnumberLinkIDs() {
    return new ArrayList<>();
  }

  @Override
  public Shape getShape() {
    return null;
  }

  @Override public void pack(int availableWidth) { /* Nothing to do */ }
  @Override public void addSchrittnummer(SchrittNummerLabel schrittNummer) { add(schrittNummer); }
  @Override public Component asComponent() { return this; }
  @Override public String getPlainText() { return ""; }
  @Override public TextEditArea asTextArea() { return null; }
  @Override public boolean isTextArea() { return false; }
  @Override public ImageEditArea asImageArea() { return null; }
  @Override public String getText() { return "table"; }

}
