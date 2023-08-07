package specman.textfield;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import specman.Specman;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import static specman.textfield.TextStyles.Hintergrundfarbe_Standard;

public class ImageEditArea extends JPanel implements FocusListener {
  static final Color FOCUS_COLOR = Color.GRAY;
  private static final int BORDER_THICKNESS = 1;
  private static final Border SELECTED_BORDER = new CompoundBorder(
    new EmptyBorder(new Insets(BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS)),
    new LineBorder(FOCUS_COLOR, BORDER_THICKNESS));
  private static final Border UNSELECTED_BORDER =
    new EmptyBorder(new Insets(BORDER_THICKNESS*2, BORDER_THICKNESS*2, BORDER_THICKNESS*2, BORDER_THICKNESS*2));
  private ImageIcon fullSizeIcon;
  private ImageIcon scaledIcon;
  private JLabel image;
  java.util.List<ImageGrabber> grabbers = new ArrayList<>();

  ImageEditArea(File imageFile) {
    setLayout(new FormLayout("fill:8px,pref:grow,fill:8px", "fill:8px,fill:pref:grow,fill:8px"));
    setBackground(Hintergrundfarbe_Standard);
    setBorder(UNSELECTED_BORDER);
    fullSizeIcon = new ImageIcon(imageFile.getName());
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
  public void focusGained(FocusEvent e) {
    setBorder(SELECTED_BORDER);
    new ImageGrabber(this, 1, 1);
    new ImageGrabber(this, 1, 3);
    new ImageGrabber(this, 3, 1);
    new ImageGrabber(this, 3, 3);
    revalidate(); // Force the grabbers to appear
  }

  @Override
  public void focusLost(FocusEvent e) {
    setBorder(UNSELECTED_BORDER);
    grabbers.forEach(g -> remove(g));
    grabbers.clear();
  }

  public void rescale(int availableWidth) {
    if (availableWidth > 0) {
      int maximumZoomedWidth = fullSizeIcon.getIconWidth() * Specman.instance().getZoomFactor() / 100;
      int scaledWidth = Math.min(availableWidth, maximumZoomedWidth);
      if (scaledIcon == null || scaledWidth != scaledIcon.getIconWidth()) {
        float scalePercent = (float)scaledWidth / (float)fullSizeIcon.getIconWidth();
        scaledIcon = new ImageIcon(fullSizeIcon.getImage()
          .getScaledInstance((int)(fullSizeIcon.getIconWidth() * scalePercent),
            (int)(fullSizeIcon.getIconHeight() * scalePercent), Image.SCALE_SMOOTH));
        image.setIcon(scaledIcon);
      }
    }
  }

  @Override
  public TextfieldShef getParent() { return (TextfieldShef) super.getParent(); }

}
