package specman.textfield;

import specman.Specman;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ImageEditArea extends JLabel {
  ImageIcon fullSizeIcon;
  ImageIcon scaledIcon;

  ImageEditArea(File imageFile) {
    fullSizeIcon = new ImageIcon(imageFile.getName());
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
        setIcon(scaledIcon);
      }
    }
  }

}
