package specman.editarea;

import specman.Aenderungsart;
import specman.Specman;
import specman.model.v001.ListItemEditAreaModel_V001;
import specman.pdf.LabelShapeText;
import specman.pdf.Shape;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static specman.editarea.TextStyles.DIAGRAMM_LINE_COLOR;

public class OrderedListItemEditArea extends AbstractListItemEditArea {
  private static final int DEFAULT_RIGHT_GAP = 3;
  private int rightGap;
  private Rectangle2D lastLabelRect;

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
    lastLabelRect = metrics.getStringBounds(itemNumberString, g);
    g.drawString(itemNumberString, promptSpace - (int)lastLabelRect.getWidth() - rightGap, baseline);
  }

  @Override
  public void skalieren(int prozentNeu, int prozentAktuell) {
    super.skalieren(prozentNeu, prozentAktuell);
    initRightGap();
  }

  @Override
  public specman.pdf.Shape getShape() {
    int topMargin = content.getTopMargin();
    String itemNumberString = Integer.toString(getParent().getItemNumber(this));

    System.out.println(lastLabelRect);
    return super.getShape()
      .add(new Shape(promptSpace - (int)lastLabelRect.getWidth() - rightGap, topMargin)
        .withText(new LabelShapeText(itemNumberString, new Insets(0, 0, 0, 0), DIAGRAMM_LINE_COLOR, content.getFont()))
      );
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
