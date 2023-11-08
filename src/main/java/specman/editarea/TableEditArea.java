package specman.editarea;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import specman.Aenderungsart;
import specman.EditorI;
import specman.SpaltenContainerI;
import specman.SpaltenResizer;
import specman.Specman;
import specman.model.v001.AbstractEditAreaModel_V001;
import specman.model.v001.EditorContentModel_V001;
import specman.model.v001.TableEditAreaModel_V001;
import specman.pdf.Shape;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static specman.editarea.TextStyles.DIAGRAMM_LINE_COLOR;
import static specman.view.AbstractSchrittView.FORMLAYOUT_GAP;
import static specman.view.AbstractSchrittView.ZEILENLAYOUT_INHALT_SICHTBAR;

/** The edit area itself is a surrounding panel which contains the actual table panel
 * with a little space in between to all sides. This space is for interactions to remove
 * and add table columns and rows, and to remove the whole table and other things like
 * that. The table panel follows the same approach as step views do concerning basic layout:
 * the panel itself is black and the table cells are placed as children with a little
 * gap between them. This causes the black panel background to shine through the gaps,
 * creating the impression of lines. */
public class TableEditArea extends JPanel implements EditArea, SpaltenContainerI {
  private static final int BORDER_THICKNESS = 5;
  private static final String TABLELINE_GAP = FORMLAYOUT_GAP;
  private static final String TABLELAYOUT_ROWSPEC = ZEILENLAYOUT_INHALT_SICHTBAR;
  private static final String TABLELAYOUT_COLSPEC = "pref:grow";
  private static final int WHOLETABLE_COLUMN_INDICATOR = -1;

  private List<List<EditContainer>> cells = new ArrayList<>();
  private Aenderungsart aenderungsart;
  private final List<FocusListener> editAreasFocusListeners = new ArrayList<>();
  private final List<ComponentListener> editAreasComponentListeners = new ArrayList<>();
  private FormLayout tablePanelLayout;
  private JPanel tablePanel;
  private int tableWidthPercent;
  private List<Integer> columnsWidthPercent;

  public TableEditArea(int columns, int rows, Aenderungsart aenderungsart) {
    this.aenderungsart = aenderungsart;
    this.tableWidthPercent = 100;
    initPanels(columns, rows);
    addInitialCells(columns, rows);
  }

  public TableEditArea(TableEditAreaModel_V001 model) {
    this.aenderungsart = model.aenderungsart;
    this.tableWidthPercent = model.tableWidthPercent;
    this.columnsWidthPercent = model.columnsWidthPercent;
    int rows = model.cells.size();
    int columns = model.cells.get(0).size();
    initPanels(columns, rows);
    addCells(model.cells);
    setEditBackground(null);
  }

  private Stream<EditContainer> cellstream() { return cells.stream().flatMap(l -> l.stream()); }

  private void initPanels(int columns, int rows) {
    tablePanel = new JPanel();
    tablePanel.setBackground(DIAGRAMM_LINE_COLOR);
    createTablePanelLayout(columns, rows);
    createColumnResizers(columns, rows);
    refreshBorderSpaceGeometricsAndColor();
  }

  private void createColumnResizers(int columns, int rows) {
    EditorI editor = Specman.instance();
    int resizerHeight = rows * 2;
    // Resizer for the whole table
    tablePanel.add(new SpaltenResizer(this, WHOLETABLE_COLUMN_INDICATOR, editor), CC.xywh(1 + columns * 2, 1, 1, resizerHeight));

    // Resizers for all columns except the last one
    for (int c = 0; c < columns-1; c++) {
      tablePanel.add(new SpaltenResizer(this, c, editor), CC.xywh(3 + c * 2, 1, 1, resizerHeight));
    }
  }

  private int minimumBorderSize() {
    return (int)((float)BORDER_THICKNESS * (float)Specman.instance().getZoomFactor() / 100f);
  }

