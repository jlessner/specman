package specman.editarea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.Objects;

import static specman.view.AbstractSchrittView.LINIENBREITE;

public class TableEditAreaSelectionTracker implements MouseListener, MouseMotionListener {
  private static final Color SELECTION_COLOR = new Color(200, 200, 200, 150);
  private static final int SELEECTION_GAP_SIZE = 4 * LINIENBREITE;

  public enum Operation {
    AddColumn, AddRow, DeleteColumn, DeleteRow, DeleteTable;
  }

  private final TableEditArea editArea;
  private final JPanel tablePanel;
  private Rectangle selectionHighlight;
  private Integer selectionIndex;
  private Operation selectionOperation;

  public TableEditAreaSelectionTracker(TableEditArea editArea) {
    this.editArea = editArea;
    this.tablePanel = editArea.tablePanel;
    editArea.addMouseListener(this);
    editArea.addMouseMotionListener(this);
  }

  @Override public void mousePressed(MouseEvent e) {}
  @Override public void mouseReleased(MouseEvent e) {}
  @Override public void mouseEntered(MouseEvent e) {}
  @Override public void mouseDragged(MouseEvent e) {}

  @Override public void mouseClicked(MouseEvent e) {
    System.out.println(selectionOperation + " " + selectionIndex);
  }

  @Override
  public void mouseExited(MouseEvent e) {
    selectionHighlight = null;
    editArea.repaint();
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    Point mousePos = e.getPoint();
    Rectangle selectionUpdate = null;
    selectionUpdate = wholeTableSelected(mousePos);
    if (selectionUpdate == null) {
      selectionUpdate = yToTableRowGabSelection(mousePos);
    }
    if (selectionUpdate == null) {
      selectionUpdate = xToTableColumnGapSelection(mousePos);
    }
    if (selectionUpdate == null) {
      selectionUpdate = yToTableRowSelection(mousePos);
    }
    if (selectionUpdate == null) {
      selectionUpdate = xToTableColumnSelection(mousePos);
    }
    if (!Objects.equals(selectionHighlight, selectionUpdate)) {
      selectionHighlight = selectionUpdate;
      editArea.repaint();
    }
  }

  private Rectangle wholeTableSelected(Point mousePos) {
    if (mousePos.y < tablePanel.getY() && mousePos.x < tablePanel.getX()) {
      selectionOperation = Operation.DeleteTable;
      return tablePanel.getBounds();
    }
    return null;
  }

  private Rectangle xToTableColumnGapSelection(Point mousePos) {
    if (mousePos.y < tablePanel.getY()) {
      final int x = mousePos.x - tablePanel.getX();
      List<EditContainer> leadingRow = editArea.cells.get(0);
      EditContainer columnLeader = null;
      for (int c = 0; c < leadingRow.size(); c++) {
        columnLeader = leadingRow.get(c);
        Rectangle selection = xToTableColumnGapSelection(x, columnLeader.getX() - LINIENBREITE, columnLeader);
        if (selection != null) {
          selectionIndex = c;
          return selection;
        }
      }

      Rectangle selection = xToTableColumnGapSelection(x, columnLeader.getX() + columnLeader.getWidth(), columnLeader);
      if (selection != null) {
        selectionIndex = leadingRow.size();
      }
      return selection;
    }
    return null;
  }

  private Rectangle xToTableColumnGapSelection(int x, int xGapLeft, EditContainer cell) {
    if (x >= xGapLeft && x <= xGapLeft + SELEECTION_GAP_SIZE) {
      selectionOperation = Operation.AddColumn;
      return new Rectangle(xGapLeft + tablePanel.getX(), tablePanel.getY(), SELEECTION_GAP_SIZE, tablePanel.getHeight());
    }
    return null;
  }

  private Rectangle yToTableRowGabSelection(Point mousePos) {
    if (mousePos.x < tablePanel.getX()) {
      final int y = mousePos.y - tablePanel.getY();
      EditContainer rowLeader = null;
      for (int r = 0; r < editArea.cells.size(); r++) {
        rowLeader = editArea.cells.get(r).get(0);
        Rectangle selection = yToTableRowGapSelection(y, rowLeader.getY() - LINIENBREITE, rowLeader);
        if (selection != null) {
          selectionIndex = r;
          return selection;
        }
      }

      Rectangle selection = yToTableRowGapSelection(y, rowLeader.getY() + rowLeader.getHeight(), rowLeader);
      if (selection != null) {
        selectionIndex = editArea.cells.size();
      }
      return selection;
    }
    return null;
  }

  private Rectangle yToTableRowGapSelection(int y, int yGapTop, EditContainer rowLeader) {
    if (y >= yGapTop && y <= yGapTop + SELEECTION_GAP_SIZE) {
      selectionOperation = Operation.AddRow;
      return new Rectangle(tablePanel.getX(), yGapTop + tablePanel.getY(), tablePanel.getWidth(), SELEECTION_GAP_SIZE);
    }
    return null;
  }

  private Rectangle xToTableColumnSelection(Point mousePos) {
    if (mousePos.y < tablePanel.getY()) {
      final int x = mousePos.x - tablePanel.getX();
      List<EditContainer> leadingRow = editArea.cells.get(0);
      for (int c = 0; c < leadingRow.size(); c++) {
        EditContainer columnLeader = leadingRow.get(c);
        if (isAtXPosition(x, columnLeader)) {
          selectionIndex = c;
          selectionOperation = Operation.DeleteColumn;
          return new Rectangle(
            tablePanel.getX() + columnLeader.getX(),
            tablePanel.getY(),
            columnLeader.getWidth(),
            tablePanel.getHeight());
        }
      }
    }
    selectionIndex = null;
    return null;
  }

  private boolean isAtXPosition(int x, EditContainer cell) {
    return cell.getX() <= x && cell.getX() + cell.getWidth() >= x;
  }

  private Rectangle yToTableRowSelection(Point mousePos) {
    if (mousePos.x < tablePanel.getX()) {
      final int y = mousePos.y - tablePanel.getY();
      for (int r = 0; r < editArea.cells.size(); r++) {
        EditContainer rowLeader = editArea.cells.get(r).get(0);
        if (isAtYPosition(y, rowLeader)) {
          selectionIndex = r;
          selectionOperation = Operation.DeleteRow;
          return new Rectangle(
            tablePanel.getX(),
            tablePanel.getY() + rowLeader.getY(),
            tablePanel.getWidth(),
            rowLeader.getHeight());
        }
      }
    }
    return null;
  }

  private boolean isAtYPosition(int y, EditContainer rowLeader) {
    return rowLeader.getY() <= y && rowLeader.getY() + rowLeader.getHeight() >= y;
  }

  public void paintSelection(Graphics g) {
    if (selectionHighlight != null) {
      g.setColor(SELECTION_COLOR);
      g.fillRect(selectionHighlight.x, selectionHighlight.y, selectionHighlight.width, selectionHighlight.height);
    }
  }
}
