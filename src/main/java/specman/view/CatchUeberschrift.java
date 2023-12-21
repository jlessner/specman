package specman.view;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import specman.SchrittID;
import specman.editarea.EditContainer;
import specman.pdf.LineShape;
import specman.pdf.Shape;
import specman.undo.props.UDBL;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Arrays;

import static specman.editarea.TextStyles.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE;
import static specman.editarea.TextStyles.BACKGROUND_COLOR_STANDARD;
import static specman.view.AbstractSchrittView.LINIENBREITE;
import static specman.view.AbstractSchrittView.ZEILENLAYOUT_INHALT_SICHTBAR;
import static specman.view.AbstractSchrittView.umgehungLayout;

public class CatchUeberschrift extends JPanel implements ComponentListener {
  final EditContainer ueberschrift;
  final FormLayout layout;

  public CatchUeberschrift(EditContainer ueberschrift) {
    this.ueberschrift = ueberschrift;
    this.setBackground(ueberschrift.getBackground());
    layout = new FormLayout(umgehungLayout() + ", 10px:grow", ZEILENLAYOUT_INHALT_SICHTBAR);
    setLayout(layout);
    add(ueberschrift, CC.xy(2, 1));
    addComponentListener(this);
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

  public int aenderungenUebernehmen() {
    return ueberschrift.aenderungenUebernehmen();
  }

  public void aenderungsmarkierungenEntfernen(SchrittID id) {
    ueberschrift.aenderungsmarkierungenEntfernen(id);
    setBackground(BACKGROUND_COLOR_STANDARD);
  }

  public int aenderungenVerwerfen() { return ueberschrift.aenderungenVerwerfen(); }

  public void alsGeloeschtMarkierenUDBL(SchrittID id) {
    ueberschrift.setGeloeschtMarkiertStilUDBL(id);
    UDBL.setBackgroundUDBL(this, AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE);
  }

  @Override public void componentResized(ComponentEvent e) { ueberschrift.updateBounds(); }
  @Override public void componentMoved(ComponentEvent e) {}
  @Override public void componentShown(ComponentEvent e) {}
  @Override public void componentHidden(ComponentEvent e) {}

  public Shape getShape() {
    return new Shape(this)
      .add(ueberschrift.getShape())
      .add(buildTriangle());
  }
}
