package specman.pdf;

import com.itextpdf.kernel.geom.PageSize;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import specman.Specman;
import specman.model.v001.PDFExportOptionsModel_V001;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.prefs.Preferences;

public class PDFExportChooser extends JFileChooser {
  public static final String PDF_EXTENSION = ".pdf";
  public static final String PDF_PAGE_PORTRAIT_PREF = "pdf.page.portrait";
  public static final String PDF_PAGE_SIZE_PREF = "pdf.page.size";
  public static final String PDF_PAGING_PREF = "pdf.paging";
  public static final String PDF_DISPLAY_PREF = "pdf.display";

  JPanel pageOptions = new JPanel();
  JComboBox<String> pageSize = new JComboBox<>();
  JRadioButton portrait = new JRadioButton("Portrait");
  JRadioButton landscape = new JRadioButton("Landscape");
  JCheckBox paging = new JCheckBox("Paging");
  JCheckBox display = new JCheckBox("Display result");

  public PDFExportChooser() {
    setFileSelectionMode(JFileChooser.FILES_ONLY);
    addChoosableFileFilter(new FileNameExtensionFilter(PDF_EXTENSION, "pdf"));
    setAcceptAllFileFilterUsed(true);
    initPageOptions();
  }

  @Override
  protected JDialog createDialog(Component parent) throws HeadlessException {
    JDialog dialog = super.createDialog(parent);
    Container contentPane = dialog.getContentPane();
    contentPane.add(pageOptions, BorderLayout.NORTH);
    dialog.pack();
    dialog.setLocation(parent.getLocationOnScreen());
    return dialog;
  }

  private void initPageOptions() {
    Preferences prefs = Preferences.userNodeForPackage(Specman.class);

    pageOptions = new JPanel();
    pageOptions.setLayout(new FormLayout("10px,78px,10px,fill:245px,10px", "10px, fill:28px, 28px, 28px, 10px"));
    pageOptions.add(new JLabel("Page size:"), CC.xy(2, 2));
    pageOptions.add(pageSize, CC.xy(4, 2));
    pageOptions.add(portrait, CC.xy(2, 3));
    pageOptions.add(landscape, CC.xy(4, 3));
    pageOptions.add(paging, CC.xy(2, 4));
    pageOptions.add(display, CC.xy(4, 4));
    addAvailablePageSizes(prefs);
    ButtonGroup orientiations = new ButtonGroup();
    orientiations.add(portrait);
    orientiations.add(landscape);
    setOrientation(prefs.getBoolean(PDF_PAGE_PORTRAIT_PREF, true));
    paging.setSelected(prefs.getBoolean(PDF_PAGING_PREF, false));
    display.setSelected(prefs.getBoolean(PDF_DISPLAY_PREF, true));
  }

  public void initFromModel(PDFExportOptionsModel_V001 pdfExportOptions) {
    if (pdfExportOptions != null) {
      setOrientation(pdfExportOptions.portrait);
      paging.setSelected(pdfExportOptions.paging);
      pageSize.setSelectedItem(pdfExportOptions.pageSize);
      setSelectedFile(new File(pdfExportOptions.filename));
    }
  }

  private void setOrientation(boolean portraitOrientation) {
    if (portraitOrientation) {
      portrait.setSelected(true);
    }
    else {
      landscape.setSelected(true);
    }
  }

  private void addAvailablePageSizes(Preferences prefs) {
    for (Field field : PageSize.class.getFields()) {
      if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
        pageSize.addItem(field.getName());
      }
    }
    pageSize.setSelectedItem(prefs.get(PDF_PAGE_SIZE_PREF, "A4"));
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

  public boolean isPortrait() { return portrait.isSelected(); }

  public boolean displayResult() { return display.isSelected(); }

  public void safeUserPreferences() {
    Preferences prefs = Preferences.userNodeForPackage(Specman.class);
    prefs.put(PDF_PAGING_PREF, Boolean.toString(paging.isSelected()));
    prefs.put(PDF_PAGE_SIZE_PREF, pageSize.getSelectedItem().toString());
    prefs.put(PDF_PAGE_PORTRAIT_PREF, Boolean.toString(portrait.isSelected()));
    prefs.put(PDF_DISPLAY_PREF, Boolean.toString(display.isSelected()));
  }

  public int showSaveDialog(Component component, File diagrammDatei) {
    File exportDirectory = diagrammDatei != null
      ? diagrammDatei.getParentFile() : new File(".");
    setCurrentDirectory(exportDirectory);
    return showSaveDialog(component);
  }

  public PDFExportOptionsModel_V001 getExportOptions() {
    return new PDFExportOptionsModel_V001(
      getSelectedFile().getName(),
      pageSize.getSelectedItem().toString(),
      portrait.isSelected(),
      paging.isSelected()
    );
  }
}