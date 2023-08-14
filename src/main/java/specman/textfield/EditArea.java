package specman.textfield;

import specman.model.v001.Aenderungsmarkierung_V001;
import specman.model.v001.AbstractEditAreaModel_V001;

import javax.swing.border.Border;
import javax.swing.text.MutableAttributeSet;
import java.awt.*;
import java.awt.event.FocusListener;

public interface EditArea extends InteractiveStepFragment {
  void addSchrittnummer(SchrittNummerLabel schrittNummer);

  int getWidth();

  void pack(int availableWidth);

  void setStyle(MutableAttributeSet style);

  void markAsDeleted();

  Component asComponent();

  AbstractEditAreaModel_V001 toModel(boolean formatierterText);

  String getPlainText();

  void skalieren(int prozentNeu, int prozentAktuell);

  void setEditable(boolean editable);

  void addFocusListener(FocusListener focusListener);

  void requestFocus();

  void setBorder(Border editorPaneBorder);

  void setOpaque(boolean isOpaque);

  void setBackground(Color bg);

  Color getBackground();

  void aenderungsmarkierungenUebernehmen();

  void aenderungsmarkierungenVerwerfen();

  TextEditArea asTextArea();
}
