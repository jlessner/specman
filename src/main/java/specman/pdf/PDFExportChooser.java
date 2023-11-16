package specman.pdf;

import com.itextpdf.kernel.geom.PageSize;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

public class PDFExportChooser extends JFileChooser {
  JPanel pageOptions = new JPanel();
  JComboBox<String> pageSize = new JComboBox<>();
  JRadioButton portrait = new JRadioButton("Portrait");
  JRadioButton landscape = new JRadioButton("Landscape");
  JCheckBox paging = new JCheckBox("Paging");

  @Override
  protected JDialog createDialog(Component parent) throws HeadlessException {
    JDialog dialog = super.createDialog(parent);
    addPageOptions(dialog);
    dialog.pack();
    dialog.setLocationRelativeTo(parent);
    return dialog;
  }

  private void addPageOptions(JDialog dialog) {
    pageOptions = new JPanel();
    pageOptions.setLayout(new FormLayout("10px,78px,10px,fill:245px,10px", "10px, fill:28px, 28px, 28px, 10px"));
    pageOptions.add(new JLabel("Page size:"), CC.xy(2, 2));
    pageOptions.add(pageSize, CC.xy(4, 2));
    pageOptions.add(portrait, CC.xy(2, 3));
    pageOptions.add(landscape, CC.xy(4, 3));
    pageOptions.add(paging, CC.xy(2, 4));
    addAvailablePageSizes();
    ButtonGroup orientiations = new ButtonGroup();
    orientiations.add(portrait);
    orientiations.add(landscape);
    portrait.setSelected(true);
    Container contentPane = dialog.getContentPane();
    contentPane.add(pageOptions, BorderLayout.NORTH);
  }

  private void addAvailablePageSizes() {
    for (Field field : PageSize.class.getFields()) {
      if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
        pageSize.addItem(field.getName());
      }
    }
    pageSize.setSelectedItem("A4");
  }

  public PageSize getSelectedPageSize() {
    try {
      Field sizeField = PageSize.class.getField(pageSize.getSelectedItem().toString());
      return (PageSize)sizeField.get(null);
    }
    catch(Exception x) {
      x.printStackTrace();
    }
    return null;
  }

  public boolean getPaging() { return paging.isSelected(); }
}