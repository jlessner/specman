package specman.textfield;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import specman.Specman;
import specman.model.v001.Aenderungsmarkierung_V001;
import specman.model.v001.EditArea_V001;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.MutableAttributeSet;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static specman.textfield.TextStyles.Hintergrundfarbe_Standard;
import static specman.textfield.TextStyles.ganzerSchrittGeloeschtStil;

public class ImageEditArea extends JPanel implements EditArea, FocusListener {
  static final Color FOCUS_COLOR = Color.GRAY;
  private static final int BORDER_THICKNESS = 1;
  private static final Border SELECTED_BORDER = new CompoundBorder(
    new EmptyBorder(new Insets(BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS)),
    new LineBorder(FOCUS_COLOR, BORDER_THICKNESS));
  private static final Border UNSELECTED_BORDER =
    new EmptyBorder(new Insets(BORDER_THICKNESS*2, BORDER_THICKNESS*2, BORDER_THICKNESS*2, BORDER_THICKNESS*2));
  private BufferedImage fullSizeImage;
  private ImageIcon scaledIcon;
  private JLabel image;
  java.util.List<ImageGrabber> grabbers = new ArrayList<>();
  private JPanel focusGlass;

  ImageEditArea(File imageFile) {
    setLayout(new FormLayout("fill:8px,pref:grow,fill:8px", "fill:8px,fill:pref:grow,fill:8px"));
    setBackground(Hintergrundfarbe_Standard);
    setBorder(UNSELECTED_BORDER);
    try {
      fullSizeImage = ImageIO.read(imageFile);
    }
    catch(IOException iox) {
      throw new RuntimeException(iox);
    }
    image = new JLabel();
    add(image, CC.xywh(1, 1, 3, 3));
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) { requestFocus(); }
    });
    addFocusListener(this);
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
          getParent().removeImage(ImageEditArea.this);
          e.consume();
          Specman.instance().diagrammAktualisieren(null);
        }
      }
    });
  }

  @Override
  public void addSchrittnummer(SchrittNummerLabel schrittNummer) {
    add(schrittNummer);
  }

  @Override
  public void focusGained(FocusEvent e) {
    setBorder(SELECTED_BORDER);
    new ImageGrabber(this, 1, 1);
    new ImageGrabber(this, 1, 3);
    new ImageGrabber(this, 3, 1);
    new ImageGrabber(this, 3, 3);
    focusGlass = new JPanel();
    focusGlass.setBackground(new Color(100, 100, 100, 10));
    add(focusGlass, CC.xywh(1, 1, 3, 3));
    revalidate(); // Force the grabbers to appear
  }

  @Override
  public void focusLost(FocusEvent e) {
    setBorder(UNSELECTED_BORDER);
    grabbers.forEach(g -> remove(g));
    grabbers.clear();
    remove(focusGlass);
    focusGlass = null;
  }

  public void pack(int availableWidth) {
    if (availableWidth > 0) {
      int maximumZoomedWidth = fullSizeImage.getWidth() * Specman.instance().getZoomFactor() / 100;
      int scaledWidth = Math.min(availableWidth, maximumZoomedWidth);
      if (scaledIcon == null || scaledWidth != scaledIcon.getIconWidth()) {
        float scalePercent = (float)scaledWidth / (float)fullSizeImage.getWidth();
        scaledIcon = new ImageIcon(fullSizeImage
          .getScaledInstance((int)(fullSizeImage.getWidth() * scalePercent),
            (int)(fullSizeImage.getHeight() * scalePercent), Image.SCALE_SMOOTH));
        image.setIcon(scaledIcon);
      }
    }
  }

  @Override
  public TextfieldShef getParent() { return (TextfieldShef) super.getParent(); }

  @Override
  public void setStyle(MutableAttributeSet style) {
    // TODO JL: Änderungsmarkierung noch nicht dargestellt
  }

  @Override
  public void markAsDeleted() {
    // TODDO JL: Änderungsmarkierung entfernen
    // TODO JL: als gelöscht stylen
    // TODO JL: nicht mehr editierbar
  }

  @Override
  public Component asComponent() { return this; }

  @Override
  public EditArea_V001 toModel(boolean formatierterText) {
    // TODO JL: Bild serialisieren
    return null;
  }

  @Override
  public String getPlainText() { return ""; }

  @Override
  public void skalieren(int prozentNeu, int prozentAktuell) {
    // Nothing to do: image is automacically resized by the pack() methode if necessary
  }

  @Override
  public void setEditable(boolean editable) {
    // TODO JL: Editierbarkeit kann man noch nicht umschalten
  }

  @Override
  public java.util.List<Aenderungsmarkierung_V001> aenderungsmarkierungenUebernehmen() {
    // TODO JL: Änderungsmarkierung noch nicht dargestellt
    return null;
  }

  @Override
  public java.util.List<Aenderungsmarkierung_V001> aenderungsmarkierungenVerwerfen() {
    // TODO JL: Änderungsmarkierung noch nicht dargestellt
    return null;
  }

  @Override
  public String getText() { return "image"; }
}
