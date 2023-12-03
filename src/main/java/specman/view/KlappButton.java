package specman.view;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import specman.Specman;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

/**
 * Dieser Button dient dazu, unterstrukturierte Schritte auf und zuzuklappen.
 * Dafür würde man normalerweise von JToggleButton ableiten, aber leider hat diese Klasse
 * die Macke, dass man die Hintergrundfarbe im selektierten Zustand nicht individuell festlegen
 * kann. Die wollen wir aber als Indikator verwenden, ob ein Zusammenklappen entwaige Änderungen
 * verbirgt oder nicht. Also basteln wir uns aus einem JButton selber einen Toggle-Button.
 *
 * @author less02
 */
class KlappButton extends JButton implements ActionListener, MouseMotionListener, MouseListener {
  private static final Icon initialIcon = Specman.readImageIcon("minus");
  private static final Icon initialSelectedIcon = Specman.readImageIcon("plus");
  public static final int MINIMUM_ICON_LENGTH = initialIcon.getIconHeight() + 2; // The minimum border is 1px top + bottom each

  private final FormLayout layout;
  private final int klappzeile;
  private final KlappbarerBereichI klappbarerBereich;
  private final Container parent;
  private Color borderColor;

  public KlappButton(KlappbarerBereichI klappbarerBereich, Container parent, FormLayout layout, int klappzeile) {
    super(initialIcon);
    this.parent = parent;
    this.layout = layout;
    this.klappzeile = klappzeile;
    this.klappbarerBereich = klappbarerBereich;
    setSelectedIcon(initialSelectedIcon);
    setMargin(new Insets(0, 0, 0, 0));
    setOpaque(true);
    hintergrundfarbeVonParentUebernehmen();
    setVisible(false);
    addActionListener(this);
    addMouseListener(this);
    parent.addMouseMotionListener(this);
    parent.add(this);
    scale(Specman.instance().getZoomFactor(), 100);
  }

  public void init(boolean zugeklappt) {
    if (zugeklappt && !isSelected()) {
      doClick();
      setVisible(isSelected());
    }
  }

  /**
   * Diese etwas eigenartige Übernahme der Hintergrundfarbe stellt sicher, dass es nicht
   * zu unerwarteten Farbeffekten kommt. Ist der Parent nämlich z.B. nur weis, weil die
   * Farbe aus dem Farbschema des Look & Feels kommt, dann wird der Button auf Basis
   * dieser Farbe nicht unbedingt in gleicher Farbe angezeigt. Also bauen wir eine neue
   * Farbe aus der Übernahme der RGB-Werte. Dann klappt es auf jeden Fall
   */
  private void hintergrundfarbeVonParentUebernehmen() {
    Color backgroundColor = new Color(parent.getBackground().getRGB());
    setBackground(backgroundColor);
    borderColor = backgroundColor.darker();
  }

  @Override public void actionPerformed(ActionEvent e) {
    setSelected(!isSelected());
    if (isSelected()) {
      boolean zuklappenVerbirgtAenderungen = klappbarerBereich.enthaeltAenderungsmarkierungen();
      if (zuklappenVerbirgtAenderungen) {
        setBackground(Color.yellow);
      }
    } else {
      hintergrundfarbeVonParentUebernehmen();
    }
    refreshGeklappt();
  }

  public void refreshGeklappt() {
    String benoetigtesZeilenLayout = isSelected() ?
      AbstractSchrittView.ZEILENLAYOUT_INHALT_VERBORGEN :
      AbstractSchrittView.ZEILENLAYOUT_INHALT_SICHTBAR;
    layout.setRowSpec(klappzeile, RowSpec.decode(benoetigtesZeilenLayout));
    klappbarerBereich.geklappt(!isSelected());
  }

  @Override public void mouseDragged(MouseEvent e) {
  }

  @Override public void mouseMoved(MouseEvent e) {
    // Wenn die Sequenz zugeklappt ist, lassen wir den Aufklapp-Button dauerhaft angezeigt
    // Der unbedarfte User erkennt auf diese Weise leichter, wo er drücken muss, um den
    // Inhalt zu sehen. Wenn die Sequenz aufgeklappt ist, zeigen wir den Button nur an,
    // wenn die Maus an der richtigen Stelle steht. Sonst stören im aufgeklappten Zustand
    // die vielen Button-Icons das Erscheinungsbild des Diagramms
    if (!isSelected()) {
      boolean mausUeberKlappenButton = getBounds().contains(e.getPoint());
      setVisible(mausUeberKlappenButton);
    }
  }

  /**
   * Sorgt dafür, dass der Button auch verschwindet, wenn man ihn (und seinen Container)
   * über den oberen oder linken Rand verlässt. Dann kriegt man nämlich kein mouseMoved
   * mehr mit der Info, dass die Maus nicht mehr über dem Button steht
   */
  @Override public void mouseExited(MouseEvent e) {
    if (!isSelected()) {
      setVisible(false);
    }
  }

  @Override public void mouseClicked(MouseEvent e) {
  }

  @Override public void mousePressed(MouseEvent e) {
  }

  @Override public void mouseReleased(MouseEvent e) {
  }

  @Override public void mouseEntered(MouseEvent e) {
  }

  public void scale(int newPercentage, int currentPercentage) {
    if (newPercentage != currentPercentage) {
      // Get width & height by scaling the initial Icon length
      int targetWidth = (int) Specman.instance().scale(initialIcon.getIconWidth());
      int targetHeight = (int) Specman.instance().scale(initialIcon.getIconHeight());

      // Use the initial icon to prevent bad image quality through upscaling (e.g. 50% -> 100%)
      // Also no need for scaling when returning to the initial Icon
      if (targetWidth == initialIcon.getIconWidth() && targetHeight == initialIcon.getIconHeight()) {
        setIcon(initialIcon);
        setSelectedIcon(initialSelectedIcon);
      } else {
        setIcon(resizeImage(initialIcon, targetWidth, targetHeight));
        setSelectedIcon(resizeImage(initialSelectedIcon, targetWidth, targetHeight));
      }
    }
  }

  /**
   * Transforms an Icon into a BufferedImage to be able to scale the image to the desired dimensions.
   * Afterwards transforms it back for further usage.
   */
  private Icon resizeImage(Icon icon, int targetWidth, int targetHeight) {
    BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

    Graphics graphic = bufferedImage.createGraphics();
    icon.paintIcon(null, graphic, 0, 0);
    graphic.dispose();

    Image resultingImage = bufferedImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
    BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
    outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
    return new ImageIcon(outputImage);
  }

  public void updateLocation(Rectangle stepnumberBounds) {
    updateLocation(stepnumberBounds.x);
  }

  public void updateLocation(int remainingWidth) {
    if (remainingWidth > 0) {
      int desiredSize = (int) Specman.instance().scale(MINIMUM_ICON_LENGTH);
      setBounds(remainingWidth - desiredSize, 0, desiredSize, desiredSize);
      int borderSize = (int) Math.round(desiredSize * 0.1);
      setBorder(new MatteBorder(borderSize, borderSize, borderSize, borderSize, borderColor));
    }
  }

}