  private void refreshBorderSpaceGeometricsAndColor() {
    int borderThickness = minimumBorderSize();
    float tableWidthFraction = (float)tableWidthPercent / 100;
    String tableColSpec = "pref:grow(" + tableWidthFraction + ")";
    String rightGapColSpec = borderThickness + "px:grow(" + (1 - tableWidthFraction) + ")";
    String outerColumnAndRowSpec = borderThickness + "px," + tableColSpec + "," + rightGapColSpec;
    FormLayout areaLayout = new FormLayout(outerColumnAndRowSpec, outerColumnAndRowSpec);
    setLayout(areaLayout);
    setBackground(aenderungsart.toBackgroundColor());
    // Removing and re-associating the tablePanel child is required to ensure propper
    // re-layouting after changing the layout in the lines above
    remove(tablePanel);
    add(tablePanel, CC.xy(2, 2));
  }

  @Override
  public void setEditBackground(Color bg) {
    cellstream().forEach(cell -> cell.setBackground(aenderungsart.toBackgroundColor()));
    refreshBorderSpaceGeometricsAndColor();
  }

  @Override
  public synchronized void addFocusListener(FocusListener l) {
    editAreasFocusListeners.add(l);
    cellstream().forEach(cell -> cell.addEditAreasFocusListener(l));
  }

  @Override
  public synchronized void addComponentListener(ComponentListener l) {
    editAreasComponentListeners.add(l);
    cellstream().forEach(cell -> cell.addEditComponentListener(l));
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
    tablePanel.add(cell, CC.xy(2 + columnIndex * 2, 2 + rowIndex * 2));
  }

  private void addCell(int rowIndex, String content) {
    addCell(rowIndex, new EditContainer(Specman.instance(), content, null));
  }

  private void createTablePanelLayout(int columns, int rows) {
    String columnSpecs = TABLELINE_GAP;
    //columnSpecs += ",pref:grow(0.2)," + TABLELINE_GAP;
    for (int c = 0; c < columns; c++) {
      String colspec = columnsWidthPercent != null
        ? "pref:grow(" + (float)columnsWidthPercent.get(c) / 100 + ")"
        : TABLELAYOUT_COLSPEC;
      columnSpecs += "," + colspec + "," + TABLELINE_GAP;
    }
    String rowSpecs = TABLELINE_GAP;
    for (int r = 0; r < rows; r++) {
      rowSpecs += "," + TABLELAYOUT_ROWSPEC + "," + TABLELINE_GAP;
    }
    tablePanelLayout = new FormLayout(columnSpecs, rowSpecs);
    tablePanel.setLayout(tablePanelLayout);
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
    return new TableEditAreaModel_V001(cellsModel, tableWidthPercent, columnsWidthPercent, aenderungsart);
  }

  @Override
  public void skalieren(int prozentNeu, int prozentAktuell) {
    cellstream().forEach(cell -> cell.skalieren(prozentNeu, prozentAktuell));
    refreshBorderSpaceGeometricsAndColor();
  }

  @Override
  public int aenderungsmarkierungenUebernehmen() {
    return cellstream().mapToInt(cell -> cell.aenderungsmarkierungenUebernehmen()).sum();
  }

  @Override
  public int aenderungsmarkierungenVerwerfen() {
    return cellstream().mapToInt(cell -> cell.aenderungsmarkierungenVerwerfen()).sum();
  }

  @Override
  public void setStandardStil() {
    aenderungsart = Aenderungsart.Untracked;
    cellstream().forEach(cell -> cell.aenderungsmarkierungenEntfernen(null));
  }

  @Override
  public boolean enthaelt(InteractiveStepFragment fragment) {
    return cellstream().anyMatch(cell -> cell.enthaelt(fragment));
  }

  @Override
  public boolean enthaeltAenderungsmarkierungen() {
    return cellstream().anyMatch(cell -> cell.enthaeltAenderungsmarkierungen());
  }

  @Override
  public void findStepnumberLinkIDs(HashMap<TextEditArea, List<String>> stepnumberLinkMap) {
    cellstream().forEach(cell -> cell.findStepnumberLinkIDs(stepnumberLinkMap));
  }

  @Override
  public Shape getShape() {
    Shape tablePanelShape = new Shape(tablePanel);
    cellstream().forEach(cell -> tablePanelShape.add(cell.getShape()));
    return new Shape(this).add(tablePanelShape);
  }

