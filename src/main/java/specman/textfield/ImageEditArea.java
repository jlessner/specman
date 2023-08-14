package specman.textfield;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.io.FilenameUtils;
import specman.Aenderungsart;
import specman.Specman;
import specman.model.v001.AbstractEditAreaModel_V001;
import specman.model.v001.ImageEditAreaModel_V001;

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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static specman.textfield.TextStyles.AENDERUNGSMARKIERUNG_FARBE;
import static specman.textfield.TextStyles.AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE;
import static specman.textfield.TextStyles.Hintergrundfarbe_Standard;

public class ImageEditArea extends JPanel implements EditArea, FocusListener, MouseListener, KeyListener {
  static final Color FOCUS_BORDER_COLOR = Color.GRAY;
  static final Color FOCUS_AND_DELETED_GLASS_COLOR = new Color(100, 100, 100, 80);
  private static final int BORDER_THICKNESS = 1;
  private static final Border SELECTED_BORDER = new CompoundBorder(
    new EmptyBorder(new Insets(BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS)),
    new LineBorder(FOCUS_BORDER_COLOR, BORDER_THICKNESS));
  private static final Border UNSELECTED_BORDER =
    new EmptyBorder(new Insets(BORDER_THICKNESS*2, BORDER_THICKNESS*2, BORDER_THICKNESS*2, BORDER_THICKNESS*2));
  private static final Border UNSELECTED_CHANGED_BORDER = new LineBorder(AENDERUNGSMARKIERUNG_FARBE, BORDER_THICKNESS*2);

  private BufferedImage fullSizeImage;
  private String imageType;
  private ImageIcon scaledIcon;
  private JLabel image;
  private JPanel focusGlass;
  private Aenderungsart aenderungsart;
  public final java.util.List<ImageGrabber> grabbers = new ArrayList<>();

  ImageEditArea(File imageFile, Aenderungsart aenderungsart) {
    try {
      this.fullSizeImage = ImageIO.read(imageFile);
      this.imageType = FilenameUtils.getExtension(imageFile.getName());
      this.aenderungsart = aenderungsart;
      postInit();
    }
    catch(IOException iox) {
      throw new RuntimeException(iox);
    }
  }

  public ImageEditArea(ImageEditAreaModel_V001 imageEditAreaModel) {
    try {
      InputStream input = new ByteArrayInputStream(imageEditAreaModel.imageData);
      this.fullSizeImage = ImageIO.read(input);;
      this.imageType = imageEditAreaModel.imageType;
      this.aenderungsart = imageEditAreaModel.aenderungsart;
      postInit();
    }
    catch(IOException iox) {
      throw new RuntimeException(iox);
    }
  }

  @Override
  /** Override avoids color change by parent. As a difference to {@link TextEditArea}s,
   * the background depends solely depends on the change type of the image itself. */
  public void setBackground(Color bg) {
    super.setBackground(aenderungsart == Aenderungsart.Hinzugefuegt || aenderungsart == Aenderungsart.Geloescht
      ? AENDERUNGSMARKIERUNG_HINTERGRUNDFARBE : Hintergrundfarbe_Standard);
  }

  private void postInit() {
    setLayout(new FormLayout("fill:8px,pref:grow,fill:8px", "fill:8px,fill:pref:grow,fill:8px"));
    setBorderByChangetype();
    setBackground(null);
    this.image = new JLabel();
    add(image, CC.xywh(1, 1, 3, 3));
    updateListeners();
  }

  @Override
  public void addSchrittnummer(SchrittNummerLabel schrittNummer) {
    add(schrittNummer);
  }

  private void updateListeners() {
    if (aenderungsart != Aenderungsart.Geloescht) {
      addMouseListener(this);
      addFocusListener(this);
      addKeyListener(this);
    }
    else {
      removeMouseListener(this);
      removeFocusListener(this);
      removeKeyListener(this);
    }
  }

