package specman.editarea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.Objects;

public class TableEditAreaSelectionTracker implements MouseListener, MouseMotionListener {
  private static final Color SELECTION_COLOR = new Color(200, 200, 200, 150);

  private final TableEditArea editArea;
  private final JPanel tablePanel;
  private Rectangle selectionHighlight;

  public TableEditAreaSelectionTracker(TableEditArea editArea) {
    this.editArea = editArea;
    this.tablePanel = editArea.tablePanel;
    editArea.addMouseListener(this);
    editArea.addMouseMotionListener(this);
  }

  @Override
  public void mouseClicked(MouseEvent e) {}

  @Override
  public void mousePressed(MouseEvent e) {}

  @Override
  public void mouseReleased(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {
    selectionHighlight = null;
    editArea.repaint();
  }

  @Override
  public void mouseDragged(MouseEvent e) {}

  @Override
  public void mouseMoved(MouseEvent e) {
    Rectangle selectionUpdate = null;
    EditContainer rowLeader = yToTableRow(e.getPoint());
    if (rowLeader != null) {
      selectionUpdate = new Rectangle(
        tablePanel.getX(),
        tablePanel.getY() + rowLeader.getY(),
        tablePanel.getWidth(),
        rowLeader.getHeight());
    }
    EditContainer columnLeader = xToTableColumn(e.getPoint());
    if (columnLeader != null) {
      selectionUpdate = new Rectangle(
        tablePanel.getX() + columnLeader.getX(),
        tablePanel.getY(),
        columnLeader.getWidth(),
        tablePanel.getHeight());
    }
    if (!Objects.equals(selectionHighlight, selectionUpdate)) {
      selectionHighlight = selectionUpdate;
      editArea.repaint();
    }
  }

  private EditContainer xToTableColumn(Point mousePos) {
    if (mousePos.y >= tablePanel.getY()) {
      return null;
    }
    final int x = mousePos.x - tablePanel.getX();
    return editArea.cells.get(0)
      .stream()
      .filter(cell -> isAtXPosition(x, cell))
      .findFirst()
      .orElse(null);
  }

  private boolean isAtXPosition(int x, EditContainer cell) {
    return cell.getX() <= x && cell.getX() + cell.getWidth() >= x;
  }

  private EditContainer yToTableRow(Point mousePos) {
    if (mousePos.x >= tablePanel.getX()) {
      return null;
    }
    final int y = mousePos.y - tablePanel.getY();
    return editArea.cells
      .stream()
      .filter(row -> isAtYPosition(y, row))
      .map(row -> row.get(0))
      .findFirst()
      .orElse(null);
  }

  private boolean isAtYPosition(int y, List<EditContainer> row) {
    EditContainer rowLeader = row.get(0);
    return rowLeader.getY() <= y && rowLeader.getY() + rowLeader.getHeight() >= y;
  }

  public void paintSelection(Graphics g) {
    if (selectionHighlight != null) {
      g.setColor(SELECTION_COLOR);
      g.fillRect(selectionHighlight.x, selectionHighlight.y, selectionHighlight.width, selectionHighlight.height);
    }
  }
}
