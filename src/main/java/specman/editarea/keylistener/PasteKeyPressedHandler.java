package specman.editarea.keylistener;

import specman.editarea.TextEditArea;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

class PasteKeyPressedHandler extends AbstractKeyEventHandler {
  PasteKeyPressedHandler(TextEditArea textArea, KeyEvent keyEvent) {
    super(textArea, keyEvent);
  }

  void handle() {
    try {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable contents = clipboard.getContents(null);
      if (contents != null) {
        // If we got string content on the clipboard, force it to become plain text for the JEditorPane
        // paste operation. There are text sources like Microsoft Word which cause a complete mess
        // in the resulting HTML otherwise.
        if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          String stringOnly = (String)contents.getTransferData(DataFlavor.stringFlavor);
          contents = new StringSelection(stringOnly);
          clipboard.setContents(contents, null);
        }
        if (contents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
          BufferedImage image = (BufferedImage) contents.getTransferData(DataFlavor.imageFlavor);
          textArea.addImage(image);
        }
      }
    }
    catch(Exception x) {
      x.printStackTrace();
    }
  }

}