  @Override public void keyTyped(KeyEvent e) {}
  @Override public void keyReleased(KeyEvent e) {}
  @Override public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
      if (aenderungsart == null && Specman.instance().aenderungenVerfolgen()) {
        markAsDeleted();
      }
      else {
        getParent().removeImage(ImageEditArea.this);
        Specman.instance().diagrammAktualisieren(null);
      }
      e.consume();
    }
  }

  @Override public void mouseClicked(MouseEvent e) { requestFocus(); }
  @Override public void mousePressed(MouseEvent e) {}
  @Override public void mouseReleased(MouseEvent e) {}
  @Override public void mouseEntered(MouseEvent e) {}
  @Override public void mouseExited(MouseEvent e) {}

  @Override
  public void focusGained(FocusEvent e) {
    if (aenderungsart != Aenderungsart.Geloescht) {
      setBorder(SELECTED_BORDER);
      new ImageGrabber(this, 1, 1);
      new ImageGrabber(this, 1, 3);
      new ImageGrabber(this, 3, 1);
      new ImageGrabber(this, 3, 3);
      addGlassPanel();
      revalidate(); // Force the grabbers to appear
    }
  }

  @Override
  public void focusLost(FocusEvent e) {
    if (aenderungsart != Aenderungsart.Geloescht) {
      setBorderByChangetype();
      removeGrabbers();
      removeGlassPanel();
    }
  }

  private void removeGrabbers() {
    grabbers.forEach(g -> remove(g));
    grabbers.clear();
  }

  private void removeGlassPanel() {
    if (focusGlass != null) {
      remove(focusGlass);
      focusGlass = null;
    }
  }

  private void addGlassPanel() {
    if (focusGlass == null) {
      focusGlass = new JPanel();
      focusGlass.setBackground(FOCUS_AND_DELETED_GLASS_COLOR);
      add(focusGlass, CC.xywh(1, 1, 3, 3));
      // Removing and re-attaching the image causes it to be drawn *below* the focus glass
      remove(image);
      add(image, CC.xywh(1, 1, 3, 3));
    }
  }

  private void setBorderByChangetype() {
    setBorder(aenderungsart == Aenderungsart.Hinzugefuegt ? UNSELECTED_CHANGED_BORDER : UNSELECTED_BORDER);
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
    updateChangetypeAndDependentStyling(Aenderungsart.Geloescht);
    removeGrabbers();
    // TODDO JL: Durchstreichung
  }

  private void updateChangetypeAndDependentStyling(Aenderungsart aenderungsart) {
    this.aenderungsart = aenderungsart;
    setBorderByChangetype();
    setBackground(null);
    if (aenderungsart == Aenderungsart.Geloescht) {
      addGlassPanel();
    }
    else {
      removeGlassPanel();
    }
    updateListeners();
  }

  @Override
  public Component asComponent() { return this; }

  @Override
  public AbstractEditAreaModel_V001 toModel(boolean formatierterText) {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ImageIO.write(fullSizeImage, imageType, bytes);
      // TODO JL: Änderungsmarkierung
      return new ImageEditAreaModel_V001(bytes.toByteArray(), imageType, aenderungsart);
    }
    catch (IOException iox) {
      throw new RuntimeException(iox);
    }
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
  public void aenderungsmarkierungenUebernehmen() {
    if (aenderungsart != null) {
      switch (aenderungsart) {
        case Hinzugefuegt -> updateChangetypeAndDependentStyling(null);
        case Geloescht -> getParent().removeImage(this);
      }
    }
  }

  @Override
  public void aenderungsmarkierungenVerwerfen() {
    if (aenderungsart != null) {
      switch(aenderungsart) {
        case Hinzugefuegt -> getParent().removeImage(this);
        case Geloescht -> updateChangetypeAndDependentStyling(null);
      }
    }
  }

  @Override
  public String getText() { return "image"; }

  @Override
  public TextEditArea asTextArea() { return null; }
}
