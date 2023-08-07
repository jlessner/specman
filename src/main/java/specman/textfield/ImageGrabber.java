package specman.textfield;

import com.jgoodies.forms.factories.CC;

import javax.swing.*;

/** The Grabbers are shown as small gray rectangles in each corner of a focussed {@link ImageEditArea},
 * making the focussing look familiar to the user. However, the grabbers have no function yet like
 * resizing or rotating the image. This may be an interesting feature someday. */
public class ImageGrabber extends JLabel {
  public ImageGrabber(ImageEditArea imageEditArea, int col, int row) {
    setBackground(ImageEditArea.FOCUS_COLOR);
    setOpaque(true);
    imageEditArea.add(this, CC.xy(col, row));
    imageEditArea.grabbers.add(this);
  }
}