  @Override public void setQuellStil() { /* Not required for tables - source steps only contain an empty text area */ }
  @Override public void pack(int availableWidth) { /* Nothing to do */ }
  @Override public void addSchrittnummer(SchrittNummerLabel schrittNummer) { add(schrittNummer); }
  @Override public Component asComponent() { return this; }
  @Override public String getPlainText() { return ""; }
  @Override public TextEditArea asTextArea() { return null; }
  @Override public boolean isTextArea() { return false; }
  @Override public ImageEditArea asImageArea() { return null; }
  @Override public String getText() { return "table"; }
  @Override public void setEditDecorationIndentions(Indentions indentions) { /* Nothing to do */ }

  @Override
  public int spaltenbreitenAnpassenNachMausDragging(int vergroesserung, int spalte) {
    if (vergroesserung != 0) {
      return spalte == WHOLETABLE_COLUMN_INDICATOR
        ? updateTableWidthPercentage(vergroesserung)
        : updateColumnsWidthPercentage(vergroesserung, spalte);
    }
    return vergroesserung;
  }

  /** Widens the column of intrest by the number of passed pixels (respectively
   * shrink it by a negative value. The number of pixels is translated to a difference
   * in column width percentages, so that the resulting ratio remains the same if the
   * edit area or the table width is resized later on.
   * <p>
   * The behaviour is the same as it is known from MS Word: the overall width of the
   * table remains the same and the amount of groth for the column of interest is
   * taken from the following column. We can be shure that there is a following column
   * as the resizer at the right edge of the table is used to change the whole table
   * width. */
  private int updateColumnsWidthPercentage(int vergroesserung, int spalte) {
    Integer[] columnWidths = cells.get(0)
      .stream()
      .map(editContainer -> editContainer.getWidth())
      .toArray(Integer[]::new);
    if (columnWidths[spalte] + vergroesserung < 0) {
      // Dragging too far to the left is ignored
      return 0;
    }
    if (vergroesserung > columnWidths[spalte+1]) {
      // Dragging to the right exceeding the following column's width is ignored
      return 0;
    }
    columnWidths[spalte] += vergroesserung;
    columnWidths[spalte+1] -= vergroesserung;
    int columnsWidthSum = Arrays.stream(columnWidths).mapToInt(cw -> cw).sum();
    columnsWidthPercent = Arrays.stream(columnWidths)
      .map(cw -> (int)((float)cw / columnsWidthSum * 100))
      .collect(Collectors.toList());
    createTablePanelLayout(columnWidths.length, cells.size());
    reassignCellsAndResizers(columnWidths.length, cells.size());
    revalidate();
    return vergroesserung;
  }

  private void reassignCellsAndResizers(int columns, int rows) {
    tablePanel.removeAll();
    createColumnResizers(columns, rows);
    for (int r = 0; r < rows; r++) {
      List<EditContainer> row = cells.get(r);
      for (int c = 0; c < columns; c++) {
        EditContainer cell = row.get(c);
        tablePanel.add(cell, CC.xy(2 + c * 2, 2 + r * 2));
      }
    }
  }

  private int updateTableWidthPercentage(int vergroesserung) {
    int currentTablePanelWidth = tablePanel.getWidth();
    int maximumTablePanelWidth = getWidth() - 2 * minimumBorderSize();
    if (currentTablePanelWidth + vergroesserung < 0) {
      // Dragging too far to the left is ignored
      return 0;
    }
    if (currentTablePanelWidth + vergroesserung > maximumTablePanelWidth) {
      // Dragging to the right exceeding the maximum available space is
      // interpreted as: bring the table back to 100% width
      vergroesserung = maximumTablePanelWidth - currentTablePanelWidth;
    }
    float newTablePanelWidth = currentTablePanelWidth + vergroesserung;
    tableWidthPercent = (int)(newTablePanelWidth / maximumTablePanelWidth * 100);
    refreshBorderSpaceGeometricsAndColor();
    revalidate();
    return vergroesserung;
  }
}