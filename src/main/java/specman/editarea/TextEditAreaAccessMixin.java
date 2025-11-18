package specman.editarea;

import specman.editarea.document.WrappedDocument;
import specman.editarea.document.WrappedElement;
import specman.editarea.document.WrappedPosition;

import javax.swing.text.StyledEditorKit;

/** There is a whole lot of functionality for the class {@link TextEditArea}. To
 * keep the class itself from growing infinitely, most functionality is separated in
 * other classes which need access to the base functionality of {@link TextEditArea}.
 * The mixin here makes delegation to these functions a little bit simpler. The
 * implementing classes can just use func() rather than textEditArea.func() where
 * ever they access the edit area object. */
public interface TextEditAreaAccessMixin {
  TextEditArea textArea();

  default boolean aenderungsStilGesetzt() { return textArea().aenderungsStilGesetzt(); }
  default boolean elementHatAenderungshintergrund(WrappedElement element) { return textArea().elementHatAenderungshintergrund(element); }
  default boolean elementHatDurchgestrichenenText(WrappedElement element) { return textArea().elementHatDurchgestrichenenText(element); }
  default boolean stepnumberLinkChangedStyleSet(WrappedElement element) { return textArea().stepnumberLinkChangedStyleSet(element); }
  default String getStepnumberLinkIDFromElement(WrappedPosition start, WrappedPosition end) { return textArea().getStepnumberLinkIDFromElement(start, end); }
  default void setCaretPosition(int position) { textArea().setCaretPosition(position); }
  default boolean stepnumberLinkStyleSet(WrappedPosition i) { return textArea().stepnumberLinkStyleSet(i); }
  default boolean stepnumberLinkStyleSet(WrappedElement e) { return textArea().stepnumberLinkStyleSet(e); }
  default WrappedPosition getWrappedSelectionStart() { return textArea().getWrappedSelectionStart(); }
  default WrappedPosition getEndOffsetFromPosition(WrappedPosition position) { return textArea().getEndOffsetFromPosition(position); }
  default WrappedPosition getStartOffsetFromPosition(WrappedPosition position) { return textArea().getStartOffsetFromPosition(position); }
  default WrappedPosition getWrappedSelectionEnd() { return textArea().getWrappedSelectionEnd(); }
  default WrappedPosition getWrappedCaretPosition() { return textArea().getWrappedCaretPosition(); }
  default boolean isTrackingChanges() { return textArea().isTrackingChanges(); }
  default WrappedDocument getWrappedDocument() { return textArea().getWrappedDocument(); }
  default boolean stepnumberLinkNormalStyleSet(WrappedPosition position) { return textArea().stepnumberLinkStyleSet(position); }
  default int getSelectionEnd() { return textArea().getSelectionEnd(); }
  default int getSelectionStart() { return textArea().getSelectionStart(); }
  default void setSelectionStart(int position) { textArea().setSelectionStart(position); }
  default boolean isEditable() { return textArea().isEditable(); }
  default StyledEditorKit getEditorKit() { return (StyledEditorKit) textArea().getEditorKit(); }
  default void cleanupText() { textArea().cleanupText();}

}
