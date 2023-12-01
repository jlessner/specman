package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import specman.editarea.EditContainer;
import specman.pdf.LineShape;

import javax.swing.*;

import java.awt.*;
import java.util.Arrays;

import static specman.view.AbstractSchrittView.LINIENBREITE;
import static specman.view.AbstractSchrittView.ZEILENLAYOUT_INHALT_SICHTBAR;
import static specman.view.AbstractSchrittView.umgehungLayout;

public class CatchUeberschrift extends JPanel {
  final EditContainer ueberschrift;
  final FormLayout layout;

  public CatchUeberschrift(EditContainer ueberschrift) {
    this.ueberschrift = ueberschrift;
    this.setBackground(ueberschrift.getBackground());
    layout = new FormLayout(umgehungLayout() + ", 10px:grow", ZEILENLAYOUT_INHALT_SICHTBAR);
    setLayout(layout);
    add(ueberschrift, CC.xy(2, 1));
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    dreieckZeichnen((Graphics2D)g);
  }

  private void dreieckZeichnen(Graphics2D g) {
    g.setStroke(new BasicStroke(1));
    g.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    for (LineShape line: buildTriangle()) {
      g.drawLine(line.start().x, line.start().y, line.end().x, line.end().y);
    }
  }

  private java.util.List<LineShape> buildTriangle() {
    int hoehe = getHeight();
    int dreieckSpitzeY = hoehe / 2;
    int dreieckBasisX = ueberschrift.getX() - LINIENBREITE;
    return Arrays.asList(
      new LineShape(0,  0,  dreieckBasisX,  dreieckSpitzeY),
      new LineShape(dreieckBasisX,  dreieckSpitzeY, 0, hoehe));
  }

  public void skalieren(int prozentNeu, int prozentAktuell) {
    layout.setColumnSpec(1, ColumnSpec.decode(umgehungLayout()));
  }
}
