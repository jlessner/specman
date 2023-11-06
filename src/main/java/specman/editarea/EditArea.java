package specman.editarea;

import specman.model.v001.AbstractEditAreaModel_V001;
import specman.pdf.Shape;

import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusListener;
import java.util.List;

public interface EditArea extends InteractiveStepFragment {
  void addSchrittnummer(SchrittNummerLabel schrittNummer);

  int getWidth();

  void pack(int availableWidth);

  void markAsDeleted();

  Component asComponent();

  AbstractEditAreaModel_V001 toModel(boolean formatierterText);

  String getPlainText();

  void skalieren(int prozentNeu, int prozentAktuell);

  void addFocusListener(FocusListener focusListener);

  void requestFocus();

  void setBorder(Border editorPaneBorder);

  void setOpaque(boolean isOpaque);

  void setBackground(Color bg);

  Color getBackground();

  int aenderungsmarkierungenUebernehmen();

  int aenderungsmarkierungenVerwerfen();

  TextEditArea asTextArea();

  boolean isTextArea();

  ImageEditArea asImageArea();

  void setQuellStil();

  void setStandardStil();

  boolean enthaeltAenderungsmarkierungen();

  List<String> findStepnumberLinkIDs();

  public Shape getShape();
}