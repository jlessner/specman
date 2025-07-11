package specman.editarea;

import specman.Aenderungsart;
import specman.model.v001.AbstractEditAreaModel_V001;
import specman.pdf.Shape;

import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.List;

public interface EditArea extends InteractiveStepFragment {
  void addSchrittnummer(SchrittNummerLabel schrittNummer);

  int getWidth();

  void setGeloeschtMarkiertStilUDBL();

  Component asComponent();

  AbstractEditAreaModel_V001 toModel(boolean formatierterText);

  String getPlainText();

  void skalieren(int prozentNeu, int prozentAktuell);

  void addFocusListener(FocusListener focusListener);

  void requestFocus();

  void setBorder(Border editorPaneBorder);

  void setOpaque(boolean isOpaque);

  Color getBackground();

  int aenderungenUebernehmen();

  int aenderungenVerwerfen();

  TextEditArea asTextArea();

  default boolean isTextArea() { return false; }

  default boolean isOrderedListItemArea() { return false; }
  ImageEditArea asImageArea();

  void setQuellStil();

  void aenderungsmarkierungenEntfernen();

  boolean enthaeltAenderungsmarkierungen();

  void findStepnumberLinkIDs(HashMap<TextEditArea, List<String>> stepnumberLinkMap);

  Shape getShape();

  void setEditBackgroundUDBL(Color bg);

  void setEditDecorationIndentions(Indentions indentions);

  boolean enthaelt(InteractiveStepFragment fragment);

  void setAenderungsart(Aenderungsart aenderungsart);

  Aenderungsart getAenderungsart();

  EditContainer getParent();

  default boolean isListItemArea() { return false; }

  default boolean isTableEditArea() { return false; }

  default boolean isImageEditArea() { return false; }

  default AbstractListItemEditArea asListItemArea() { return null; };

}