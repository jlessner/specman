package experiments.editorkit;

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.List;

public class FontsizeBox extends JComboBox<Fontsize> implements EditorPaneListener, ItemListener {
  public static Fontsize DEFAULT_FONTSIZE = new Fontsize(14, "Default");
  private final HTMLEditComponentsProvider provider;
  private final MutableAttributeSet fontStyle = new SimpleAttributeSet();
  private static final List<Fontsize> AVAILABLE_SIZES = Arrays.asList(
    new Fontsize(8, "XXS"),
    new Fontsize(10, "XS"),
    new Fontsize(11, "S"),
    DEFAULT_FONTSIZE,
    new Fontsize(16, "L"),
    new Fontsize(24, "XL"),
    new Fontsize(32, "XXL")
  );

  public FontsizeBox(HTMLEditComponentsProvider provider) {
    this.provider = provider;
    setFocusable(false);
    addItemListener(this);
    provider.addEditorPaneListener(this);
    initReadSizes();
    AVAILABLE_SIZES.forEach(s -> addItem(s));
  }

  private void initReadSizes() {
    FontsizeScaleTestPane testPane = new FontsizeScaleTestPane();
    for (int i = 0; i < AVAILABLE_SIZES.size(); i++) {
      AVAILABLE_SIZES.get(i).setReadSize(testPane.write2readFontsize(i + 1));
    }
  }

  @Override
  public void editorUpdated(TextSelection selection) {
    setEnabled(selection != null);
    if (selection != null) {
      int sizeAtSelection = StyleConstants.getFontSize(selection.getStyle());
      Fontsize fontSize = AVAILABLE_SIZES.stream().filter(s -> s.getReadSize() == sizeAtSelection).findFirst().orElse(null);
      setSelectedItem(fontSize != null ? fontSize : DEFAULT_FONTSIZE);
    }
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    TextSelection selection = provider.getCurrentTextSelection();
    if (selection != null) {
      Fontsize selectedSize = (Fontsize)getSelectedItem();
      StyleConstants.setFontSize(fontStyle, selectedSize.getSize());
      selection.applyStyle(fontStyle);
    }
  }

}
