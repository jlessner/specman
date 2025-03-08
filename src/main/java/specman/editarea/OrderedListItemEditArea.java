package specman.editarea;

import specman.Aenderungsart;
import specman.Specman;
import specman.model.v001.ListItemEditAreaModel_V001;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static specman.editarea.TextStyles.DIAGRAMM_LINE_COLOR;

public class OrderedListItemEditArea extends AbstractListItemEditArea {
  private static final int DEFAULT_RIGHT_GAP = 3;
  private int rightGap;

  public OrderedListItemEditArea(TextEditArea initialContent, Aenderungsart aenderungsart) {
    super(initialContent, aenderungsart);
    initRightGap();
  }

  private void initRightGap() {
    rightGap = DEFAULT_RIGHT_GAP * Specman.instance().getZoomFactor() / 100;
  }

  public OrderedListItemEditArea(ListItemEditAreaModel_V001 model) {
    super(model);
  }

  @Override
  protected void drawPrompt(Graphics2D g) {
    Font font = content.getFont();
    int baseline = content.getBaseline();
    FontMetrics metrics = getFontMetrics(font);
    String itemNumberString = Integer.toString(getParent().getItemNumber(this));
    g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
    g.setFont(content.getFont());
    g.setColor(DIAGRAMM_LINE_COLOR);
    Rectangle2D rect = metrics.getStringBounds(itemNumberString, g);
    g.drawString(itemNumberString, promptSpace - (int)rect.getWidth() - rightGap, baseline);
  }

  @Override
  public void skalieren(int prozentNeu, int prozentAktuell) {
    super.skalieren(prozentNeu, prozentAktuell);
    initRightGap();
  }

  @Override
  public specman.pdf.Shape getShape() {
    return super.getShape();
    // TODO JL: Prompt hinzufügen
  }

  @Override
  protected AbstractListItemEditArea createSplittedItem(TextEditArea splitTextEditArea) {
    return new OrderedListItemEditArea(splitTextEditArea, aenderungsart);
  }

  @Override
  protected boolean ordered() { return true; }

  @Override
  public boolean isOrderedListItemArea() { return true; }
}
